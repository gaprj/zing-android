package it.studio44z.zing.ui

import android.content.Context
import it.studio44z.zing.data.RecipeEntity
import org.json.JSONObject
import java.util.Locale

/** Presentation-only translations for bundled recipes. Room data remains canonical Italian. */
data class LocalizedRecipeText(
    val name: String,
    val description: String,
    val instructions: String,
)

class RecipeLocalizer(context: Context) {
    private val translations: JSONObject
    private val ingredients: JSONObject
    private val canonicalRecipes: Map<String, String>

    init {
        val i18nText = context.assets.open("recipe_i18n.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val i18n = JSONObject(i18nText)
        translations = i18n.optJSONObject("translations") ?: JSONObject()
        ingredients = i18n.optJSONObject("ingredients") ?: JSONObject()

        val recipesText = context.assets.open("recipes.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val source = org.json.JSONArray(recipesText)
        canonicalRecipes = buildMap {
            for (index in 0 until source.length()) {
                val item = source.optJSONObject(index) ?: continue
                val name = item.optString("name").trim()
                if (name.isNotEmpty()) put(name, item.optString("instructions"))
            }
        }
    }

    fun text(recipe: RecipeEntity, languageCode: String): LocalizedRecipeText {
        if (!recipe.isSeed) {
            return LocalizedRecipeText(recipe.name, recipe.description, recipe.instructions)
        }
        val language = languageCode.lowercase(Locale.ROOT).substringBefore('-').substringBefore('_')
        if (language == "it") {
            return LocalizedRecipeText(
                name = recipe.name,
                description = recipe.description,
                instructions = splitItalianSteps(recipe.instructions),
            )
        }
        val translation = translations.optJSONObject(language)?.optJSONObject(recipe.name)
            ?: return LocalizedRecipeText(recipe.name, recipe.description, splitItalianSteps(recipe.instructions))

        val canonicalInstructions = canonicalRecipes[recipe.name]
        // If the user has edited the seeded instructions (for example custom timer durations),
        // preserve their saved version instead of replacing it with the bundled translation.
        val savedInstructionsWereEdited = canonicalInstructions != null && recipe.instructions != canonicalInstructions
        val localizedInstructions = if (savedInstructionsWereEdited) recipe.instructions
        else translation.optJSONArray("instructions")?.let { array ->
            (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }.joinToString("\n")
        } ?: splitItalianSteps(recipe.instructions)

        return LocalizedRecipeText(
            name = translation.optString("name", recipe.name).ifBlank { recipe.name },
            description = translation.optString("description", recipe.description).ifBlank { recipe.description },
            instructions = localizedInstructions,
        )
    }

    fun ingredientName(sourceName: String, languageCode: String): String {
        val language = languageCode.lowercase(Locale.ROOT).substringBefore('-').substringBefore('_')
        if (language == "it") return sourceName
        val translation = ingredients.optJSONObject(sourceName)?.optString(language, "")?.trim().orEmpty()
        return translation.ifBlank { sourceName }
    }

    /** Translates a source page title only when it exactly matches one of our bundled recipes. */
    fun sourceRecipeTitle(sourceTitle: String, languageCode: String): String {
        val language = languageCode.lowercase(Locale.ROOT).substringBefore('-').substringBefore('_')
        val candidate = sourceTitle.substringAfterLast('/').replace('_', ' ').trim()
        if (language == "it") return candidate.ifBlank { sourceTitle }
        return translations.optJSONObject(language)?.optJSONObject(candidate)
            ?.optString("name", "")?.takeIf(String::isNotBlank) ?: candidate.ifBlank { sourceTitle }
    }

    private fun splitItalianSteps(instructions: String): String = instructions
        .replace(Regex("(?<=[.!?])\\s+(?=[A-ZÀ-ÖØ-Þ])"), "\n")
        .trim()
}
