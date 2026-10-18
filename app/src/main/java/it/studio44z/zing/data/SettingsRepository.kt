package it.studio44z.zing.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dispensa_settings")

data class AppSettings(
    val darkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val daysBeforeExpiry: Int = 3,
    val householdSize: Int = 2,
    val dietaryPreferences: String = "",
    val excludedIngredients: String = "",
    val language: String = "system",
    val defaultLocation: String = "Dispensa",
    val defaultUnit: String = "pz",
    val defaultMinQuantity: Double = 0.0,
    val openedDaysLimit: Int = 4,
    val showExpiredInHome: Boolean = true,
    val scannerVibration: Boolean = true,
    val scannerQrEnabled: Boolean = true,
    val autoLookupBarcode: Boolean = true,
    val recipePriority: String = "expiry",
    val minPurchasesForHabit: Int = 2,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val EXPIRY_DAYS = intPreferencesKey("days_before_expiry")
        val HOUSEHOLD_SIZE = intPreferencesKey("household_size")
        val DIETARY = stringPreferencesKey("dietary_preferences")
        val EXCLUDED = stringPreferencesKey("excluded_ingredients")
        val LANGUAGE = stringPreferencesKey("app_language")
        val DEFAULT_LOCATION = stringPreferencesKey("default_location")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val DEFAULT_MIN_QTY = doublePreferencesKey("default_min_qty")
        val OPENED_DAYS_LIMIT = intPreferencesKey("opened_days_limit")
        val SHOW_EXPIRED_HOME = booleanPreferencesKey("show_expired_in_home")
        val SCANNER_VIBRATION = booleanPreferencesKey("scanner_vibration")
        val SCANNER_QR = booleanPreferencesKey("scanner_qr_enabled")
        val AUTO_LOOKUP_BARCODE = booleanPreferencesKey("auto_lookup_barcode")
        val RECIPE_PRIORITY = stringPreferencesKey("recipe_priority")
        val MIN_PURCHASES_HABIT = intPreferencesKey("min_purchases_habit")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            darkTheme = prefs[Keys.DARK_THEME] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            daysBeforeExpiry = prefs[Keys.EXPIRY_DAYS] ?: 3,
            householdSize = prefs[Keys.HOUSEHOLD_SIZE] ?: 2,
            dietaryPreferences = prefs[Keys.DIETARY] ?: "",
            excludedIngredients = prefs[Keys.EXCLUDED] ?: "",
            language = prefs[Keys.LANGUAGE] ?: "system",
            defaultLocation = prefs[Keys.DEFAULT_LOCATION] ?: "Dispensa",
            defaultUnit = prefs[Keys.DEFAULT_UNIT] ?: "pz",
            defaultMinQuantity = prefs[Keys.DEFAULT_MIN_QTY] ?: 0.0,
            openedDaysLimit = prefs[Keys.OPENED_DAYS_LIMIT] ?: 4,
            showExpiredInHome = prefs[Keys.SHOW_EXPIRED_HOME] ?: true,
            scannerVibration = prefs[Keys.SCANNER_VIBRATION] ?: true,
            scannerQrEnabled = prefs[Keys.SCANNER_QR] ?: true,
            autoLookupBarcode = prefs[Keys.AUTO_LOOKUP_BARCODE] ?: true,
            recipePriority = prefs[Keys.RECIPE_PRIORITY] ?: "expiry",
            minPurchasesForHabit = prefs[Keys.MIN_PURCHASES_HABIT] ?: 2,
        )
    }

    suspend fun setDarkTheme(value: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = value }
    }

    suspend fun setNotifications(value: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = value }
    }

    suspend fun setExpiryDays(value: Int) {
        context.dataStore.edit { it[Keys.EXPIRY_DAYS] = value.coerceIn(0, 30) }
    }

    suspend fun setHouseholdSize(value: Int) {
        context.dataStore.edit { it[Keys.HOUSEHOLD_SIZE] = value.coerceIn(1, 20) }
    }

    suspend fun setDietaryPreferences(value: String) {
        context.dataStore.edit { it[Keys.DIETARY] = value.trim() }
    }

    suspend fun setExcludedIngredients(value: String) {
        context.dataStore.edit { it[Keys.EXCLUDED] = value.trim() }
    }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = value }
    }

    suspend fun setDefaultLocation(value: String) {
        context.dataStore.edit { it[Keys.DEFAULT_LOCATION] = value }
    }

    suspend fun setDefaultUnit(value: String) {
        context.dataStore.edit { it[Keys.DEFAULT_UNIT] = value }
    }

    suspend fun setDefaultMinQuantity(value: Double) {
        context.dataStore.edit { it[Keys.DEFAULT_MIN_QTY] = value.coerceAtLeast(0.0) }
    }

    suspend fun setOpenedDaysLimit(value: Int) {
        context.dataStore.edit { it[Keys.OPENED_DAYS_LIMIT] = value.coerceIn(0, 30) }
    }

    suspend fun setShowExpiredInHome(value: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_EXPIRED_HOME] = value }
    }

    suspend fun setScannerVibration(value: Boolean) {
        context.dataStore.edit { it[Keys.SCANNER_VIBRATION] = value }
    }

    suspend fun setScannerQrEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.SCANNER_QR] = value }
    }

    suspend fun setAutoLookupBarcode(value: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_LOOKUP_BARCODE] = value }
    }

    suspend fun setRecipePriority(value: String) {
        context.dataStore.edit { it[Keys.RECIPE_PRIORITY] = value }
    }

    suspend fun setMinPurchasesForHabit(value: Int) {
        context.dataStore.edit { it[Keys.MIN_PURCHASES_HABIT] = value.coerceIn(1, 10) }
    }

    suspend fun restoreFromBackup(snapshot: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = snapshot.darkTheme
            prefs[Keys.NOTIFICATIONS] = snapshot.notificationsEnabled
            prefs[Keys.EXPIRY_DAYS] = snapshot.daysBeforeExpiry.coerceIn(0, 30)
            prefs[Keys.HOUSEHOLD_SIZE] = snapshot.householdSize.coerceIn(1, 20)
            prefs[Keys.DIETARY] = snapshot.dietaryPreferences
            prefs[Keys.EXCLUDED] = snapshot.excludedIngredients
            prefs[Keys.LANGUAGE] = snapshot.language
            prefs[Keys.DEFAULT_LOCATION] = snapshot.defaultLocation
            prefs[Keys.DEFAULT_UNIT] = snapshot.defaultUnit
            prefs[Keys.DEFAULT_MIN_QTY] = snapshot.defaultMinQuantity.coerceAtLeast(0.0)
            prefs[Keys.OPENED_DAYS_LIMIT] = snapshot.openedDaysLimit.coerceIn(0, 30)
            prefs[Keys.SHOW_EXPIRED_HOME] = snapshot.showExpiredInHome
            prefs[Keys.SCANNER_VIBRATION] = snapshot.scannerVibration
            prefs[Keys.SCANNER_QR] = snapshot.scannerQrEnabled
            prefs[Keys.AUTO_LOOKUP_BARCODE] = snapshot.autoLookupBarcode
            prefs[Keys.RECIPE_PRIORITY] = snapshot.recipePriority
            prefs[Keys.MIN_PURCHASES_HABIT] = snapshot.minPurchasesForHabit.coerceIn(1, 10)
        }
    }

    suspend fun reset() {
        context.dataStore.edit { it.clear() }
    }
}