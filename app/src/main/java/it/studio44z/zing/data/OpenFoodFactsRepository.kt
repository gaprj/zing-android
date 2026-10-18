package it.studio44z.zing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class RemoteProduct(
    val barcode: String,
    val name: String,
    val brand: String = "",
    val category: String = "Altro",
    val quantityLabel: String = "",
    val imageUrl: String? = null,
    val nutritionGrade: String? = null,
    val novaGroup: Int? = null,
    val energyKcal100g: Double? = null,
    val sugarsG100g: Double? = null,
    val saturatedFatG100g: Double? = null,
    val saltG100g: Double? = null,
    val fiberG100g: Double? = null,
    val proteinG100g: Double? = null,
    val ingredientsText: String? = null,
) {
    fun parsedAmountAndUnit(): Pair<Double, String>? {
        if (quantityLabel.isBlank()) return null
        val regex = Regex("""(\d+(?:[.,]\d+)?)\s*(kg|g|ml|cl|l|pz)\b""", RegexOption.IGNORE_CASE)
        val match = regex.find(quantityLabel) ?: return null
        val rawNumber = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val rawUnit = match.groupValues[2].lowercase(Locale.ROOT)
        return when (rawUnit) {
            "cl" -> (rawNumber * 10.0) to "ml"
            else -> rawNumber to rawUnit
        }
    }
}

sealed interface ProductLookupResult {
    data class Found(val product: RemoteProduct) : ProductLookupResult
    data object NotFound : ProductLookupResult
    data class Unavailable(val reason: String) : ProductLookupResult
}

class OpenFoodFactsRepository {

    suspend fun lookup(barcode: String, preferredLang: String = "system"): ProductLookupResult = withContext(Dispatchers.IO) {
        val cleanCode = extractCleanBarcode(barcode)
        if (cleanCode.isBlank()) return@withContext ProductLookupResult.NotFound

        val lang = if (preferredLang == "system" || preferredLang.isBlank()) {
            Locale.getDefault().language.ifBlank { "it" }
        } else {
            preferredLang
        }

        val endpoints = listOf(
            "https://world.openfoodfacts.org/api/v2/product/$cleanCode.json",
            "https://it.openfoodfacts.org/api/v0/product/$cleanCode.json",
            "https://world.openproductsfacts.org/api/v0/product/$cleanCode.json"
        )

        var lastError: Exception? = null
        for (endpoint in endpoints) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(endpoint)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 10000
                    setRequestProperty("User-Agent", "ZingPantry/1.1 (Android; contact: studio44z.info@gmail.com)")
                    setRequestProperty("Accept", "application/json")
                }

                val httpStatus = connection.responseCode
                if (httpStatus == HttpURLConnection.HTTP_NOT_FOUND) {
                    continue
                }
                if (httpStatus !in 200..299) {
                    continue
                }

                val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = JSONObject(body)
                val statusObj = root.opt("status")
                val found = when (statusObj) {
                    is Number -> statusObj.toInt() == 1
                    is String -> statusObj == "1" || statusObj.startsWith("success")
                    else -> root.has("product")
                }
                if (!found) continue

                val product = root.optJSONObject("product") ?: continue
                val parsed = parseProductJson(cleanCode, product, lang)
                if (parsed != null) {
                    return@withContext ProductLookupResult.Found(parsed)
                }
            } catch (e: Exception) {
                lastError = e
            } finally {
                connection?.disconnect()
            }
        }

        if (lastError != null) {
            return@withContext ProductLookupResult.Unavailable(
                when (lastError) {
                    is java.net.SocketTimeoutException -> "Il catalogo sta rispondendo lentamente. Puoi inserire il prodotto a mano."
                    is java.net.UnknownHostException -> "Nessuna connessione a Internet. Puoi inserire il prodotto a mano."
                    else -> "Ricerca online temporaneamente non disponibile."
                }
            )
        }
        ProductLookupResult.NotFound
    }

    private fun extractCleanBarcode(raw: String): String {
        val trimmed = raw.trim()
        val eanMatch = Regex("""\b(\d{8,14})\b""").find(trimmed)
        return eanMatch?.groupValues?.get(1) ?: trimmed.filter { it.isDigit() }.ifBlank { trimmed }
    }

    private fun parseProductJson(code: String, product: JSONObject, lang: String): RemoteProduct? {
        val brand = product.optString("brands", "")
            .split(',')
            .firstOrNull()
            ?.trim()
            .orEmpty()

        val nameCandidates = mutableListOf(
            product.optString("product_name_$lang", ""),
            product.optString("product_name_it", ""),
            product.optString("product_name_en", ""),
            product.optString("product_name_es", ""),
            product.optString("product_name_fr", ""),
            product.optString("product_name_de", ""),
            product.optString("product_name_pt", ""),
            product.optString("product_name", ""),
            product.optString("abbreviated_product_name", ""),
            product.optString("generic_name_$lang", ""),
            product.optString("generic_name_it", ""),
            product.optString("generic_name_en", ""),
            product.optString("generic_name", "")
        )

        val keys = product.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k.startsWith("product_name_") || k.startsWith("generic_name_")) {
                nameCandidates.add(product.optString(k, ""))
            }
        }

        val name = nameCandidates.firstOrNull { it.isNotBlank() }?.trim()
            ?: brand.takeIf { it.isNotBlank() }
            ?: return null

        val tags = product.optJSONArray("categories_tags")
        val tagText = buildString {
            append(product.optString("categories", ""))
            if (tags != null) {
                for (index in 0 until tags.length()) {
                    append(' ').append(tags.optString(index))
                }
            }
        }

        val quantityLabel = listOf(
            product.optString("quantity", ""),
            product.optString("product_quantity", "").let { q ->
                val u = product.optString("product_quantity_unit", "g")
                if (q.isNotBlank()) "$q $u" else ""
            }
        ).firstOrNull { it.isNotBlank() }?.trim().orEmpty()

        val imageUrl = listOf(
            product.optString("image_front_url", ""),
            product.optString("image_front_small_url", ""),
            product.optString("image_url", "")
        ).firstOrNull { it.startsWith("https://") }
        val nutriments = product.optJSONObject("nutriments") ?: JSONObject()
        fun nutrient(vararg keys: String): Double? = keys.firstNotNullOfOrNull { key ->
            if (!nutriments.has(key) || nutriments.isNull(key)) null else {
                val value = when (val raw = nutriments.opt(key)) {
                    is Number -> raw.toDouble()
                    is String -> raw.replace(',', '.').toDoubleOrNull()
                    else -> null
                }
                value?.takeIf { it.isFinite() && it >= 0.0 }
            }
        }
        val energyKcal = nutrient("energy-kcal_100g")
            ?: nutrient("energy-kj_100g")?.div(4.184)
        val grade = listOf(product.optString("nutriscore_grade", ""), product.optString("nutrition_grade_fr", ""))
            .firstOrNull { it.lowercase(Locale.ROOT) in setOf("a", "b", "c", "d", "e") }
        val ingredientsText = listOf(
            product.optString("ingredients_text_$lang", ""),
            product.optString("ingredients_text_it", ""),
            product.optString("ingredients_text_en", ""),
            product.optString("ingredients_text", ""),
        ).firstOrNull { it.isNotBlank() }

        return RemoteProduct(
            barcode = code,
            name = name,
            brand = brand,
            category = mapCategory(tagText),
            quantityLabel = quantityLabel,
            imageUrl = imageUrl,
            nutritionGrade = grade,
            novaGroup = nutriments.optIntOrNull("nova-group_100g") ?: nutriments.optIntOrNull("nova-group") ?: product.optIntOrNull("nova_group"),
            energyKcal100g = energyKcal,
            sugarsG100g = nutrient("sugars_100g"),
            saturatedFatG100g = nutrient("saturated-fat_100g"),
            saltG100g = nutrient("salt_100g"),
            fiberG100g = nutrient("fiber_100g", "fibers_100g"),
            proteinG100g = nutrient("proteins_100g"),
            ingredientsText = ingredientsText,
        )
    }

    private fun JSONObject.optIntOrNull(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        return when (val value = opt(key)) {
            is Number -> value.toInt().takeIf { it in 1..4 }
            is String -> value.toDoubleOrNull()?.toInt()?.takeIf { it in 1..4 }
            else -> null
        }
    }

    private fun mapCategory(raw: String): String {
        val value = raw.lowercase(Locale.ROOT)
        return when {
            listOf("fruit", "vegetable", "frutta", "verdura", "ortaggi", "legumi", "plant-based")
                .any { value.contains(it) } -> "Frutta e verdura"
            listOf("dairy", "dairies", "milk", "cheese", "yogurt", "lattic", "latte", "formagg", "burro", "mozzarella")
                .any { value.contains(it) } -> "Latticini"
            listOf("meat", "poultry", "fish", "seafood", "carne", "pesce", "salumi", "pollo", "tonno")
                .any { value.contains(it) } -> "Carne e pesce"
            listOf("beverage", "drink", "bevanda", "acqua", "succo", "succhi", "bibite", "vino", "birra", "waters", "juices")
                .any { value.contains(it) } -> "Bevande"
            listOf("pasta", "rice", "cereal", "cereale", "riso", "farina", "avena", "noodles", "spaghetti")
                .any { value.contains(it) } -> "Pasta e cereali"
            listOf("frozen", "congelat", "surgelat", "gelat", "ice-cream")
                .any { value.contains(it) } -> "Surgelati"
            listOf("canned", "conserv", "sottoli", "salse", "sughi", "pelati", "passata", "sauces", "spreads")
                .any { value.contains(it) } -> "Conserve"
            listOf("bread", "bakery", "pane", "forno", "biscott", "fette biscottate", "merend", "cracker", "cakes")
                .any { value.contains(it) } -> "Pane e forno"
            listOf("snack", "chips", "chocolate", "cioccolat", "patatine", "dolci", "sweet", "cocoa")
                .any { value.contains(it) } -> "Snack"
            else -> "Altro"
        }
    }
}