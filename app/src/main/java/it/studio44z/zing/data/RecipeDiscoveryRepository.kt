package it.studio44z.zing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/** A recipe entry retrieved from a free Italian Wikibooks recipe page. */
data class RemoteRecipeSuggestion(
    val id: String,
    val name: String,
    val category: String = "",
    val area: String = "",
    val imageUrl: String? = null,
    val sourceUrl: String? = null,
    val instructions: String = "",
    val ingredients: List<Pair<String, String>> = emptyList(),
    val matchedIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
) {
    val matchPercent: Int
        get() = if (ingredients.isEmpty()) 0 else (matchedIngredients.size * 100 / ingredients.size).coerceIn(0, 100)
}

/**
 * Free, no-key recipe discovery from Italian Wikibooks.
 *
 * Searches the public MediaWiki API for cookbook pages, retrieves their wikitext in batches,
 * and only suggests pages for which the Ingredienti section can be parsed as a list. The
 * recipe itself remains on Wikibooks; this app only derives the ingredient comparison and
 * links back to the source page. Wikibooks text is CC BY-SA 4.0 / GFDL as indicated by each
 * page, so the UI also exposes the attribution and license.
 */
class RecipeDiscoveryRepository {
    suspend fun searchWithPantry(pantryNames: List<String>, limit: Int = 12): List<RemoteRecipeSuggestion> =
        withContext(Dispatchers.IO) {
            val pantry = pantryNames.map(String::trim)
                .filter(String::isNotBlank)
                .filterNot(RecipeIngredientRules::isPantryStaple)
                .distinct()
            if (pantry.isEmpty()) return@withContext emptyList()

            val cacheKey = pantry.map(::normalizeForCompare).sorted().joinToString("|")
            val now = System.currentTimeMillis()
            searchCache[cacheKey]?.takeIf { now - it.savedAt < SEARCH_CACHE_TTL_MS }
                ?.let { return@withContext it.results.take(limit.coerceIn(1, 20)) }

            val queryIngredients = pantry.mapNotNull(::searchTermsForPantryItem).distinctBy { it.joinToString("|") }.take(4)
            if (queryIngredients.isEmpty()) return@withContext emptyList()

            // Query one pantry ingredient at a time, including common Italian variants. At most
            // four lightweight search requests run concurrently; page contents are fetched in batches.
            val rankByTitle = linkedMapOf<String, Int>()
            val titleLists = coroutineScope {
                queryIngredients.map { terms -> async(Dispatchers.IO) { fetchCandidateTitles(terms) } }.awaitAll()
            }
            titleLists.flatten().forEach { title -> rankByTitle[title] = (rankByTitle[title] ?: 0) + 1 }

            val orderedTitles = rankByTitle.entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.lowercase(Locale.ITALIAN) })
                .take(MAX_CANDIDATE_PAGES)
                .map { it.key }

            val pagesToFetch = orderedTitles.filter { title ->
                val cached = pageCache[title]
                cached == null || now - cached.savedAt >= PAGE_CACHE_TTL_MS
            }
            pagesToFetch.chunked(PAGES_PER_BATCH).forEach { batch ->
                fetchRecipePages(batch).forEach { (title, recipe) ->
                    pageCache[title] = CachedPage(recipe, now)
                }
                delay(180)
            }

            val matchedResults = orderedTitles.mapNotNull { title ->
                val recipe = pageCache[title]?.recipe ?: return@mapNotNull null
                val required = recipe.ingredients.filterNot { RecipeIngredientRules.isPantryStaple(it.first) }
                if (required.size < 2) return@mapNotNull null

                val matched = required.map { it.first }
                    .filter { recipeIngredient -> pantry.any { ItalianIngredientMatcher.matches(it, recipeIngredient) } }
                    .distinct()
                if (matched.isEmpty()) return@mapNotNull null
                val missing = required.map { it.first }
                    .filterNot { recipeIngredient -> pantry.any { ItalianIngredientMatcher.matches(it, recipeIngredient) } }
                    .distinct()
                recipe.copy(
                    ingredients = required,
                    matchedIngredients = matched,
                    missingIngredients = missing,
                )
            }.sortedWith(
                compareByDescending<RemoteRecipeSuggestion> { it.matchPercent }
                    .thenByDescending { it.matchedIngredients.size }
                    .thenByDescending { rankByTitle[it.id.removePrefix(ID_PREFIX)] ?: 0 }
                    .thenBy { it.name.lowercase(Locale.ITALIAN) }
            ).take(limit.coerceIn(1, 20))

            searchCache[cacheKey] = CachedSearch(matchedResults, now)
            matchedResults
        }

    private fun fetchCandidateTitles(searchTerms: List<String>): List<String> {
        if (searchTerms.isEmpty()) return emptyList()
        val query = searchTerms.distinct().take(6).joinToString(" OR ") { term -> "insource:\"$term\"" }
        val url = "$API_BASE?action=query&list=search&srsearch=${encode(query)}&srnamespace=0&srlimit=$RESULTS_PER_INGREDIENT&format=json&formatversion=2"
        val root = getJson(url) ?: return emptyList()
        val search = root.optJSONObject("query")?.optJSONArray("search") ?: return emptyList()
        return (0 until search.length()).mapNotNull { index ->
            val title = search.optJSONObject(index)?.optString("title", "")?.trim().orEmpty()
            title.takeIf {
                it.startsWith(RECIPE_NAMESPACE_PREFIX) &&
                    !it.endsWith("/Ricette per ingrediente", ignoreCase = true) &&
                    !it.endsWith("/Ricette per regione", ignoreCase = true)
            }
        }.distinct()
    }

    private fun fetchRecipePages(titles: List<String>): List<Pair<String, RemoteRecipeSuggestion>> {
        if (titles.isEmpty()) return emptyList()
        val titleParameter = encode(titles.joinToString("|"))
        val url = "$API_BASE?action=query&prop=revisions&rvprop=content&rvslots=main&titles=$titleParameter&format=json&formatversion=2"
        val root = getJson(url) ?: return emptyList()
        val pages = root.optJSONObject("query")?.optJSONArray("pages") ?: return emptyList()
        val parsed = mutableListOf<Pair<String, RemoteRecipeSuggestion>>()
        for (index in 0 until pages.length()) {
            val page = pages.optJSONObject(index) ?: continue
            if (page.optBoolean("missing", false)) continue
            val title = page.optString("title", "").trim()
            if (!title.startsWith(RECIPE_NAMESPACE_PREFIX)) continue
            val revision = page.optJSONArray("revisions")?.optJSONObject(0) ?: continue
            val slot = revision.optJSONObject("slots")?.optJSONObject("main") ?: continue
            val wikitext = slot.optString("content", slot.optString("*", ""))
            if (wikitext.isBlank()) continue
            val recipe = parseRecipePage(title, wikitext) ?: continue
            parsed += title to recipe
        }
        return parsed
    }

    private fun parseRecipePage(title: String, wikitext: String): RemoteRecipeSuggestion? {
        val ingredients = parseIngredientSection(wikitext)
        if (ingredients.size < 2) return null
        val name = title.substringAfterLast('/').replace('_', ' ').trim()
        if (name.isBlank()) return null
        return RemoteRecipeSuggestion(
            id = ID_PREFIX + title,
            name = name,
            category = "Wikibooks",
            area = "",
            sourceUrl = articleUrl(title),
            ingredients = ingredients.map { it to "" },
        )
    }

    /** Only accepts explicit bullet/numbered ingredient lists; prose-only pages are skipped. */
    internal fun parseIngredientSection(wikitext: String): List<String> {
        var insideIngredients = false
        val results = mutableListOf<String>()
        for (rawLine in wikitext.lineSequence()) {
            val line = rawLine.trim()
            val heading = HEADING_REGEX.matchEntire(line)?.groupValues?.getOrNull(1)?.trim()
            if (heading != null) {
                if (insideIngredients) break
                if (normalizeForCompare(heading).startsWith("ingredienti")) insideIngredients = true
                continue
            }
            if (!insideIngredients) continue
            if (!(line.startsWith('*') || line.startsWith('#'))) continue
            val clean = cleanWikitextIngredient(line.dropWhile { it == '*' || it == '#' || it == ':' || it == ';' })
            if (clean.length < 3 || clean.length > 140) continue
            if (normalizeForCompare(clean).startsWith("vedi anche")) continue
            results += clean
        }
        return results.distinctBy(::normalizeForCompare)
    }

    private fun cleanWikitextIngredient(raw: String): String {
        var value = raw.trim()
        value = value.replace(Regex("(?is)<ref\\b[^>]*>.*?</ref>"), " ")
            .replace(Regex("(?is)<ref\\b[^>]*/\\s*>"), " ")
            .replace(Regex("(?is)<[^>]+>"), " ")
            .replace(Regex("\\[\\[([^\\]]+)]]")) { match ->
                val target = match.groupValues[1]
                target.substringAfterLast('|').substringAfterLast(':').trim()
            }
            .replace(Regex("\\{\\{[^{}]*}}"), " ")
            .replace(Regex("'{2,}"), "")
            .replace(Regex("\\[\\s*\\d+\\s*]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '\t', '.', ';', ':')
        return value
    }

    private fun searchTermsForPantryItem(raw: String): List<String>? {
        val normalized = normalizeForCompare(raw)
        if (normalized.isBlank()) return null
        val group = SEARCH_SYNONYMS.firstOrNull { (_, aliases) ->
            aliases.any { alias -> containsPhrase(normalized, normalizeForCompare(alias)) }
        }
        if (group != null) return group.second
        val ignored = setOf("di", "del", "dello", "della", "dei", "degli", "delle", "da", "dal", "dalla", "con", "per", "in", "a", "al", "alla", "alle", "fresco", "fresca", "freschi", "fresche", "secco", "secca", "tritato", "tritata", "grattugiato", "grattugiata", "tagliato", "tagliata", "a fette", "q b")
        val words = normalized.split(' ').filter { it.isNotBlank() && it !in ignored }
        val key = words.lastOrNull()?.takeIf { it.length >= 3 } ?: return null
        return listOf(key)
    }

    private fun getJson(url: String): JSONObject? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ZingAndroid/1.0 (Android; contact: studio44z.info@gmail.com)")
            }
            if (connection.responseCode !in 200..299) null
            else connection.inputStream.bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun articleUrl(title: String): String = "https://it.wikibooks.org/wiki/" +
        title.split('/').joinToString("/") { URLEncoder.encode(it, Charsets.UTF_8.name()).replace("+", "_") }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun containsPhrase(text: String, phrase: String): Boolean =
        text == phrase || text.startsWith("$phrase ") || text.endsWith(" $phrase") || text.contains(" $phrase ")

    private data class CachedPage(val recipe: RemoteRecipeSuggestion, val savedAt: Long)
    private data class CachedSearch(val results: List<RemoteRecipeSuggestion>, val savedAt: Long)

    companion object {
        private const val API_BASE = "https://it.wikibooks.org/w/api.php"
        private const val RECIPE_NAMESPACE_PREFIX = "Libro di cucina/Ricette/"
        private const val ID_PREFIX = "wikibooks:"
        private const val RESULTS_PER_INGREDIENT = 18
        private const val MAX_CANDIDATE_PAGES = 54
        private const val PAGES_PER_BATCH = 18
        private const val SEARCH_CACHE_TTL_MS = 4 * 60 * 60 * 1000L
        private val HEADING_REGEX = Regex("={2,6}\\s*(.*?)\\s*={2,6}")
        private const val PAGE_CACHE_TTL_MS = 14 * 24 * 60 * 60 * 1000L
        private val pageCache = ConcurrentHashMap<String, CachedPage>()
        private val searchCache = ConcurrentHashMap<String, CachedSearch>()

        private val SEARCH_SYNONYMS: List<Pair<String, List<String>>> = listOf(
            "pomodoro" to listOf("pomodoro", "pomodori", "passata", "pelati", "tomato"),
            "uovo" to listOf("uovo", "uova", "tuorlo", "tuorli", "egg", "eggs"),
            "pollo" to listOf("pollo", "pollame", "chicken"),
            "cipolla" to listOf("cipolla", "cipolle", "onion"),
            "aglio" to listOf("aglio", "garlic"),
            "carota" to listOf("carota", "carote", "carrot"),
            "patata" to listOf("patata", "patate", "potato"),
            "zucchina" to listOf("zucchina", "zucchine", "zucchini", "courgette"),
            "melanzana" to listOf("melanzana", "melanzane", "aubergine", "eggplant"),
            "peperone" to listOf("peperone", "peperoni", "bell pepper"),
            "riso" to listOf("riso", "risotto", "rice"),
            "pasta" to listOf("pasta", "spaghetti", "penne", "maccheroni"),
            "farina" to listOf("farina", "farine", "flour"),
            "formaggio" to listOf("formaggio", "formaggi", "cheese"),
            "parmigiano" to listOf("parmigiano", "parmesan"),
            "pecorino" to listOf("pecorino", "pecorino romano"),
            "mozzarella" to listOf("mozzarella"),
            "ricotta" to listOf("ricotta"),
            "burro" to listOf("burro", "butter"),
            "latte" to listOf("latte", "milk"),
            "panna" to listOf("panna", "cream"),
            "basilico" to listOf("basilico", "basil"),
            "prezzemolo" to listOf("prezzemolo", "parsley"),
            "limone" to listOf("limone", "limoni", "lemon"),
            "arancia" to listOf("arancia", "arance", "orange"),
            "manzo" to listOf("manzo", "bovino", "beef"),
            "maiale" to listOf("maiale", "pancetta", "guanciale", "pork"),
            "salsiccia" to listOf("salsiccia", "salsicce", "sausage"),
            "prosciutto" to listOf("prosciutto", "ham"),
            "tonno" to listOf("tonno", "tuna"),
            "salmone" to listOf("salmone", "salmon"),
            "acciuga" to listOf("acciuga", "acciughe", "alici", "alice", "anchovy"),
            "gambero" to listOf("gambero", "gamberi", "gamberetti", "shrimp", "prawn"),
            "cozza" to listOf("cozza", "cozze", "mussels"),
            "vongola" to listOf("vongola", "vongole", "clam"),
            "calamaro" to listOf("calamaro", "calamari", "squid"),
            "fungo" to listOf("fungo", "funghi", "mushroom"),
            "spinacio" to listOf("spinacio", "spinaci", "spinach"),
            "cece" to listOf("cece", "ceci", "chickpea"),
            "fagiolo" to listOf("fagiolo", "fagioli", "beans"),
            "lenticchia" to listOf("lenticchia", "lenticchie", "lentils"),
            "pisello" to listOf("pisello", "piselli", "pea", "peas"),
            "zucca" to listOf("zucca", "pumpkin"),
            "sedano" to listOf("sedano", "celery"),
            "porro" to listOf("porro", "porri", "leek"),
            "asparago" to listOf("asparago", "asparagi", "asparagus"),
            "broccolo" to listOf("broccolo", "broccoli"),
            "cavolo" to listOf("cavolo", "cavoli", "cavolo verza", "cabbage"),
            "olive" to listOf("oliva", "olive", "olives"),
            "cappero" to listOf("cappero", "capperi", "caper"),
            "peperoncino" to listOf("peperoncino", "peperoncini", "chili"),
            "zucchero" to listOf("zucchero", "sugar"),
            "cioccolato" to listOf("cioccolato", "chocolate"),
            "cacao" to listOf("cacao", "cocoa"),
            "mela" to listOf("mela", "mele", "apple"),
            "pera" to listOf("pera", "pere", "pear"),
            "noce" to listOf("noce", "noci", "walnut"),
            "mandorla" to listOf("mandorla", "mandorle", "almond"),
        )

        internal fun normalizeForCompare(raw: String): String {
            val deaccented = Normalizer.normalize(raw.lowercase(Locale.ITALIAN), Normalizer.Form.NFD)
                .replace(Regex("\\p{Mn}+"), "")
            return deaccented.replace('_', ' ').replace('-', ' ')
                .replace(Regex("[^a-z0-9 ]"), " ")
                .replace(Regex("\\s+"), " ").trim()
        }
    }
}

/** Conservative Italian ingredient matching; generic pantry labels don't satisfy specific foods. */
internal object ItalianIngredientMatcher {
    private val stopWords = setOf(
        "di", "del", "dello", "della", "dei", "degli", "delle", "da", "dal", "dallo", "dalla", "dai", "dagli", "dalle",
        "al", "allo", "alla", "ai", "agli", "alle", "a", "ad", "e", "ed", "o", "oppure", "con", "per", "in", "su", "tra", "fra",
        "q", "b", "quanto", "basta", "un", "uno", "una", "uno", "mezzo", "mezza", "circa", "eventualmente", "facoltativo", "facoltativa",
        "fresco", "fresca", "freschi", "fresche", "secco", "secca", "secchi", "secche", "tritato", "tritata", "tritati", "tritate",
        "tagliato", "tagliata", "tagliati", "tagliate", "grattugiato", "grattugiata", "macinato", "macinata", "sminuzzato", "sminuzzata",
        "affettato", "affettata", "pelato", "pelata", "sbucciato", "sbucciata", "lesso", "lessa", "cotto", "cotta", "cotti", "cotte",
        "piccolo", "piccola", "piccoli", "piccole", "grande", "grandi", "medio", "media", "medi", "medie", "maturo", "matura", "maturi", "mature",
        "g", "gr", "kg", "mg", "ml", "cl", "dl", "l", "litro", "litri", "cucchiaio", "cucchiai", "cucchiaino", "cucchiaini",
        "tazza", "tazze", "bicchiere", "bicchieri", "mestolo", "mestoli", "pizzico", "pizzichi", "rametto", "rametti", "foglia", "foglie",
        "spicchio", "spicchi", "fetta", "fette", "mazzetto", "mazzetti", "dose", "dosi", "q", "b",
    )
    private val broadLabels = setOf("formaggio", "carne", "pesce", "verdura", "verdure", "frutta", "salsa", "brodo", "olio")
    private val canonicalAliases = mapOf(
        "uova" to "uovo", "tuorli" to "tuorlo", "pomodori" to "pomodoro", "pelati" to "pomodoro",
        "cipolle" to "cipolla", "patate" to "patata", "carote" to "carota", "zucchine" to "zucchina",
        "melanzane" to "melanzana", "peperoni" to "peperone", "funghi" to "fungo", "spinaci" to "spinacio",
        "ceci" to "cece", "fagioli" to "fagiolo", "lenticchie" to "lenticchia", "piselli" to "pisello",
        "gamberi" to "gambero", "gamberetti" to "gambero", "vongole" to "vongola", "cozze" to "cozza",
        "acciughe" to "acciuga", "alici" to "alice", "olive" to "oliva", "capperi" to "cappero",
        "arance" to "arancia", "limoni" to "limone", "mele" to "mela", "pere" to "pera", "noci" to "noce",
        "mandorle" to "mandorla", "asparagi" to "asparago", "broccoli" to "broccolo", "porri" to "porro",
        "maccheroni" to "pasta", "spaghetti" to "pasta", "penne" to "pasta", "tagliatelle" to "pasta",
        "parmigiano reggiano" to "parmigiano", "grana padano" to "grana",
        "beef" to "manzo", "chicken" to "pollo", "pork" to "maiale", "tomato" to "pomodoro", "egg" to "uovo",
        "onion" to "cipolla", "garlic" to "aglio", "potato" to "patata", "courgette" to "zucchina", "zucchini" to "zucchina",
        "aubergine" to "melanzana", "eggplant" to "melanzana", "mushroom" to "fungo", "spinach" to "spinacio",
        "chickpeas" to "cece", "chickpea" to "cece", "beans" to "fagiolo", "lentils" to "lenticchia", "shrimp" to "gambero",
    )

    fun matches(pantryRaw: String, recipeRaw: String): Boolean {
        val pantry = coreTokens(pantryRaw)
        val recipe = coreTokens(recipeRaw)
        if (pantry.isEmpty() || recipe.isEmpty()) return false
        val pantryCanonical = pantry.map(::canonical).toSet()
        val recipeCanonical = recipe.map(::canonical).toSet()
        if (pantryCanonical == recipeCanonical) return true
        if (pantryCanonical.any { it in recipeCanonical } || recipeCanonical.any { it in pantryCanonical }) {
            val broadPantry = pantryCanonical.size == 1 && pantryCanonical.first() in broadLabels
            val broadRecipe = recipeCanonical.size == 1 && recipeCanonical.first() in broadLabels
            if (broadPantry || broadRecipe) return pantryCanonical == recipeCanonical
            return true
        }
        return false
    }

    private fun coreTokens(raw: String): List<String> {
        var value = RecipeDiscoveryRepository.normalizeForCompare(raw)
        value = value.replace(Regex("\\b\\d+(?:[.,]\\d+)?(?:\\s*/\\s*\\d+)?\\b"), " ")
        value = value.replace(Regex("\\b(?:mezzo|mezza|un|uno|una|due|tre|quattro|cinque|sei|sette|otto|nove|dieci)\\b"), " ")
        return value.split(' ').map { it.trim() }
            .filter { it.isNotBlank() && it !in stopWords && it.length >= 2 }
            .map(::canonical)
            .distinct()
    }

    private fun canonical(token: String): String = canonicalAliases[token] ?: token
}
