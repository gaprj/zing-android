package it.studio44z.zing.data

import java.util.Locale

/** Pure matching rules used to show online recipes only when their essential ingredients are present. */
internal object RecipeIngredientRules {
    private val pantryStaples = setOf(
        "sale", "sale fino", "sale grosso", "pepe", "pepe nero", "pepe bianco", "acqua", "olio",
        "olio di oliva", "olio d oliva", "olio extravergine di oliva", "olio extravergine d oliva",
        "salt", "sea salt", "table salt", "kosher salt", "salt flakes", "pepper", "black pepper", "white pepper",
        "salt and pepper", "water", "tap water", "olive oil", "extra virgin olive oil", "vegetable oil",
        "sunflower oil", "cooking oil", "oil", "seasoning", "seasoning mix", "mixed herbs", "mixed spice",
        "mixed spices", "italian seasoning", "herbs de provence", "spices", "herbs", "dried herbs", "fresh herbs",
        "italian herbs", "chilli flakes", "chili flakes", "red pepper flakes", "crushed red pepper flakes",
        "crushed red pepper", "chilli powder", "chili powder", "paprika", "smoked paprika", "cayenne pepper", "cumin",
        "cinnamon", "ground cinnamon", "nutmeg", "ground nutmeg", "turmeric", "oregano", "dried oregano",
        "basil", "dried basil", "parsley", "dried parsley", "thyme", "dried thyme", "rosemary", "dried rosemary",
        "bay leaf", "bay leaves", "curry powder", "garam masala", "allspice", "cardamom", "coriander powder",
        "ground coriander", "ground cumin", "ground turmeric", "ground ginger", "ginger powder", "garlic powder",
        "onion powder", "mustard powder", "mustard seeds", "ground cloves", "five spice", "five spice powder",
        "black peppercorns", "peppercorns", "vanilla extract", "vanilla essence",
    )

    // Broad labels must not satisfy a more specific recipe ingredient (e.g. "cheese" != "cheddar cheese").
    private val broadIngredientLabels = setOf(
        "cheese", "cream", "milk", "flour", "oil", "meat", "fish", "seafood", "sauce", "stock", "broth",
        "herbs", "spices", "seasoning", "vegetables", "vegetable", "fruit", "fruits",
    )

    // These words describe preparation/state, not a different core ingredient.
    private val descriptors = setOf(
        "fresh", "dried", "ground", "chopped", "minced", "finely", "roughly", "sliced", "diced", "grated",
        "shredded", "peeled", "small", "large", "medium", "raw", "cooked", "frozen", "canned", "boneless",
        "skinless", "organic", "ripe", "whole", "crushed", "chopped", "thinly", "thickly", "seeded",
        "deseeded", "trimmed", "beaten", "softened", "melted", "breast", "cheese",
    )

    fun isPantryStaple(raw: String): Boolean {
        val value = normalize(raw)
        if (value in pantryStaples) return true
        val quantityFree = value
            .replace(Regex("\\b\\d+(?:[.,]\\d+)?(?:\\s*/\\s*\\d+)?\\b"), " ")
            .replace(Regex("\\b(?:g|gr|kg|mg|ml|cl|dl|l|litro|litri|cucchiaio|cucchiai|cucchiaino|cucchiaini|bicchiere|bicchieri|tazza|tazze|pizzico|pizzichi|q|b)\\b"), " ")
            .replace(Regex("\\b(?:di|del|dello|della|dei|degli|delle|da|dal|dalla|per|quanto|basta|un|uno|una|mezzo|mezza)\\b"), " ")
            .replace(Regex("\\s+"), " ").trim()
        if (quantityFree in pantryStaples) return true
        val withoutPreparationWords = quantityFree.split(' ')
            .filterNot { it in setOf("ground", "dried", "fresh", "crushed", "whole", "powder", "powdered", "leaves", "leaf", "fino", "grosso", "nera", "nero", "bianco", "bianca", "extravergine", "vergine", "oliva", "olive", "semi") }
            .joinToString(" ").trim()
        if (withoutPreparationWords in pantryStaples) return true
        if (Regex("\\b(?:sale|pepe|acqua)\\b").containsMatchIn(quantityFree) &&
            quantityFree.split(' ').all { it in setOf("sale", "pepe", "acqua", "fino", "grosso", "nera", "nero", "bianco", "bianca", "presa", "q", "b") }) return true
        if (quantityFree == "olio" || quantityFree.startsWith("olio ") || quantityFree.matches(Regex("(?:un )?filo d olio"))) return true
        return value.contains("salt and pepper") || value.contains("mixed herbs") ||
            value.contains("mixed spice") || value.contains("mixed seasoning")
    }

    /**
     * The pantry argument should already be mapped to a canonical English ingredient name.
     * Exact/core matches are preferred. Partial matches are allowed only when the extra recipe
     * words are common preparation descriptors; broad generic labels never match specific foods.
     */
    fun matchesPantryIngredient(pantryCanonical: String, recipeIngredient: String): Boolean {
        val pantry = canonicalIngredient(pantryCanonical)
        val recipe = canonicalIngredient(recipeIngredient)
        if (pantry.isBlank() || recipe.isBlank()) return false

        val pantrySingular = singular(pantry)
        val recipeSingular = singular(recipe)
        if (pantrySingular == recipeSingular) return true

        val pantryCore = core(pantry)
        val recipeCore = core(recipe)
        val pantryCoreSingular = singular(pantryCore)
        val recipeCoreSingular = singular(recipeCore)
        if (pantryCoreSingular.isNotBlank() && pantryCoreSingular == recipeCoreSingular) return true

        if (pantrySingular in broadIngredientLabels || pantryCoreSingular in broadIngredientLabels ||
            recipeSingular in broadIngredientLabels || recipeCoreSingular in broadIngredientLabels) {
            return false
        }

        val pantryTokens = pantrySingular.split(' ').filter(String::isNotBlank)
        val recipeTokens = recipeSingular.split(' ').filter(String::isNotBlank)
        if (pantryTokens.size == 1 && recipeTokens.contains(pantryTokens.single())) {
            val extras = recipeTokens.filterNot { it == pantryTokens.single() }
            if (extras.all { it in descriptors }) return true
        }
        if (recipeTokens.size == 1 && pantryTokens.contains(recipeTokens.single())) {
            val extras = pantryTokens.filterNot { it == recipeTokens.single() }
            if (extras.all { it in descriptors }) return true
        }
        return false
    }


    private fun canonicalIngredient(raw: String): String {
        val value = normalize(raw)
        return when (value) {
            "courgette", "courgettes", "zucchini", "zucchinis" -> "zucchini"
            "aubergine", "aubergines", "eggplant", "eggplants" -> "eggplant"
            "bell pepper", "bell peppers", "green pepper", "green peppers", "red pepper",
            "red peppers", "yellow pepper", "yellow peppers", "orange pepper", "orange peppers",
            "capsicum", "sweet pepper", "sweet peppers" -> "bell pepper"
            "prawn", "prawns", "shrimp", "shrimps" -> "shrimp"
            "chickpea", "chickpeas" -> "chickpea"
            else -> value
        }
    }

    private fun core(value: String): String = normalize(value)
        .split(' ')
        .filter { it.isNotBlank() && it !in descriptors }
        .joinToString(" ")
        .trim()

    private fun singular(value: String): String = value.split(' ').joinToString(" ") { word ->
        when {
            word.endsWith("ies") && word.length > 4 -> word.dropLast(3) + "y"
            word.endsWith("oes") && word.length > 4 -> word.dropLast(2)
            (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes") || word.endsWith("zes")) && word.length > 4 -> word.dropLast(2)
            word.endsWith("s") && !word.endsWith("ss") && word.length > 3 -> word.dropLast(1)
            else -> word
        }
    }

    private fun normalize(value: String): String = value.lowercase(Locale.ROOT)
        .replace('_', ' ').replace('-', ' ')
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ").trim()
}
