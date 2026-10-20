package it.studio44z.zing.data

import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import it.studio44z.zing.domain.IngredientAmount
import it.studio44z.zing.domain.LotAmount
import it.studio44z.zing.domain.QuantityMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class HabitSuggestion(
    val name: String,
    val reason: String,
    val category: String = "Altro",
    val unit: String = "pz",
)

data class ImportSummary(
    val lots: Int,
    val recipes: Int,
    val shopping: Int,
    val events: Int,
)

data class RecipeShoppingUpdateResult(
    val itemsAddedOrUpdated: Int,
    val ingredientsAlreadyAvailable: Int,
    val ingredientsWithoutQuantity: Int,
    val recipesNotFound: Int,
)

private data class BackupPayload(
    val lots: List<FoodLotEntity> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
    val shopping: List<ShoppingItemEntity> = emptyList(),
    val events: List<FoodEventEntity> = emptyList(),
    val barcodeCache: List<BarcodeCacheEntity> = emptyList(),
    val appSettings: AppSettings? = null,
    val extraPreferencesJson: String? = null,
)

fun formatItalianDate(millis: Long?): String {
    if (millis == null) return ""
    return SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).format(Date(millis))
}

fun parseItalianDate(value: String): Long? {
    val raw = value.trim()
    if (raw.isBlank()) return null
    val digits = raw.filter(Char::isDigit)
    val clean = if (raw.all(Char::isDigit) && digits.length == 8) {
        "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4, 8)}"
    } else raw
    return runCatching {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).apply { isLenient = false }
        val position = ParsePosition(0)
        val parsed = formatter.parse(clean, position)
        parsed?.takeIf { position.index == clean.length && position.errorIndex < 0 }?.time
    }.getOrNull()
}

class PantryRepository(
    val context: Context,
    val db: AppDatabase = AppDatabase.get(context)
) {
    private val lotDao = db.foodLotDao()
    private val recipeDao = db.recipeDao()
    private val shoppingDao = db.shoppingDao()
    private val eventDao = db.foodEventDao()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun ensureSeeds() = withContext(Dispatchers.IO) {
        SeedRecipeLoader.seedIfNeeded(context, db)
    }

    fun parseIngredients(json: String): List<IngredientAmount> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            QuantityMath.normalizeIngredient(
                IngredientAmount(
                    name = item.optString("name", "Ingrediente"),
                    quantity = if (item.isNull("quantity")) null else item.optDouble("quantity"),
                    unit = item.optString("unit", ""),
                    note = item.optString("note", "").takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    fun ingredientsJson(ingredients: List<IngredientAmount>): String =
        JSONArray().apply {
            ingredients.forEach { ingredient ->
                put(
                    JSONObject().apply {
                        put("name", ingredient.name.trim())
                        if (ingredient.quantity == null) {
                            put("quantity", JSONObject.NULL)
                        } else {
                            put("quantity", ingredient.quantity)
                        }
                        put("unit", ingredient.unit)
                        put("note", ingredient.note ?: "")
                    }
                )
            }
        }.toString()

    suspend fun addLot(item: FoodLotEntity): Long = db.withTransaction {
        require(item.name.isNotBlank()) { "Inserisci il nome dell'alimento." }
        require(item.quantity >= 0.0 && item.quantity.isFinite()) {
            "La quantità deve essere zero o positiva."
        }
        val clean = item.copy(
            name = item.name.trim(),
            brand = item.brand.trim(),
            barcode = item.barcode.trim()
        )
        val id = lotDao.insert(clean)
        eventDao.insert(
            FoodEventEntity(
                foodName = clean.name,
                type = "purchase",
                quantity = clean.quantity,
                unit = clean.unit
            )
        )
        id
    }

    suspend fun updateLot(item: FoodLotEntity) {
        require(item.name.isNotBlank()) { "Inserisci il nome dell'alimento." }
        require(item.quantity >= 0.0 && item.quantity.isFinite()) {
            "La quantità deve essere zero o positiva."
        }
        lotDao.update(item.copy(name = item.name.trim()))
    }

    suspend fun removeLot(id: Long, reason: String) {
        val item = lotDao.getById(id) ?: return
        val normalizedReason = reason.trim().lowercase(Locale.ROOT)
        val eventType = when (normalizedReason) {
            "used" -> "consume"
            "discarded", "expired" -> "waste"
            "donated" -> "donated"
            else -> "removed"
        }
        val details = when (normalizedReason) {
            "used" -> "Rimosso perché utilizzato"
            "discarded" -> "Buttato"
            "expired" -> "Eliminato perché scaduto"
            "donated" -> "Regalato o ceduto"
            else -> "Rimozione manuale"
        }
        db.withTransaction {
            lotDao.delete(item)
            eventDao.insert(
                FoodEventEntity(
                    foodName = item.name,
                    type = eventType,
                    quantity = item.quantity,
                    unit = item.unit,
                    details = details,
                ),
            )
        }
    }

    suspend fun deleteLot(id: Long) = removeLot(id, "other")

    suspend fun adjustLot(id: Long, delta: Double) = db.withTransaction {
        val item = lotDao.getById(id) ?: return@withTransaction
        val next = max(0.0, item.quantity + delta)
        val changed = next - item.quantity
        if (changed == 0.0) return@withTransaction

        lotDao.update(item.copy(quantity = next))
        eventDao.insert(
            FoodEventEntity(
                foodName = item.name,
                type = if (changed < 0) "consume" else "restock",
                quantity = abs(changed),
                unit = item.unit,
                details = if (changed < 0) "Consumo manuale" else "Aggiunta manuale"
            )
        )
    }

    suspend fun expiringLots(days: Int = 3): List<FoodLotEntity> {
        val today = todayLocalDate()
        return lotDao.getAll().filter { lot ->
            lot.quantity > 0.0 && isDueOrExpiringDate(lot.expiryDateMillis, days, today)
        }
    }

    suspend fun addOrMergeShopping(item: ShoppingItemEntity): Long = db.withTransaction {
        val cleanName = item.name.trim()
        require(cleanName.isNotBlank()) { "Inserisci il nome del prodotto." }
        val cleanQty = if (item.quantity.isFinite() && item.quantity > 0.0) item.quantity else 1.0
        val cleanUnit = item.unit.trim().ifBlank { "pz" }

        val existing = shoppingDao.getOpen().firstOrNull {
            it.name.equals(cleanName, ignoreCase = true) &&
                    it.unit.equals(cleanUnit, ignoreCase = true)
        }

        if (existing != null) {
            val sameRecipe = item.sourceRecipeId != null && existing.sourceRecipeId == item.sourceRecipeId
            val nextQuantity = if (sameRecipe) max(existing.quantity, cleanQty) else existing.quantity + cleanQty
            val updated = existing.copy(
                quantity = nextQuantity,
                notes = if (item.notes.isNotBlank() && existing.notes.isBlank()) item.notes else existing.notes,
                sourceRecipeId = existing.sourceRecipeId ?: item.sourceRecipeId,
            )
            if (updated != existing) shoppingDao.update(updated)
            existing.id
        } else {
            shoppingDao.insert(
                item.copy(
                    name = cleanName,
                    quantity = cleanQty,
                    unit = cleanUnit,
                    ignored = false
                )
            )
        }
    }

    suspend fun toggleShopping(id: Long, checked: Boolean) {
        val existing = shoppingDao.getById(id) ?: return
        shoppingDao.update(existing.copy(isChecked = checked))
    }

    suspend fun deleteShopping(id: Long) {
        val existing = shoppingDao.getById(id) ?: return
        shoppingDao.delete(existing)
    }

    suspend fun ignoreShoppingSuggestion(id: Long) = db.withTransaction {
        val existing = shoppingDao.getById(id) ?: return@withTransaction
        shoppingDao.update(existing.copy(ignored = true))
        eventDao.insert(
            FoodEventEntity(
                foodName = existing.name,
                type = "ignored_suggestion",
                quantity = existing.quantity,
                unit = existing.unit,
                details = "Suggerimento spesa ignorato"
            )
        )
    }

    suspend fun addRecipeNeedsToShopping(requests: List<Pair<Long, Int>>): RecipeShoppingUpdateResult = db.withTransaction {
        val pantryLots = lotDao.getAll().map {
            LotAmount(
                name = it.name,
                quantity = it.quantity,
                unit = it.unit,
                expiryEpochMillis = it.expiryDateMillis,
            )
        }
        var itemsAddedOrUpdated = 0
        var ingredientsAlreadyAvailable = 0
        var ingredientsWithoutQuantity = 0
        var recipesNotFound = 0

        for ((recipeId, requestedServings) in requests) {
            val recipe = recipeDao.getById(recipeId)
            if (recipe == null) {
                recipesNotFound++
                continue
            }
            val baseServings = recipe.servings.coerceAtLeast(1)
            val servings = requestedServings.coerceAtLeast(1)
            val ingredients = parseIngredients(recipe.ingredientsJson)

            for (ingredient in ingredients) {
                val sourceQuantity = ingredient.quantity
                if (sourceQuantity == null || !sourceQuantity.isFinite() || sourceQuantity <= 0.0) {
                    ingredientsWithoutQuantity++
                    continue
                }
                val scaledQuantity = QuantityMath.scale(sourceQuantity, baseServings, servings)
                if (scaledQuantity == null) {
                    ingredientsWithoutQuantity++
                    continue
                }
                val neededQty = scaledQuantity
                if (neededQty <= 0.0) continue

                val availableQty = QuantityMath.availableQuantity(ingredient, pantryLots)
                val missing = QuantityMath.clean(neededQty - availableQty)
                if (missing > 0.0001) {
                    val rangeNote = if (QuantityMath.isRangeNote(ingredient.note)) {
                        "Quantità indicativa in ricetta: ${QuantityMath.format(neededQty, ingredient.unit, ingredient.note)}"
                    } else {
                        ingredient.note.orEmpty().takeUnless { QuantityMath.isRangeNote(it) }.orEmpty()
                    }
                    addOrMergeShopping(
                        ShoppingItemEntity(
                            name = ingredient.name.trim(),
                            quantity = missing,
                            unit = ingredient.unit.ifBlank { "pz" },
                            category = recipe.category.ifBlank { "Altro" },
                            notes = rangeNote,
                            isManual = false,
                            sourceRecipeId = recipe.id,
                        ),
                    )
                    itemsAddedOrUpdated++
                } else {
                    ingredientsAlreadyAvailable++
                }
            }
        }

        RecipeShoppingUpdateResult(
            itemsAddedOrUpdated = itemsAddedOrUpdated,
            ingredientsAlreadyAvailable = ingredientsAlreadyAvailable,
            ingredientsWithoutQuantity = ingredientsWithoutQuantity,
            recipesNotFound = recipesNotFound,
        )
    }

    suspend fun upsertRecipe(item: RecipeEntity): Long {
        require(item.name.isNotBlank()) { "Inserisci il titolo della ricetta." }
        val now = System.currentTimeMillis()
        val clean = item.copy(
            name = item.name.trim(),
            description = item.description.trim(),
            instructions = item.instructions.trim(),
            createdAtMillis = if (item.id == 0L) now else item.createdAtMillis,
            updatedAtMillis = now
        )
        return recipeDao.insert(clean)
    }

    suspend fun duplicateRecipe(id: Long): Long? {
        val existing = recipeDao.getById(id) ?: return null
        val now = System.currentTimeMillis()
        return recipeDao.insert(
            existing.copy(
                id = 0,
                name = "${existing.name} (copia)",
                isSeed = false,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun deleteRecipe(id: Long) {
        val existing = recipeDao.getById(id) ?: return
        recipeDao.delete(existing)
    }

    suspend fun setFavorite(id: Long, value: Boolean) {
        val existing = recipeDao.getById(id) ?: return
        recipeDao.update(
            existing.copy(
                favorite = value,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun logIgnoredRecipeSuggestion(id: Long) {
        val recipe = recipeDao.getById(id)
        eventDao.insert(
            FoodEventEntity(
                foodName = recipe?.name ?: "Ricetta",
                type = "ignored_suggestion",
                recipeId = id,
                details = "Suggerimento ricetta ignorato"
            )
        )
    }

    suspend fun consumeRecipe(id: Long, servings: Int): List<String> = db.withTransaction {
        val recipe = recipeDao.getById(id)
            ?: throw IllegalArgumentException("Ricetta non trovata.")
        val baseServings = recipe.servings.coerceAtLeast(1)
        val scale = servings.coerceAtLeast(1).toDouble() / baseServings.toDouble()
        val ingredients = parseIngredients(recipe.ingredientsJson)
        val allLots = lotDao.getAll().toMutableList()
        val shortages = mutableListOf<String>()

        for (ingredient in ingredients) {
            val required = (ingredient.quantity ?: continue) * scale
            if (required <= 0.0) continue

            var remaining = required
            val matchingIndices = allLots.indices.filter { idx ->
                val lot = allLots[idx]
                lot.quantity > 0.0 &&
                        lot.name.equals(ingredient.name.trim(), ignoreCase = true)
            }

            for (idx in matchingIndices) {
                if (remaining <= 0.0) break
                val lot = allLots[idx]
                val used = minOf(lot.quantity, remaining)
                if (used > 0.0) {
                    val updatedLot = lot.copy(quantity = max(0.0, lot.quantity - used))
                    allLots[idx] = updatedLot
                    lotDao.update(updatedLot)
                    eventDao.insert(
                        FoodEventEntity(
                            foodName = lot.name,
                            type = "consume",
                            quantity = used,
                            unit = lot.unit,
                            recipeId = recipe.id,
                            details = "Usato per ${recipe.name}"
                        )
                    )
                    remaining -= used
                }
            }

            if (remaining > 0.01) {
                shortages.add(ingredient.name)
            }
        }

        eventDao.insert(
            FoodEventEntity(
                foodName = recipe.name,
                type = "cooked",
                quantity = servings.toDouble(),
                unit = "porzioni",
                recipeId = recipe.id,
                details = "Preparata ricetta"
            )
        )

        shortages
    }

    suspend fun registerPurchase(
        itemId: Long,
        quantity: Double,
        unit: String,
        location: String,
        expiry: Long?
    ) = db.withTransaction {
        val shoppingItem = shoppingDao.getById(itemId)
            ?: throw IllegalArgumentException("Elemento della spesa non trovato.")
        val cleanQty = if (quantity.isFinite() && quantity > 0.0) quantity else shoppingItem.quantity.coerceAtLeast(1.0)
        val cleanUnit = unit.trim().ifBlank { shoppingItem.unit.ifBlank { "pz" } }
        val cleanLocation = location.trim().ifBlank { "Dispensa" }
        val now = System.currentTimeMillis()

        lotDao.insert(
            FoodLotEntity(
                name = shoppingItem.name.trim(),
                category = shoppingItem.category.ifBlank { "Altro" },
                quantity = cleanQty,
                unit = cleanUnit,
                location = cleanLocation,
                purchaseDateMillis = now,
                expiryDateMillis = expiry,
                notes = shoppingItem.notes
            )
        )

        shoppingDao.delete(shoppingItem)

        eventDao.insert(
            FoodEventEntity(
                foodName = shoppingItem.name.trim(),
                type = "purchase",
                quantity = cleanQty,
                unit = cleanUnit,
                details = "Acquistato dalla lista della spesa"
            )
        )
    }

    suspend fun generateRestockSuggestions() = db.withTransaction {
        val lots = lotDao.getAll()
        val openShopping = shoppingDao.getOpen()

        lots.filter { it.minQuantity > 0.0 && it.quantity < it.minQuantity }
            .forEach { lot ->
                val alreadyListed = openShopping.any {
                    it.name.equals(lot.name, ignoreCase = true)
                }
                if (!alreadyListed) {
                    val needed = max(1.0, lot.minQuantity - lot.quantity)
                    addOrMergeShopping(
                        ShoppingItemEntity(
                            name = lot.name,
                            quantity = needed,
                            unit = lot.unit,
                            category = lot.category,
                            notes = "Scorta sotto soglia minima",
                            isManual = false
                        )
                    )
                }
            }

        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L
        val recentEvents = eventDao.since(thirtyDaysAgo)
        val ignoredNames = recentEvents
            .filter { it.type == "ignored_suggestion" }
            .map { it.foodName.lowercase(Locale.ROOT) }
            .toSet()

        recentEvents
            .filter { it.type == "consume" || it.type == "purchase" }
            .groupBy { it.foodName.trim().lowercase(Locale.ROOT) }
            .filter { (key, events) -> key.isNotBlank() && key !in ignoredNames && events.size >= 2 }
            .forEach { (_, events) ->
                val sample = events.first()
                val available = lots
                    .filter { it.name.equals(sample.foodName, ignoreCase = true) }
                    .sumOf { it.quantity }
                val alreadyInShopping = shoppingDao.getOpen().any {
                    it.name.equals(sample.foodName, ignoreCase = true)
                }
                if (available <= 0.0 && !alreadyInShopping) {
                    addOrMergeShopping(
                        ShoppingItemEntity(
                            name = sample.foodName,
                            quantity = 1.0,
                            unit = sample.unit.ifBlank { "pz" },
                            notes = "Suggerito dalle tue abitudini",
                            isManual = false
                        )
                    )
                }
            }
    }

    suspend fun habitSuggestions(): List<HabitSuggestion> = withContext(Dispatchers.IO) {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L
        val events = eventDao.since(thirtyDaysAgo)
        val lots = lotDao.getAll()
        val openShopping = shoppingDao.getOpen()

        val ignored = events
            .filter { it.type == "ignored_suggestion" }
            .map { it.foodName.trim().lowercase(Locale.ROOT) }
            .toSet()

        val suggestions = mutableListOf<HabitSuggestion>()

        lots.filter {
            it.minQuantity > 0.0 &&
                    it.quantity < it.minQuantity &&
                    it.name.trim().lowercase(Locale.ROOT) !in ignored &&
                    openShopping.none { s -> s.name.equals(it.name, ignoreCase = true) }
        }.forEach { lot ->
            suggestions.add(
                HabitSuggestion(
                    name = lot.name,
                    reason = "Scorta bassa (${lot.quantity} ${lot.unit} su min ${lot.minQuantity} ${lot.unit})",
                    category = lot.category,
                    unit = lot.unit
                )
            )
        }

        events.filter { it.type == "consume" || it.type == "purchase" }
            .groupBy { it.foodName.trim().lowercase(Locale.ROOT) }
            .filter { (nameKey, list) ->
                nameKey.isNotBlank() &&
                        nameKey !in ignored &&
                        list.size >= 2 &&
                        lots.filter { it.name.equals(list.first().foodName, ignoreCase = true) }.sumOf { it.quantity } <= 0.0 &&
                        openShopping.none { it.name.equals(list.first().foodName, ignoreCase = true) } &&
                        suggestions.none { it.name.equals(list.first().foodName, ignoreCase = true) }
            }
            .forEach { (_, list) ->
                val sample = list.first()
                suggestions.add(
                    HabitSuggestion(
                        name = sample.foodName,
                        reason = "Usato o acquistato ${list.size} volte nell'ultimo mese",
                        unit = sample.unit.ifBlank { "pz" }
                    )
                )
            }

        suggestions
    }

    suspend fun exportBackup(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val extraPreferences = context.getSharedPreferences("zest_extra_prefs", Context.MODE_PRIVATE)
            .all
            .filterKeys { !ProBackupPolicy.isSensitiveKey(it) }
        val payload = BackupPayload(
            lots = lotDao.getAll(),
            recipes = recipeDao.getAll(),
            shopping = shoppingDao.getAll(),
            events = eventDao.since(0L),
            barcodeCache = db.barcodeCacheDao().getAll(),
            appSettings = SettingsRepository(context).settings.first(),
            // This includes the local nutrition profile (birth date, weight, height, sex,
            // activity and goals) and other user preferences. Pro entitlement is excluded:
            // purchase state must always be recovered from Google Play, never from a backup.
            extraPreferencesJson = gson.toJson(extraPreferences),
        )
        val json = gson.toJson(payload)
        outputStream.writer(Charsets.UTF_8).use { writer -> writer.write(json) }
    }

    suspend fun importBackup(inputStream: InputStream): ImportSummary = withContext(Dispatchers.IO) {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val payload = gson.fromJson(text, BackupPayload::class.java)
            ?: throw IllegalArgumentException("File di backup non valido.")

        val lotsToInsert = payload.lots.orEmpty()
        val recipesToInsert = payload.recipes.orEmpty()
        val shoppingToInsert = payload.shopping.orEmpty()
        val eventsToInsert = payload.events.orEmpty()
        val barcodeCacheToInsert = payload.barcodeCache.orEmpty()

        db.withTransaction {
            if (lotsToInsert.isNotEmpty()) lotDao.insertAll(lotsToInsert)
            if (recipesToInsert.isNotEmpty()) recipeDao.insertAll(recipesToInsert)
            if (shoppingToInsert.isNotEmpty()) shoppingDao.insertAll(shoppingToInsert)
            if (eventsToInsert.isNotEmpty()) eventDao.insertAll(eventsToInsert)
            if (barcodeCacheToInsert.isNotEmpty()) db.barcodeCacheDao().insertAll(barcodeCacheToInsert)
        }

        payload.appSettings?.let { SettingsRepository(context).restoreFromBackup(it) }
        payload.extraPreferencesJson?.let(::restoreExtraPreferences)

        ImportSummary(
            lots = lotsToInsert.size,
            recipes = recipesToInsert.size,
            shopping = shoppingToInsert.size,
            events = eventsToInsert.size
        )
    }

    private fun restoreExtraPreferences(json: String) {
        val objectJson = JSONObject(json)
        val editor = context.getSharedPreferences("zest_extra_prefs", Context.MODE_PRIVATE).edit().clear()
        val keys = objectJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (ProBackupPolicy.isSensitiveKey(key)) continue
            val value = objectJson.opt(key)
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Double -> {
                    if (value.isFinite()) {
                        val asInt = value.toInt()
                        if (asInt.toDouble() == value) editor.putInt(key, asInt)
                        else editor.putFloat(key, value.toFloat())
                    }
                }
            }
        }
        check(editor.commit()) { "Impossibile ripristinare le preferenze personali dal backup." }
    }


    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        db.withTransaction {
            lotDao.clear()
            shoppingDao.clear()
            eventDao.clear()
            db.barcodeCacheDao().clear()
            recipeDao.clearPersonal()
            recipeDao.resetSeedPreferences(System.currentTimeMillis())
        }
        ensureSeeds()
    }
}