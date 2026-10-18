package it.studio44z.zing.data

import android.content.Context
import org.json.JSONArray
import java.nio.charset.StandardCharsets

object SeedRecipeLoader {
    suspend fun seedIfNeeded(context: Context, database: AppDatabase) {
        if (database.recipeDao().countSeed() > 0) return
        val json = context.assets.open("recipes.json").bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        val array = JSONArray(json)
        val now = System.currentTimeMillis()
        val recipes = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            RecipeEntity(
                name = item.getString("name"),
                description = item.optString("description", ""),
                category = item.optString("category", "Altro"),
                prepMinutes = item.optInt("prepMinutes", 10).coerceAtLeast(0),
                cookMinutes = item.optInt("cookMinutes", 0).coerceAtLeast(0),
                servings = item.optInt("servings", 2).coerceAtLeast(1),
                ingredientsJson = item.getJSONArray("ingredients").toString(),
                instructions = item.optString("instructions", ""),
                isSeed = true,
                createdAtMillis = now,
                updatedAtMillis = now,
            )
        }
        recipes.forEach { database.recipeDao().insertSeedIfAbsent(it) }
    }
}
