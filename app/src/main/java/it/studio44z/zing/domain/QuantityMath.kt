package it.studio44z.zing.domain

import java.text.Normalizer
import java.util.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class IngredientAmount(
    val name: String,
    val quantity: Double?,
    val unit: String,
    val note: String? = null,
)

data class LotAmount(
    val name: String,
    val quantity: Double,
    val unit: String,
    val expiryEpochMillis: Long? = null,
)

data class IngredientCoverage(
    val ingredient: IngredientAmount,
    val available: Double?,
    val missing: Double?,
    val availableUnit: String = ingredient.unit,
    val ratio: Double,
)

data class RecipeCoverage(
    val percentage: Int,
    val fullyAvailable: Boolean,
    val missing: List<IngredientCoverage>,
    val details: List<IngredientCoverage>,
)

object QuantityMath {
    private const val RANGE_NOTE_PREFIX = "__ZEST_RANGE__:"

    private enum class Dimension { MASS, VOLUME, COUNT, OTHER }
    private data class UnitSpec(val dimension: Dimension, val factorToBase: Double, val canonical: String)

    private fun unitSpec(raw: String): UnitSpec {
        val u = raw.trim().lowercase(Locale.ITALIAN)
            .replace(".", "")
            .replace(" ", "")
            .replace('à', 'a')
        return when (u) {
            "g", "gr", "grammo", "grammi" -> UnitSpec(Dimension.MASS, 1.0, "g")
            "kg", "chilogrammo", "chilogrammi" -> UnitSpec(Dimension.MASS, 1000.0, "g")
            "mg", "milligrammo", "milligrammi" -> UnitSpec(Dimension.MASS, 0.001, "g")
            "oz", "ounce", "ounces" -> UnitSpec(Dimension.MASS, 28.349523125, "g")
            "lb", "lbs", "pound", "pounds" -> UnitSpec(Dimension.MASS, 453.59237, "g")
            "ml", "millilitro", "millilitri" -> UnitSpec(Dimension.VOLUME, 1.0, "ml")
            "cl" -> UnitSpec(Dimension.VOLUME, 10.0, "ml")
            "dl" -> UnitSpec(Dimension.VOLUME, 100.0, "ml")
            "floz", "fluidounce", "fluidounces" -> UnitSpec(Dimension.VOLUME, 29.5735295625, "ml")
            "cup", "cups", "tazza", "tazze" -> UnitSpec(Dimension.VOLUME, 236.5882365, "ml")
            "tbsp", "tablespoon", "tablespoons" -> UnitSpec(Dimension.VOLUME, 14.78676478125, "ml")
            "tsp", "teaspoon", "teaspoons" -> UnitSpec(Dimension.VOLUME, 4.92892159375, "ml")
            "l", "lt", "litro", "litri" -> UnitSpec(Dimension.VOLUME, 1000.0, "ml")
            "pz", "pza", "pezzo", "pezzi", "unita", "u", "unit", "units" -> UnitSpec(Dimension.COUNT, 1.0, "pz")
            else -> UnitSpec(Dimension.OTHER, 1.0, u)
        }
    }

    fun areCompatibleUnits(first: String, second: String): Boolean {
        val a = unitSpec(first)
        val b = unitSpec(second)
        return a.dimension == b.dimension && (a.dimension != Dimension.OTHER || a.canonical == b.canonical)
    }

    fun convert(quantity: Double, fromUnit: String, toUnit: String): Double? {
        val from = unitSpec(fromUnit)
        val to = unitSpec(toUnit)
        if (from.dimension != to.dimension) return null
        if (from.dimension == Dimension.OTHER && from.canonical != to.canonical) return null
        return quantity * from.factorToBase / to.factorToBase
    }

    fun scale(quantity: Double?, originalServings: Int, desiredServings: Int): Double? {
        if (quantity == null) return null
        require(originalServings > 0) { "Le porzioni originali devono essere maggiori di zero" }
        require(desiredServings > 0) { "Le nuove porzioni devono essere maggiori di zero" }
        return clean(quantity * desiredServings.toDouble() / originalServings.toDouble())
    }

    fun clean(value: Double): Double {
        if (!value.isFinite()) return value
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        return if (abs(rounded) < 0.0001) 0.0 else rounded
    }

    fun format(quantity: Double?, unit: String, note: String? = null): String {
        if (quantity == null) return note?.takeIf { it.isNotBlank() && !isRangeNote(it) } ?: "q.b."
        val rangeAmount = formatRangeAmount(quantity, note)
        val amount = rangeAmount ?: formatNumber(quantity)
        return listOf(amount, unit.trim()).filter { it.isNotBlank() }.joinToString(" ")
    }

    fun normalizeIngredient(ingredient: IngredientAmount): IngredientAmount {
        val originalName = ingredient.name.trim()
        val originalUnit = ingredient.unit.trim()
        val quantity = ingredient.quantity?.takeIf { it.isFinite() }
        val unitMissing = originalUnit.isBlank() || originalUnit.equals("q.b.", ignoreCase = true)

        val range = legacyRangeRegex.find(originalName)
        if (range != null) {
            val low = range.groupValues[1].toNumberOrNull()
            val high = range.groupValues[2].toNumberOrNull()
            val parsedUnit = canonicalUnit(range.groupValues[3])
            val tail = range.groupValues[4].trim()
            if (low != null && high != null && high >= low && tail.isNotBlank() && parsedUnit.isNotBlank()) {
                val midpoint = (low + high) / 2.0
                val fieldsAgree = quantity != null && abs(quantity - midpoint) < 0.01 && !unitMissing && areCompatibleUnits(originalUnit, parsedUnit)
                if (quantity == null || unitMissing || fieldsAgree) {
                    return IngredientAmount(
                        name = stripLeadingRecipePreposition(tail),
                        quantity = midpoint,
                        unit = parsedUnit,
                        note = rangeNote(low, high),
                    )
                }
            }
        }

        val single = legacySingleRegex.find(originalName)
        if (single != null) {
            val parsedQuantity = single.groupValues[1].toNumberOrNull()
            val parsedUnit = canonicalUnit(single.groupValues[2].ifBlank { "pz" })
            val tail = stripLeadingRecipePreposition(single.groupValues[3].trim())
            if (parsedQuantity != null && tail.isNotBlank()) {
                val fieldsAgree = quantity != null && abs(quantity - parsedQuantity) < 0.01 && !unitMissing && areCompatibleUnits(originalUnit, parsedUnit)
                if (quantity == null || unitMissing || fieldsAgree) {
                    return IngredientAmount(
                        name = tail,
                        quantity = quantity ?: parsedQuantity,
                        unit = if (!unitMissing && fieldsAgree) originalUnit else parsedUnit,
                        note = ingredient.note?.takeUnless { it.equals("q.b.", ignoreCase = true) || isRangeNote(it) },
                    )
                }
            }
        }

        return ingredient.copy(
            name = originalName,
            quantity = quantity,
            unit = when {
                originalUnit.isNotBlank() -> originalUnit
                quantity != null -> "pz"
                else -> "q.b."
            },
            note = ingredient.note?.takeIf { it.isNotBlank() },
        )
    }

    fun isRangeNote(note: String?): Boolean = note?.startsWith(RANGE_NOTE_PREFIX) == true

    fun rangeNote(low: Double, high: Double): String {
        val midpoint = (low + high) / 2.0
        if (midpoint <= 0.0) return RANGE_NOTE_PREFIX + "1.0:1.0"
        val lowRatio = low / midpoint
        val highRatio = high / midpoint
        return RANGE_NOTE_PREFIX + "$lowRatio:$highRatio"
    }

    fun formatRangeAmount(quantity: Double?, note: String?): String? {
        if (quantity == null || !isRangeNote(note)) return null
        val ratios = note!!.removePrefix(RANGE_NOTE_PREFIX).split(':')
        if (ratios.size != 2) return null
        val lowRatio = ratios[0].toDoubleOrNull() ?: return null
        val highRatio = ratios[1].toDoubleOrNull() ?: return null
        return "${formatNumber(quantity * lowRatio)}–${formatNumber(quantity * highRatio)}"
    }

    private fun formatNumber(rawValue: Double): String {
        val value = clean(rawValue)
        return if (abs(value - value.toLong()) < 0.0001) value.toLong().toString()
        else String.format(Locale.ITALIAN, "%.2f", value).trimEnd('0').trimEnd(',')
    }

    private fun String.toNumberOrNull(): Double? = replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }

    private fun canonicalUnit(raw: String): String = when (raw.trim().lowercase(Locale.ITALIAN)) {
        "grammo", "grammi", "gr" -> "g"
        "chilogrammo", "chilogrammi" -> "kg"
        "milligrammo", "milligrammi" -> "mg"
        "millilitro", "millilitri" -> "ml"
        "litro", "litri", "lt" -> "l"
        "ounces", "ounce" -> "oz"
        "pound", "pounds", "lbs" -> "lb"
        "fluid ounces", "fluid ounce", "floz", "fl oz" -> "fl oz"
        "cups" -> "cup"
        "tablespoon", "tablespoons" -> "tbsp"
        "teaspoon", "teaspoons" -> "tsp"
        "pezzo", "pezzi", "pezza", "pezze", "unità", "unita" -> "pz"
        else -> raw.trim().lowercase(Locale.ITALIAN)
    }

    private fun stripLeadingRecipePreposition(value: String): String = value.replace(
        Regex("""^(?:di|del|della|dello|dell['’]|delle|degli)\s+""", RegexOption.IGNORE_CASE),
        "",
    ).trim()

    private val amountUnitPattern = """(?:fluid\s+ounces|fluid\s+ounce|tablespoons|tablespoon|teaspoons|teaspoon|confezioni|confezione|cucchiaini|cucchiaino|cucchiai|cucchiaio|vaschette|vaschetta|porzioni|porzione|fette|fetta|foglie|foglia|grammi|grammo|chilogrammi|chilogrammo|millilitri|millilitro|milligrammi|milligrammo|pezzi|pezzo|cups|cup|fl\s*oz|lbs|lb|oz|tbsp|tsp|pz|kg|mg|ml|cl|dl|lt|g|l)"""
    private val legacyRangeRegex = Regex(
        """^\s*(\d+(?:[.,]\d+)?)\s*[-–]\s*(\d+(?:[.,]\d+)?)\s*($amountUnitPattern)\s+(?:(?:di|del|della|dello|dell['’]|delle|degli)\s+)?(.+?)\s*$""",
        RegexOption.IGNORE_CASE,
    )
    private val legacySingleRegex = Regex(
        """^\s*(\d+(?:[.,]\d+)?)\s*($amountUnitPattern)?\s+(.+?)\s*$""",
        RegexOption.IGNORE_CASE,
    )

    fun normalizeName(raw: String): String {
        val decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ITALIAN)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val singularAliases = mapOf(
            "pomodori" to "pomodoro", "uova" to "uovo", "zucchine" to "zucchina",
            "cipolle" to "cipolla", "carote" to "carota", "patate" to "patata",
            "melanzane" to "melanzana", "peperoni" to "peperone", "funghi" to "fungo",
            "mele" to "mela", "pere" to "pera", "banane" to "banana",
            "arance" to "arancia", "limoni" to "limone", "lenticchie" to "lenticchia",
            "ceci" to "cece", "fagioli" to "fagiolo", "piselli" to "pisello",
            "fragole" to "fragola", "olive" to "oliva", "noci" to "noce",
        )
        return singularAliases[decomposed] ?: decomposed
    }

    private fun canonicalIngredientName(raw: String): String {
        val normalized = normalizeName(raw)
        val canonical = RecipeIngredientAliases.canonicalByLocalizedName[normalized] ?: raw
        return normalizeName(canonical)
    }

    fun namesMatch(first: String, second: String): Boolean {
        // Accept the bundled English/Spanish/French/German/Portuguese labels while keeping
        // Room data untouched in its canonical Italian form.
        val a = canonicalIngredientName(first)
        val b = canonicalIngredientName(second)
        if (a.isBlank() || b.isBlank()) return false
        if (a == b) return true
        return a.startsWith("$b ") || b.startsWith("$a ")
    }

    private fun isExpired(lot: LotAmount, today: LocalDate): Boolean =
        lot.expiryEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().isBefore(today) } ?: false

    fun availableQuantity(ingredient: IngredientAmount, lots: Collection<LotAmount>): Double {
        val requiredUnit = ingredient.unit
        val today = LocalDate.now(ZoneId.systemDefault())
        return lots.asSequence()
            .filter { !isExpired(it, today) }
            .filter { namesMatch(it.name, ingredient.name) && it.quantity > 0.0 }
            .mapNotNull { lot -> convert(lot.quantity, lot.unit, requiredUnit) }
            .sum()
            .let(::clean)
    }

    fun coverage(ingredients: List<IngredientAmount>, lots: Collection<LotAmount>): RecipeCoverage {
        if (ingredients.isEmpty()) return RecipeCoverage(0, false, emptyList(), emptyList())
        val today = LocalDate.now(ZoneId.systemDefault())
        val details = ingredients.map { ingredient ->
            val matching = lots.filter { !isExpired(it, today) && namesMatch(it.name, ingredient.name) && it.quantity > 0.0 }
            if (ingredient.quantity == null) {
                val haveIt = matching.isNotEmpty()
                IngredientCoverage(ingredient, if (haveIt) null else 0.0, if (haveIt) null else null,
                    ratio = if (haveIt) 1.0 else 0.0)
            } else {
                val available = matching.mapNotNull { convert(it.quantity, it.unit, ingredient.unit) }.sum().let(::clean)
                val required = max(0.0, ingredient.quantity)
                val ratio = if (required <= 0.0) 1.0 else min(1.0, available / required)
                IngredientCoverage(
                    ingredient = ingredient,
                    available = available,
                    missing = clean(max(0.0, required - available)),
                    ratio = ratio,
                )
            }
        }
        val pct = (details.sumOf { it.ratio } / details.size * 100.0).toInt().coerceIn(0, 100)
        val missing = details.filter { it.ratio < 0.9999 }
        return RecipeCoverage(pct, missing.isEmpty(), missing, details)
    }

    fun missingQuantity(ingredient: IngredientAmount, lots: Collection<LotAmount>): Double? {
        val required = ingredient.quantity ?: return null
        val available = availableQuantity(ingredient, lots)
        return clean(max(0.0, required - available))
    }
}
