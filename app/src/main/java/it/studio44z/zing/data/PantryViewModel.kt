package it.studio44z.zing.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import it.studio44z.zing.domain.IngredientAmount
import it.studio44z.zing.notifications.ExpiryReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PantryViewModel(application: Application) : AndroidViewModel(application) {
    val repository = PantryRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val barcodeRepository = OpenFoodFactsRepository()

    val lots: StateFlow<List<FoodLotEntity>> = repository.db.foodLotDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recipes: StateFlow<List<RecipeEntity>> = repository.db.recipeDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val shopping: StateFlow<List<ShoppingItemEntity>> = repository.db.shoppingDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val events: StateFlow<List<FoodEventEntity>> = repository.db.foodEventDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _habitSuggestions = MutableStateFlow<List<HabitSuggestion>>(emptyList())
    val habitSuggestions: StateFlow<List<HabitSuggestion>> = _habitSuggestions.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.ensureSeeds() }.onFailure {
                _message.value = "Impossibile caricare la libreria ricette: ${it.message.orEmpty()}"
            }
            val initialSettings = settingsRepository.settings.first()
            if (initialSettings.notificationsEnabled) scheduleNotifications()
            else WorkManager.getInstance(getApplication()).cancelUniqueWork(WORK_NAME)
            refreshHabits()
        }
    }

    private fun perform(successMessage: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            try {
                block()
                if (successMessage != null) _message.value = successMessage
                refreshHabits()
            } catch (e: Exception) {
                _message.value = e.message?.takeIf { it.isNotBlank() } ?: "Operazione non riuscita."
            } finally {
                _busy.value = false
            }
        }
    }

    fun addLot(item: FoodLotEntity) = perform("✓") { repository.addLot(item) }
    fun updateLot(item: FoodLotEntity) = perform("✓") { repository.updateLot(item) }
    fun removeLot(id: Long, reason: String) = perform("✓") { repository.removeLot(id, reason) }
    fun deleteLot(id: Long) = removeLot(id, "other")
    fun adjustLot(id: Long, delta: Double) = perform { repository.adjustLot(id, delta) }
    fun addShoppingItem(item: ShoppingItemEntity) = perform("✓") { repository.addOrMergeShopping(item) }
    fun toggleShopping(id: Long, checked: Boolean) = perform { repository.toggleShopping(id, checked) }
    fun deleteShopping(id: Long) = perform { repository.deleteShopping(id) }
    fun ignoreShopping(id: Long) = perform { repository.ignoreShoppingSuggestion(id) }
    fun addRecipeNeeds(ids: List<Pair<Long, Int>>) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val result = repository.addRecipeNeedsToShopping(ids)
                _message.value = when {
                    result.recipesNotFound > 0 && result.itemsAddedOrUpdated == 0 -> "Ricetta non trovata: aggiorna la schermata e riprova."
                    result.itemsAddedOrUpdated > 0 -> "Lista della spesa aggiornata: ${result.itemsAddedOrUpdated} ingredienti mancanti aggiunti o aggiornati."
                    result.ingredientsAlreadyAvailable > 0 -> "Non ci sono ingredienti mancanti da aggiungere: le quantità calcolabili risultano già presenti in dispensa."
                    result.ingredientsWithoutQuantity > 0 -> "Nessun ingrediente quantificabile da aggiungere. Gli ingredienti indicati q.b. sono stati saltati perché non hanno una quantità calcolabile."
                    else -> "Non ci sono ingredienti da aggiungere alla lista della spesa."
                }
                refreshHabits()
            } catch (e: Exception) {
                _message.value = e.message?.takeIf { it.isNotBlank() } ?: "Impossibile aggiornare la lista della spesa."
            } finally {
                _busy.value = false
            }
        }
    }
    fun saveRecipe(item: RecipeEntity) = perform("✓") { repository.upsertRecipe(item) }
    fun duplicateRecipe(id: Long) = perform("✓") { repository.duplicateRecipe(id) }
    fun deleteRecipe(id: Long) = perform("✓") { repository.deleteRecipe(id) }
    fun setFavorite(id: Long, value: Boolean) = perform { repository.setFavorite(id, value) }
    fun ignoreRecipeSuggestion(id: Long) = perform { repository.logIgnoredRecipeSuggestion(id) }

    fun clearCheckedShopping() = perform("✓") {
        val checkedItems = shopping.value.filter { it.isChecked }
        checkedItems.forEach { repository.deleteShopping(it.id) }
    }

    fun consumeRecipe(id: Long, servings: Int, onResult: (List<String>) -> Unit = {}) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val shortages = repository.consumeRecipe(id, servings)
                _message.value = if (shortages.isEmpty()) "✓" else "Mancanze: ${shortages.joinToString()}"
                onResult(shortages)
                refreshHabits()
            } catch (e: Exception) {
                _message.value = e.message ?: "Errore"
            } finally {
                _busy.value = false
            }
        }
    }

    fun registerPurchase(itemId: Long, quantity: Double, unit: String, location: String, expiry: Long?) =
        perform("✓") {
            repository.registerPurchase(itemId, quantity, unit, location, expiry)
        }

    fun lookupBarcode(barcode: String, forceRemote: Boolean = false, callback: (ProductLookupResult) -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            try {
                val code = barcode.trim()
                val currentLang = settings.value.language
                val cached = if (!forceRemote) repository.db.barcodeCacheDao().findByBarcode(code) else null
                if (cached != null && cached.name.isNotBlank() &&
                    (cached.imageUrl != null || cached.energyKcal100g != null || cached.nutritionGrade != null) &&
                    System.currentTimeMillis() - cached.fetchedAtMillis < 30L * 24 * 60 * 60 * 1000
                ) {
                    callback(ProductLookupResult.Found(cached.toRemoteProduct()))
                    return@launch
                }
                val inPantry = if (!forceRemote) repository.db.foodLotDao().findByBarcode(code) else null
                when (val result = barcodeRepository.lookup(code, currentLang)) {
                    is ProductLookupResult.Found -> {
                        val p = result.product
                        repository.db.barcodeCacheDao().upsert(
                            BarcodeCacheEntity(
                                barcode = p.barcode, name = p.name, brand = p.brand, category = p.category,
                                quantityLabel = p.quantityLabel, imageUrl = p.imageUrl,
                                nutritionGrade = p.nutritionGrade, novaGroup = p.novaGroup,
                                energyKcal100g = p.energyKcal100g, sugarsG100g = p.sugarsG100g,
                                saturatedFatG100g = p.saturatedFatG100g, saltG100g = p.saltG100g,
                                fiberG100g = p.fiberG100g, proteinG100g = p.proteinG100g,
                                ingredientsText = p.ingredientsText,
                            )
                        )
                        callback(result)
                    }
                    else -> {
                        if (inPantry != null && inPantry.name.isNotBlank()) {
                            val staleImage = cached?.imageUrl
                            callback(ProductLookupResult.Found(RemoteProduct(
                                barcode = inPantry.barcode, name = inPantry.name, brand = inPantry.brand,
                                category = inPantry.category, imageUrl = staleImage,
                            )))
                        } else if (cached != null && cached.name.isNotBlank()) {
                            callback(ProductLookupResult.Found(cached.toRemoteProduct()))
                        } else callback(result)
                    }
                }
            } catch (e: Exception) {
                callback(
                    ProductLookupResult.Unavailable(
                        e.message?.takeIf { it.isNotBlank() } ?: "Ricerca non disponibile."
                    )
                )
            } finally {
                _busy.value = false
            }
        }
    }

    fun setDarkTheme(value: Boolean) = perform { settingsRepository.setDarkTheme(value) }
    fun setExpiryDays(value: Int) = perform { settingsRepository.setExpiryDays(value) }
    fun setHouseholdSize(value: Int) = perform { settingsRepository.setHouseholdSize(value) }
    fun setDietaryPreferences(value: String) = perform { settingsRepository.setDietaryPreferences(value) }
    fun setExcludedIngredients(value: String) = perform { settingsRepository.setExcludedIngredients(value) }
    fun setLanguage(value: String) = perform { settingsRepository.setLanguage(value) }
    fun setDefaultLocation(value: String) = perform { settingsRepository.setDefaultLocation(value) }
    fun setDefaultUnit(value: String) = perform { settingsRepository.setDefaultUnit(value) }
    fun setDefaultMinQuantity(value: Double) = perform { settingsRepository.setDefaultMinQuantity(value) }
    fun setOpenedDaysLimit(value: Int) = perform { settingsRepository.setOpenedDaysLimit(value) }
    fun setShowExpiredInHome(value: Boolean) = perform { settingsRepository.setShowExpiredInHome(value) }
    fun setScannerVibration(value: Boolean) = perform { settingsRepository.setScannerVibration(value) }
    fun setScannerQrEnabled(value: Boolean) = perform { settingsRepository.setScannerQrEnabled(value) }
    fun setAutoLookupBarcode(value: Boolean) = perform { settingsRepository.setAutoLookupBarcode(value) }
    fun setRecipePriority(value: String) = perform { settingsRepository.setRecipePriority(value) }
    fun setMinPurchasesForHabit(value: Int) = perform { settingsRepository.setMinPurchasesForHabit(value) }

    fun setNotifications(value: Boolean) = perform {
        settingsRepository.setNotifications(value)
        if (value) scheduleNotifications()
        else WorkManager.getInstance(getApplication()).cancelUniqueWork(WORK_NAME)
    }

    fun triggerTestNotification() {
        val request = OneTimeWorkRequestBuilder<ExpiryReminderWorker>().build()
        WorkManager.getInstance(getApplication()).enqueue(request)
        _message.value = "✓"
    }

    fun clearBarcodeCache() = perform("✓") {
        withContext(Dispatchers.IO) {
            runCatching {
                val db = repository.db.openHelper.writableDatabase
                db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                    while (cursor.moveToNext()) {
                        val table = cursor.getString(0) ?: continue
                        if (table.contains("barcode", ignoreCase = true)) {
                            db.execSQL("DELETE FROM `$table`")
                        }
                    }
                }
            }
        }
    }

    fun clearHistoryEvents() = perform("✓") {
        withContext(Dispatchers.IO) {
            runCatching {
                val db = repository.db.openHelper.writableDatabase
                db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                    while (cursor.moveToNext()) {
                        val table = cursor.getString(0) ?: continue
                        if (table.contains("event", ignoreCase = true)) {
                            db.execSQL("DELETE FROM `$table`")
                        }
                    }
                }
            }
        }
    }

    fun exportBackup(uri: Uri) = perform("✓") {
        val stream = getApplication<Application>().contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("Impossibile aprire il file di destinazione.")
        stream.use { repository.exportBackup(it) }
    }

    fun importBackup(uri: Uri) = perform(null) {
        val stream = getApplication<Application>().contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Impossibile leggere il file selezionato.")
        val summary = stream.use { repository.importBackup(it) }
        _message.value = "Backup ripristinato: ${summary.lots} alimenti, ${summary.recipes} ricette, ${summary.shopping} elementi spesa e preferenze personali."
    }

    fun deleteAllData() = perform("✓") {
        repository.deleteAllData()
        settingsRepository.reset()
        scheduleNotifications()
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun reportMessage(message: String) {
        _message.value = message
    }

    fun generateSmartShoppingList() = perform("✓") {
        repository.generateRestockSuggestions()
    }

    fun refreshHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.habitSuggestions() }.onSuccess { _habitSuggestions.value = it }
        }
    }

    private fun scheduleNotifications() {
        val request = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun parseIngredients(json: String): List<IngredientAmount> = repository.parseIngredients(json)

    companion object {
        private const val WORK_NAME = "dispensa-smart-expiry-reminder"
    }
}

private fun BarcodeCacheEntity.toRemoteProduct() = RemoteProduct(
    barcode = barcode, name = name, brand = brand, category = category,
    quantityLabel = quantityLabel, imageUrl = imageUrl, nutritionGrade = nutritionGrade,
    novaGroup = novaGroup, energyKcal100g = energyKcal100g, sugarsG100g = sugarsG100g,
    saturatedFatG100g = saturatedFatG100g, saltG100g = saltG100g, fiberG100g = fiberG100g,
    proteinG100g = proteinG100g, ingredientsText = ingredientsText,
)
