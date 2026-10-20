package it.studio44z.zing.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import it.studio44z.zing.billing.ZingProCodeRedemptionClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.studio44z.zing.ads.ZingAdsManager
import it.studio44z.zing.ads.findActivity
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import it.studio44z.zing.billing.GooglePlayBillingManager
import it.studio44z.zing.data.AppSettings
import it.studio44z.zing.data.FoodEventEntity
import it.studio44z.zing.data.FoodLotEntity
import it.studio44z.zing.data.HabitSuggestion
import it.studio44z.zing.data.PantryViewModel
import it.studio44z.zing.data.ProductLookupResult
import it.studio44z.zing.data.RemoteRecipeSuggestion
import it.studio44z.zing.data.RecipeDiscoveryRepository
import it.studio44z.zing.data.RecipeEntity
import it.studio44z.zing.data.ShoppingItemEntity
import it.studio44z.zing.data.formatItalianDate
import it.studio44z.zing.data.isDueOrExpiringDate
import it.studio44z.zing.data.isExpiredDate
import it.studio44z.zing.data.isExpiringDate
import it.studio44z.zing.data.parseItalianDate
import it.studio44z.zing.data.todayLocalDate
import it.studio44z.zing.domain.IngredientAmount
import it.studio44z.zing.domain.LotAmount
import it.studio44z.zing.domain.NutritionAdvisor
import it.studio44z.zing.domain.NutritionAssessment
import it.studio44z.zing.domain.NutritionFacts
import it.studio44z.zing.domain.NutritionProfile
import it.studio44z.zing.domain.QuantityMath
import it.studio44z.zing.domain.RecipeCoverage
import it.studio44z.zing.notifications.AlarmSoundController
import it.studio44z.zing.notifications.ExpiryReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

private object ItalianDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit).take(8)
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append('/')
                append(char)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safe = offset.coerceIn(0, digits.length)
                val separators = (if (safe > 2) 1 else 0) + (if (safe > 4) 1 else 0)
                return (safe + separators).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, formatted.length)
                return formatted.take(safe).count(Char::isDigit).coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

private fun dateInputDigits(value: String): String = value.filter(Char::isDigit).take(8)

private const val ZING_FEEDBACK_EMAIL = "studio44z.info@gmail.com"
private const val ZING_PRIVACY_POLICY_URL = "https://gaprj.github.io/zing-android/privacy-policy.html"

private enum class MainTab {
    HOME, PANTRY, RECIPES, SHOPPING, SETTINGS
}

private data class ZingExtraPrefs(
    val language: String = "system",
    val dateFormat: String = "dd/MM/yyyy",
    val defaultLocation: String = "Dispensa",
    val defaultUnit: String = "pz",
    val defaultMinStock: String = "0",
    val notifyDayBeforeExpiry: Boolean = true,
    val openedAlertDays: Int = 3,
    val showExpiredInHome: Boolean = true,
    val vibrateOnScan: Boolean = true,
    val autoLookupBarcode: Boolean = true,
    val recipePriority: String = "balanced",
    val showActionSnackbars: Boolean = false,
    val syncSystemDynamicIslandTimer: Boolean = true,
    val voluntarySupportAdsEnabled: Boolean = false,
    val supporterProUnlocked: Boolean = false,
    val supporterAccentTheme: String = "emerald",
    val keepScreenOnCooking: Boolean = false,
    val measurementSystem: String = "metric",
    val nutritionBirthDate: String = "",
    val nutritionAge: String = "",
    val nutritionWeightKg: String = "",
    val nutritionHeightCm: String = "",
    val nutritionSex: String = "unspecified",
    val nutritionTrainingDays: Int = 0,
    val nutritionFocus: String = "balanced",
)

private fun loadExtraPrefs(context: Context): ZingExtraPrefs {
    val sp = context.getSharedPreferences("zest_extra_prefs", Context.MODE_PRIVATE)
    return ZingExtraPrefs(
        language = sp.getString("language", "system") ?: "system",
        dateFormat = sp.getString("dateFormat", "dd/MM/yyyy") ?: "dd/MM/yyyy",
        defaultLocation = sp.getString("defaultLocation", "Dispensa") ?: "Dispensa",
        defaultUnit = sp.getString("defaultUnit", "pz") ?: "pz",
        defaultMinStock = sp.getString("defaultMinStock", "0") ?: "0",
        notifyDayBeforeExpiry = sp.getBoolean("notifyDayBeforeExpiry", true),
        openedAlertDays = sp.getInt("openedAlertDays", 3),
        showExpiredInHome = sp.getBoolean("showExpiredInHome", true),
        vibrateOnScan = sp.getBoolean("vibrateOnScan", true),
        autoLookupBarcode = sp.getBoolean("autoLookupBarcode", true),
        recipePriority = sp.getString("recipePriority", "balanced") ?: "balanced",
        showActionSnackbars = sp.getBoolean("showActionSnackbars", false),
        syncSystemDynamicIslandTimer = sp.getBoolean("syncSystemDynamicIslandTimer", true),
        voluntarySupportAdsEnabled = sp.getBoolean("voluntarySupportAdsEnabled", false),
        supporterProUnlocked = false,
        supporterAccentTheme = sp.getString("supporterAccentTheme", "emerald") ?: "emerald",
        keepScreenOnCooking = sp.getBoolean("keepScreenOnCooking", false),
        measurementSystem = sp.getString("measurementSystem", "metric") ?: "metric",
        nutritionBirthDate = sp.getString("nutritionBirthDate", "") ?: "",
        nutritionAge = sp.getString("nutritionAge", "") ?: "",
        nutritionWeightKg = sp.getString("nutritionWeightKg", "") ?: "",
        nutritionHeightCm = sp.getString("nutritionHeightCm", "") ?: "",
        nutritionSex = sp.getString("nutritionSex", "unspecified") ?: "unspecified",
        nutritionTrainingDays = sp.getInt("nutritionTrainingDays", 0).coerceIn(0, 7),
        nutritionFocus = sp.getString("nutritionFocus", "balanced") ?: "balanced",
    )
}

private fun saveExtraPrefs(context: Context, prefs: ZingExtraPrefs) {
    context.getSharedPreferences("zest_extra_prefs", Context.MODE_PRIVATE).edit()
        .putString("language", prefs.language)
        .putString("dateFormat", prefs.dateFormat)
        .putString("defaultLocation", prefs.defaultLocation)
        .putString("defaultUnit", prefs.defaultUnit)
        .putString("defaultMinStock", prefs.defaultMinStock)
        .putBoolean("notifyDayBeforeExpiry", prefs.notifyDayBeforeExpiry)
        .putInt("openedAlertDays", prefs.openedAlertDays)
        .putBoolean("showExpiredInHome", prefs.showExpiredInHome)
        .putBoolean("vibrateOnScan", prefs.vibrateOnScan)
        .putBoolean("autoLookupBarcode", prefs.autoLookupBarcode)
        .putString("recipePriority", prefs.recipePriority)
        .putBoolean("showActionSnackbars", prefs.showActionSnackbars)
        .putBoolean("syncSystemDynamicIslandTimer", prefs.syncSystemDynamicIslandTimer)
        .putBoolean("voluntarySupportAdsEnabled", prefs.voluntarySupportAdsEnabled)
        .putBoolean("supporterProUnlocked", false)
        .putBoolean("supporterProRedeemed", false)
        .putString("supporterAccentTheme", prefs.supporterAccentTheme)
        .putBoolean("keepScreenOnCooking", prefs.keepScreenOnCooking)
        .putString("measurementSystem", prefs.measurementSystem)
        .putString("nutritionBirthDate", prefs.nutritionBirthDate)
        .putString("nutritionAge", prefs.nutritionAge)
        .putString("nutritionWeightKg", prefs.nutritionWeightKg)
        .putString("nutritionHeightCm", prefs.nutritionHeightCm)
        .putString("nutritionSex", prefs.nutritionSex)
        .putInt("nutritionTrainingDays", prefs.nutritionTrainingDays.coerceIn(0, 7))
        .putString("nutritionFocus", prefs.nutritionFocus)
        .apply()
}

private fun parseBirthDateOrNull(value: String): LocalDate? =
    runCatching { LocalDate.parse(value) }.getOrNull()

private fun LocalDate.toUtcDatePickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun datePickerMillisToLocalDate(value: Long): LocalDate =
    Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate()

private fun formatBirthDate(date: LocalDate, langCode: String): String {
    val locale = Locale.forLanguageTag(langCode.ifBlank { Locale.getDefault().toLanguageTag() })
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(date)
}

private fun buildZingColorScheme(darkTheme: Boolean, accentKey: String): ColorScheme {
    data class AccentPalette(
        val lightPrimary: Color,
        val lightPrimaryContainer: Color,
        val lightOnPrimaryContainer: Color,
        val lightSecondary: Color,
        val lightSecondaryContainer: Color,
        val darkPrimary: Color,
        val darkPrimaryContainer: Color,
        val darkOnPrimaryContainer: Color,
    )

    val palette = when (accentKey) {
        "citrus" -> AccentPalette(
            lightPrimary = Color(0xFFB86B00),
            lightPrimaryContainer = Color(0xFFFFE0B2),
            lightOnPrimaryContainer = Color(0xFF3A2000),
            lightSecondary = Color(0xFF7A5900),
            lightSecondaryContainer = Color(0xFFFFECB3),
            darkPrimary = Color(0xFFFFB74D),
            darkPrimaryContainer = Color(0xFF5C3500),
            darkOnPrimaryContainer = Color(0xFFFFE0B2),
        )
        "berry" -> AccentPalette(
            lightPrimary = Color(0xFF9C2758),
            lightPrimaryContainer = Color(0xFFF8BBD0),
            lightOnPrimaryContainer = Color(0xFF3E001A),
            lightSecondary = Color(0xFF7B3E57),
            lightSecondaryContainer = Color(0xFFF4D3E0),
            darkPrimary = Color(0xFFF48FB1),
            darkPrimaryContainer = Color(0xFF631135),
            darkOnPrimaryContainer = Color(0xFFF8BBD0),
        )
        "ocean" -> AccentPalette(
            lightPrimary = Color(0xFF006782),
            lightPrimaryContainer = Color(0xFFB8EAFF),
            lightOnPrimaryContainer = Color(0xFF001F29),
            lightSecondary = Color(0xFF3F6270),
            lightSecondaryContainer = Color(0xFFD1E6F0),
            darkPrimary = Color(0xFF6CD2FF),
            darkPrimaryContainer = Color(0xFF004D62),
            darkOnPrimaryContainer = Color(0xFFB8EAFF),
        )
        "terracotta" -> AccentPalette(
            lightPrimary = Color(0xFFA84424),
            lightPrimaryContainer = Color(0xFFFFDBD0),
            lightOnPrimaryContainer = Color(0xFF3A0B00),
            lightSecondary = Color(0xFF77574D),
            lightSecondaryContainer = Color(0xFFFFDBD0),
            darkPrimary = Color(0xFFFFB59D),
            darkPrimaryContainer = Color(0xFF7E2B0E),
            darkOnPrimaryContainer = Color(0xFFFFDBD0),
        )
        "lavender" -> AccentPalette(
            lightPrimary = Color(0xFF5E479A),
            lightPrimaryContainer = Color(0xFFE5DEFF),
            lightOnPrimaryContainer = Color(0xFF1A0255),
            lightSecondary = Color(0xFF5F5C71),
            lightSecondaryContainer = Color(0xFFE5E0F9),
            darkPrimary = Color(0xFFC9BFFF),
            darkPrimaryContainer = Color(0xFF462F80),
            darkOnPrimaryContainer = Color(0xFFE5DEFF),
        )
        else -> AccentPalette(
            lightPrimary = Color(0xFF216C4E),
            lightPrimaryContainer = Color(0xFFCFF2DF),
            lightOnPrimaryContainer = Color(0xFF06291A),
            lightSecondary = Color(0xFF446455),
            lightSecondaryContainer = Color(0xFFD6EAE0),
            darkPrimary = Color(0xFF7EE19E),
            darkPrimaryContainer = Color(0xFF155138),
            darkOnPrimaryContainer = Color(0xFFCFF2DF),
        )
    }

    return if (darkTheme) {
        darkColorScheme(
            primary = palette.darkPrimary,
            onPrimary = Color(0xFF052014),
            primaryContainer = palette.darkPrimaryContainer,
            onPrimaryContainer = palette.darkOnPrimaryContainer,
            secondary = palette.darkPrimary,
            secondaryContainer = palette.darkPrimaryContainer.copy(alpha = 0.7f),
            onSecondaryContainer = palette.darkOnPrimaryContainer,
            background = Color(0xFF101512),
            surface = Color(0xFF18201C),
            surfaceVariant = Color(0xFF25302B),
            onSurface = Color(0xFFECF3EE),
            onSurfaceVariant = Color(0xFFB5C4BC),
            inverseSurface = Color(0xFFECF3EE),
            inverseOnSurface = Color(0xFF141B18),
        )
    } else {
        lightColorScheme(
            primary = palette.lightPrimary,
            onPrimary = Color.White,
            primaryContainer = palette.lightPrimaryContainer,
            onPrimaryContainer = palette.lightOnPrimaryContainer,
            secondary = palette.lightSecondary,
            secondaryContainer = palette.lightSecondaryContainer,
            onSecondaryContainer = palette.lightOnPrimaryContainer,
            background = Color(0xFFF6F8F5),
            surface = Color.White,
            surfaceVariant = Color(0xFFE5ECE7),
            onSurface = Color(0xFF17201B),
            onSurfaceVariant = Color(0xFF4D5D54),
            inverseSurface = Color(0xFF1E2B24),
            inverseOnSurface = Color.White,
        )
    }
}

private data class RunningCookingTimer(
    val id: String,
    val stepKey: String,
    val label: String,
    val totalSeconds: Int,
    val endTimeMillis: Long,
)

private data class StepDurationOption(
    val label: String,
    val seconds: Int,
)

private fun extractStepDurations(stepText: String): List<StepDurationOption> {
    val results = mutableListOf<StepDurationOption>()
    val lower = stepText.lowercase(Locale.ROOT)

    val rangeRegex = Regex("""(\d{1,3})\s*(?:[-–/]|o|a|to)\s*(\d{1,3})\s*(minuti|minuto|min|m|secondi|secondo|sec|s|ore|ora|h)\b""")
    rangeRegex.findAll(lower).forEach { match ->
        val first = match.groupValues[1].toIntOrNull() ?: return@forEach
        val second = match.groupValues[2].toIntOrNull() ?: return@forEach
        val unit = match.groupValues[3]
        val mult = when {
            unit.startsWith("s") -> 1
            unit.startsWith("o") || unit == "h" -> 3600
            else -> 60
        }
        val unitLabel = when (mult) {
            1 -> "sec"
            3600 -> "h"
            else -> "min"
        }
        results.add(StepDurationOption("$first $unitLabel", first * mult))
        if (second != first) {
            results.add(StepDurationOption("$second $unitLabel", second * mult))
        }
    }

    val cleanedFromRanges = rangeRegex.replace(lower, " ")
    val singleRegex = Regex("""\b(\d{1,3})\s*(minuti|minuto|min|secondi|secondo|sec|ore|ora)\b""")
    singleRegex.findAll(cleanedFromRanges).forEach { match ->
        val amount = match.groupValues[1].toIntOrNull() ?: return@forEach
        if (amount <= 0) return@forEach
        val unit = match.groupValues[2]
        val mult = when {
            unit.startsWith("s") -> 1
            unit.startsWith("o") -> 3600
            else -> 60
        }
        val unitLabel = when (mult) {
            1 -> "sec"
            3600 -> "h"
            else -> "min"
        }
        results.add(StepDurationOption("$amount $unitLabel", amount * mult))
    }

    if (Regex("""\b(un\s+minuto|1\s+minuto|one\s+minute|un\s+min)\b""").containsMatchIn(lower)) {
        results.add(StepDurationOption("1 min", 60))
    }
    if (Regex("""\b(un\s+paio\s+di\s+minuti|due\s+minuti|couple\s+of\s+minutes)\b""").containsMatchIn(lower)) {
        results.add(StepDurationOption("2 min", 120))
    }
    if (Regex("""\b(qualche\s+minuto|alcuni\s+minuti|pochi\s+minuti|few\s+minutes)\b""").containsMatchIn(lower)) {
        results.add(StepDurationOption("3 min", 180))
    }
    if (Regex("""\b(mezz'ora|mezzora|half\s+an\s+hour)\b""").containsMatchIn(lower)) {
        results.add(StepDurationOption("30 min", 1800))
    }

    return results.distinctBy { it.seconds }
}

private fun applyTimerToStep(step: String, minutes: Int): String {
    if (minutes < 0) return step

    val timerRegex = Regex(
        """\b(?:\d{1,3}\s*(?:[-–/]|o|a|to)\s*\d{1,3}\s*(?:secondi|secondo|sec|minuti|minuto|min|ore|ora|h)|\d{1,3}\s*(?:secondi|secondo|sec|minuti|minuto|min|ore|ora|h)|un\s+minuto|qualche\s+minuto|mezz['’]?ora)\b""",
        RegexOption.IGNORE_CASE,
    )
    val trailingTimerAnnotation = Regex("""\s*\(\s*\d{1,3}\s*min(?:uti)?\s*\)\s*$""", RegexOption.IGNORE_CASE)

    val match = timerRegex.find(step)
    if (minutes == 0) {
        if (match == null) return trailingTimerAnnotation.replace(step, "").trimEnd()
        var start = match.range.first
        val prefix = step.substring(0, start)
        val perPrefix = Regex("""\bper(?:\s+circa)?\s+$""", RegexOption.IGNORE_CASE).find(prefix)
        if (perPrefix != null) start = perPrefix.range.first
        var result = step.removeRange(start, match.range.last + 1)
        result = result.replace(Regex("""\s*\(\s*\)"""), "")
        result = result.replace(Regex("""\bper\s+(?=[,.;]|$)""", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("""\s+([,.;])"""), "$1")
        result = result.replace(Regex("""[ \t]{2,}"""), " ").trim()
        return trailingTimerAnnotation.replace(result, "").trimEnd()
    }

    val replacement = if (minutes == 1) "1 minuto" else "$minutes minuti"
    return if (match != null) {
        step.replaceRange(match.range, replacement)
    } else {
        step.trimEnd() + " ($replacement)"
    }
}

private fun formatCountdown(remainingSeconds: Long): String {
    val clamped = remainingSeconds.coerceAtLeast(0L)
    val hrs = clamped / 3600
    val mins = (clamped % 3600) / 60
    val secs = clamped % 60
    return if (hrs > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", mins, secs)
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? = runCatching {
    val dir = File(context.filesDir, "recipe_images").apply { mkdirs() }
    val file = File(dir, "recipe_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
    }
    Uri.fromFile(file).toString()
}.getOrNull()

private fun scheduleBackgroundTimerWork(context: Context, timerId: String, label: String, durationSeconds: Int) {
    val data = Data.Builder()
        .putString(ExpiryReminderWorker.KEY_TIMER_LABEL, label)
        .build()
    val request = OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
        .setInitialDelay(durationSeconds.toLong(), TimeUnit.SECONDS)
        .setInputData(data)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "zest_cooking_timer_$timerId",
        ExistingWorkPolicy.REPLACE,
        request,
    )
}

private fun cancelBackgroundTimerWork(context: Context, timerId: String) {
    WorkManager.getInstance(context).cancelUniqueWork("zest_cooking_timer_$timerId")
}

private data class ZingStrings(
    val langCode: String,
    val appTagline: String,
    val tabHome: String,
    val tabPantry: String,
    val tabRecipes: String,
    val tabShopping: String,
    val tabSettings: String,
    val greeting: String,
    val homeHero: String,
    val metricItems: String,
    val metricItemsSub: String,
    val metricExpiring: String,
    val quickAddTitle: String,
    val quickAddSub: String,
    val btnScan: String,
    val btnAdd: String,
    val consumeFirstTitle: String,
    val consumeFirstSub: String,
    val seeAll: String,
    val noUrgencyTitle: String,
    val noUrgencyText: String,
    val ideasTodayTitle: String,
    val ideasTodaySub: String,
    val emptyRecipesTitle: String,
    val emptyRecipesText: String,
    val openRecipes: String,
    val watchOutTitle: String,
    val routineTitle: String,
    val routineSub: String,
    val expired: String,
    val today: String,
    val tomorrow: String,
    val withoutDate: String,
    val inDays: (Int) -> String,
    val availablePct: (Int) -> String,
    val pantryTitle: String,
    val pantrySub: String,
    val searchPantryHint: String,
    val filterAll: String,
    val filterExpiring: String,
    val filterExpired: String,
    val filterLowStock: String,
    val sortBy: String,
    val sortExpiry: String,
    val sortName: String,
    val sortQty: String,
    val sortLocation: String,
    val noResults: String,
    val emptyPantryTitle: String,
    val emptyPantryText: String,
    val availableLabel: String,
    val expiryLabel: String,
    val recipesTitle: String,
    val recipesSub: String,
    val searchRecipeHint: String,
    val filterReady: String,
    val filterFavorites: String,
    val filterMyRecipes: String,
    val personalBadge: String,
    val allIngredientsReady: String,
    val missingIngredientsLabel: (Int) -> String,
    val shoppingTitle: String,
    val shoppingSub: String,
    val toBuyCount: (Int) -> String,
    val smartShoppingSub: String,
    val btnGenerateSmart: String,
    val btnClearChecked: String,
    val emptyShoppingTitle: String,
    val emptyShoppingText: String,
    val doneLabel: String,
    val suggestedLabel: String,
    val boughtAddInventory: String,
    val settingsTitle: String,
    val settingsSub: String,
    val secAppearance: String,
    val secAppearanceSub: String,
    val languageLabel: String,
    val darkThemeTitle: String,
    val darkThemeSub: String,
    val dateFormatLabel: String,
    val secDefaults: String,
    val secDefaultsSub: String,
    val defaultLocationLabel: String,
    val defaultUnitLabel: String,
    val defaultMinStockLabel: String,
    val secExpiry: String,
    val secExpirySub: String,
    val notificationsTitle: String,
    val notificationsSub: String,
    val notifyDayBeforeTitle: String,
    val notifyDayBeforeSub: String,
    val expiryAdvanceTitle: String,
    val openedAlertTitle: String,
    val openedAlertSub: String,
    val showExpiredHomeTitle: String,
    val showExpiredHomeSub: String,
    val secScanner: String,
    val secScannerSub: String,
    val vibrateScanTitle: String,
    val vibrateScanSub: String,
    val autoLookupTitle: String,
    val autoLookupSub: String,
    val secCooking: String,
    val secCookingSub: String,
    val householdTitle: String,
    val householdSub: String,
    val priorityLabel: String,
    val dietaryLabel: String,
    val excludedLabel: String,
    val savePrefsBtn: String,
    val secData: String,
    val secDataSub: String,
    val exportBackupBtn: String,
    val importBackupBtn: String,
    val deleteAllBtn: String,
    val cancelBtn: String,
    val confirmBtn: String,
    val saveBtn: String,
)

private fun stringsForLanguage(selectedLang: String): ZingStrings {
    val code = if (selectedLang == "system") {
        Locale.getDefault().language.lowercase(Locale.ROOT)
    } else {
        selectedLang.lowercase(Locale.ROOT)
    }
    return when (code) {
        "en" -> ZingStrings(
            langCode = "en",
            appTagline = "Zing · Organize your smart pantry",
            tabHome = "Home", tabPantry = "Pantry", tabRecipes = "Recipes", tabShopping = "Shopping", tabSettings = "Settings",
            greeting = "WELCOME TO ZING",
            homeHero = "Your kitchen,\nunder control.",
            metricItems = "Items", metricItemsSub = "currently in stock",
            metricExpiring = "To check",
            quickAddTitle = "Add in seconds", quickAddSub = "Scan a barcode or add an item manually",
            btnScan = "Scan", btnAdd = "Add",
            consumeFirstTitle = "Use first", consumeFirstSub = "Upcoming expirations & opened items",
            seeAll = "See all",
            noUrgencyTitle = "All clear", noUrgencyText = "Expiring items will appear right here.",
            ideasTodayTitle = "Ideas for today", ideasTodaySub = "Matched with your current stock",
            emptyRecipesTitle = "Start cooking", emptyRecipesText = "Open the recipe library to see what you can cook.",
            openRecipes = "Open recipes",
            watchOutTitle = "Keep an eye on",
            routineTitle = "Your routine", routineSub = "Suggestions from your history",
            expired = "Expired", today = "Today", tomorrow = "Tomorrow", withoutDate = "No date",
            inDays = { "In $it d" },
            availablePct = { "$it% in stock" },
            pantryTitle = "Your pantry", pantrySub = "Every lot tracks its own quantity and expiry date.",
            searchPantryHint = "Search item, brand or barcode",
            filterAll = "All", filterExpiring = "Expiring", filterExpired = "Expired", filterLowStock = "Low stock",
            sortBy = "Sort by", sortExpiry = "Expiry", sortName = "Name", sortQty = "Quantity", sortLocation = "Location",
            noResults = "No matching items", emptyPantryTitle = "Your pantry is empty", emptyPantryText = "Add your first item or scan a barcode.",
            availableLabel = "Available", expiryLabel = "Expiry",
            recipesTitle = "Recipes", recipesSub = "Practical ideas starting from what you already have.",
            searchRecipeHint = "Search a recipe or ingredient",
            filterReady = "Ready to cook", filterFavorites = "Favorites", filterMyRecipes = "My recipes",
            personalBadge = "Personal",
            allIngredientsReady = "✓ All ingredients in stock",
            missingIngredientsLabel = { if (it == 1) "Missing 1 ingredient" else "Missing $it ingredients" },
            shoppingTitle = "Shopping list", shoppingSub = "Only what you actually need.",
            toBuyCount = { "$it to buy" },
            smartShoppingSub = "Suggestions consider minimum stock and recurring purchases.",
            btnGenerateSmart = "Generate smart suggestions",
            btnClearChecked = "Remove checked items",
            emptyShoppingTitle = "Your list is clear", emptyShoppingText = "Add an item or generate smart suggestions.",
            doneLabel = "Done", suggestedLabel = "Suggested", boughtAddInventory = "Purchased · add to pantry",
            settingsTitle = "Settings", settingsSub = "Customize Zing on your device, with no account required.",
            secAppearance = "Language, Appearance & Formats", secAppearanceSub = "Interface, theme and date display",
            languageLabel = "App language",
            darkThemeTitle = "Dark theme", darkThemeSub = "Easier on the eyes at night",
            dateFormatLabel = "Date format",
            secDefaults = "Pantry Defaults", secDefaultsSub = "Pre-filled values when adding new lots",
            defaultLocationLabel = "Default location",
            defaultUnitLabel = "Default unit",
            defaultMinStockLabel = "Default minimum stock",
            secExpiry = "Expirations & Notifications", secExpirySub = "Dual reminders (preset day + day before)",
            notificationsTitle = "Expiry notifications", notificationsSub = "Local daily reminders",
            notifyDayBeforeTitle = "Extra alert 1 day before expiry", notifyDayBeforeSub = "Send a dedicated reminder the day before an item expires",
            expiryAdvanceTitle = "Preset warning advance",
            openedAlertTitle = "Opened package alert", openedAlertSub = "Highlight items opened for more than N days (0 = off)",
            showExpiredHomeTitle = "Show expired items on Home", showExpiredHomeSub = "Include already expired items in Home alerts",
            secScanner = "Barcode Scanner", secScannerSub = "Camera and Open Food Facts behavior",
            vibrateScanTitle = "Vibrate on scan", vibrateScanSub = "Haptic feedback when a barcode is detected",
            autoLookupTitle = "Automatic online lookup", autoLookupSub = "Search Open Food Facts automatically after scanning",
            secCooking = "Cooking, Servings & Diet", secCookingSub = "Recipe scaling, ranking priority and filters",
            householdTitle = "Household size", householdSub = "Default servings for recipe suggestions",
            priorityLabel = "Recipe ranking priority",
            dietaryLabel = "Dietary preferences",
            excludedLabel = "Ingredients to avoid (comma separated)",
            savePrefsBtn = "Save dietary preferences",
            secData = "Habits, Backup & Data", secDataSub = "All personal data stays on your device",
            exportBackupBtn = "Export JSON backup",
            importBackupBtn = "Restore from backup",
            deleteAllBtn = "Delete all local data",
            cancelBtn = "Cancel", confirmBtn = "Confirm", saveBtn = "Save",
        )
        "es" -> ZingStrings(
            langCode = "es",
            appTagline = "Zing · Organiza tu despensa",
            tabHome = "Inicio", tabPantry = "Despensa", tabRecipes = "Recetas", tabShopping = "Compras", tabSettings = "Ajustes",
            greeting = "HOLA, BIENVENIDO A ZING",
            homeHero = "Tu despensa,\nbajo control.",
            metricItems = "Alimentos", metricItemsSub = "con cantidad disponible",
            metricExpiring = "Por revisar",
            quickAddTitle = "Añade en un instante", quickAddSub = "Escanea o introduce un alimento",
            btnScan = "Escanear", btnAdd = "Añadir",
            consumeFirstTitle = "Consumir primero", consumeFirstSub = "Próximos vencimientos",
            seeAll = "Ver todo",
            noUrgencyTitle = "Sin urgencias", noUrgencyText = "Cuando registres fechas de caducidad aparecerán aquí.",
            ideasTodayTitle = "Ideas para hoy", ideasTodaySub = "Basadas en lo que tienes en casa",
            emptyRecipesTitle = "Empieza a cocinar", emptyRecipesText = "Abre el recetario y descubre qué puedes preparar.",
            openRecipes = "Abrir recetas",
            watchOutTitle = "Para tener en cuenta",
            routineTitle = "Tu rutina", routineSub = "Sugerencias basadas en tu historial",
            expired = "Caducado", today = "Hoy", tomorrow = "Mañana", withoutDate = "Sin fecha",
            inDays = { "En $it d" },
            availablePct = { "$it% disponible" },
            pantryTitle = "Tu despensa", pantrySub = "Cada lote conserva su cantidad y caducidad.",
            searchPantryHint = "Buscar alimento, marca o código",
            filterAll = "Todos", filterExpiring = "Por caducar", filterExpired = "Caducados", filterLowStock = "Stock bajo",
            sortBy = "Ordenar por", sortExpiry = "Caducidad", sortName = "Nombre", sortQty = "Cantidad", sortLocation = "Ubicación",
            noResults = "Sin resultados", emptyPantryTitle = "La despensa está vacía", emptyPantryText = "Añade tu primer alimento o escanea un código.",
            availableLabel = "Disponible", expiryLabel = "Caducidad",
            recipesTitle = "Recetas", recipesSub = "Ideas prácticas con los ingredientes que tienes.",
            searchRecipeHint = "Buscar receta o ingrediente",
            filterReady = "Preparables", filterFavorites = "Favoritas", filterMyRecipes = "Mis recetas",
            personalBadge = "Personal",
            allIngredientsReady = "✓ Todos los ingredientes disponibles",
            missingIngredientsLabel = { if (it == 1) "Falta 1 ingrediente" else "Faltan $it ingredientes" },
            shoppingTitle = "Lista de compras", shoppingSub = "Solo lo que realmente necesitas.",
            toBuyCount = { "$it por comprar" },
            smartShoppingSub = "Las sugerencias consideran el stock mínimo y tus compras.",
            btnGenerateSmart = "Generar sugerencias inteligentes",
            btnClearChecked = "Eliminar marcados",
            emptyShoppingTitle = "Lista vacía", emptyShoppingText = "Añade un producto o genera sugerencias automáticas.",
            doneLabel = "Hecho", suggestedLabel = "Sugerido", boughtAddInventory = "Comprado · añadir a despensa",
            settingsTitle = "Ajustes", settingsSub = "Personaliza Zing sin necesidad de crear una cuenta.",
            secAppearance = "Idioma, Apariencia y Formatos", secAppearanceSub = "Interfaz, tema visual y fechas",
            languageLabel = "Idioma de la aplicación",
            darkThemeTitle = "Tema oscuro", darkThemeSub = "Interfaz más cómoda por la noche",
            dateFormatLabel = "Formato de fecha",
            secDefaults = "Valores predeterminados", secDefaultsSub = "Valores iniciales al añadir alimentos",
            defaultLocationLabel = "Ubicación predeterminada",
            defaultUnitLabel = "Unidad predeterminada",
            defaultMinStockLabel = "Stock mínimo predeterminado",
            secExpiry = "Caducidades y Notificaciones", secExpirySub = "Aviso en día predefinido + día anterior",
            notificationsTitle = "Notificaciones de caducidad", notificationsSub = "Recordatorio diario local",
            notifyDayBeforeTitle = "Aviso extra 1 día antes", notifyDayBeforeSub = "Enviar una notificación adicional el día antes de caducar",
            expiryAdvanceTitle = "Antelación del aviso principal",
            openedAlertTitle = "Aviso de envase abierto", openedAlertSub = "Avisar tras N días desde la apertura (0 = desactivado)",
            showExpiredHomeTitle = "Mostrar caducados en Inicio", showExpiredHomeSub = "Incluir productos ya caducados en la pantalla principal",
            secScanner = "Escáner y Cámara", secScannerSub = "Comportamiento del lector y catálogo online",
            vibrateScanTitle = "Vibrar al escanear", vibrateScanSub = "Respuesta háptica al detectar un código",
            autoLookupTitle = "Búsqueda online automática", autoLookupSub = "Consultar Open Food Facts tras escanear",
            secCooking = "Cocina, Raciones y Dieta", secCookingSub = "Porciones, prioridad de recetas y filtros",
            householdTitle = "Personas en casa", householdSub = "Valor inicial para las raciones",
            priorityLabel = "Prioridad al sugerir recetas",
            dietaryLabel = "Preferencias alimentarias",
            excludedLabel = "Ingredientes a evitar",
            savePrefsBtn = "Guardar preferencias",
            secData = "Hábitos, Copia de seguridad y Datos", secDataSub = "Tus datos permanecen en el dispositivo",
            exportBackupBtn = "Exportar copia JSON",
            importBackupBtn = "Restaurar copia de seguridad",
            deleteAllBtn = "Eliminar todos los datos locales",
            cancelBtn = "Cancelar", confirmBtn = "Confirmar", saveBtn = "Guardar",
        )
        "fr" -> ZingStrings(
            langCode = "fr",
            appTagline = "Zing · Organisez votre garde-manger",
            tabHome = "Accueil", tabPantry = "Placard", tabRecipes = "Recettes", tabShopping = "Courses", tabSettings = "Réglages",
            greeting = "BIENVENUE SUR ZING",
            homeHero = "Votre cuisine,\nsous contrôle.",
            metricItems = "Aliments", metricItemsSub = "en stock",
            metricExpiring = "À vérifier",
            quickAddTitle = "Ajoutez en un instant", quickAddSub = "Scannez ou saisissez un aliment",
            btnScan = "Scanner", btnAdd = "Ajouter",
            consumeFirstTitle = "À consommer d'abord", consumeFirstSub = "Prochaines dates de péremption",
            seeAll = "Tout voir",
            noUrgencyTitle = "Aucune urgence", noUrgencyText = "Vos échéances apparaîtront ici.",
            ideasTodayTitle = "Idées du jour", ideasTodaySub = "Selon vos ingrédients disponibles",
            emptyRecipesTitle = "À vos fourneaux", emptyRecipesText = "Ouvrez les recettes pour voir ce que vous pouvez cuisiner.",
            openRecipes = "Ouvrir les recettes",
            watchOutTitle = "À surveiller",
            routineTitle = "Votre routine", routineSub = "Suggestions basées sur votre historique",
            expired = "Périmé", today = "Aujourd'hui", tomorrow = "Demain", withoutDate = "Sans date",
            inDays = { "Dans $it j" },
            availablePct = { "$it% présents" },
            pantryTitle = "Votre garde-manger", pantrySub = "Chaque lot possède sa quantité et sa date.",
            searchPantryHint = "Rechercher aliment, marque ou code",
            filterAll = "Tous", filterExpiring = "Bientôt périmés", filterExpired = "Périmés", filterLowStock = "Stock bas",
            sortBy = "Trier par", sortExpiry = "Péremption", sortName = "Nom", sortQty = "Quantité", sortLocation = "Emplacement",
            noResults = "Aucun résultat", emptyPantryTitle = "Le garde-manger est vide", emptyPantryText = "Ajoutez votre premier aliment ou scannez un code-barres.",
            availableLabel = "Disponible", expiryLabel = "Péremption",
            recipesTitle = "Recettes", recipesSub = "Des idées concrètes avec ce que vous avez.",
            searchRecipeHint = "Rechercher une recette ou un ingrédient",
            filterReady = "Prêtes à cuisiner", filterFavorites = "Favoris", filterMyRecipes = "Mes recettes",
            personalBadge = "Perso",
            allIngredientsReady = "✓ Tous les ingrédients sont présents",
            missingIngredientsLabel = { if (it == 1) "Il manque 1 ingrédient" else "Il manque $it ingrédients" },
            shoppingTitle = "Liste de courses", shoppingSub = "Uniquement ce dont vous avez besoin.",
            toBuyCount = { "$it à acheter" },
            smartShoppingSub = "Les suggestions tiennent compte du stock minimal et de vos habitudes.",
            btnGenerateSmart = "Générer des suggestions intelligentes",
            btnClearChecked = "Supprimer les articles cochés",
            emptyShoppingTitle = "La liste est vide", emptyShoppingText = "Ajoutez un produit ou générez des suggestions.",
            doneLabel = "Fait", suggestedLabel = "Suggéré", boughtAddInventory = "Acheté · ajouter au stock",
            settingsTitle = "Réglages", settingsSub = "Personnalisez Zing sans créer de compte.",
            secAppearance = "Langue, Apparence et Formats", secAppearanceSub = "Interface, thème et format de date",
            languageLabel = "Langue de l'application",
            darkThemeTitle = "Thème sombre", darkThemeSub = "Plus reposant le soir",
            dateFormatLabel = "Format de date",
            secDefaults = "Valeurs par défaut", secDefaultsSub = "Valeurs préremplies pour les nouveaux lots",
            defaultLocationLabel = "Emplacement par défaut",
            defaultUnitLabel = "Unité par défaut",
            defaultMinStockLabel = "Stock minimal par défaut",
            secExpiry = "Péremptions et Notifications", secExpirySub = "Jour prédéfini + veille de péremption",
            notificationsTitle = "Notifications d'échéance", notificationsSub = "Rappel local quotidien",
            notifyDayBeforeTitle = "Rappel supplémentaire la veille", notifyDayBeforeSub = "Envoyer une notification 1 jour avant la date limite",
            expiryAdvanceTitle = "Délai d'alerte principal",
            openedAlertTitle = "Alerte produit entamé", openedAlertSub = "Alerter après N jours d'ouverture (0 = désactivé)",
            showExpiredHomeTitle = "Afficher les périmés à l'accueil", showExpiredHomeSub = "Inclure les produits déjà périmés sur l'accueil",
            secScanner = "Scanner de code-barres", secScannerSub = "Caméra et recherche Open Food Facts",
            vibrateScanTitle = "Vibrer au scan", vibrateScanSub = "Retour haptique lors de la lecture",
            autoLookupTitle = "Recherche en ligne automatique", autoLookupSub = "Interroger Open Food Facts après le scan",
            secCooking = "Cuisine, Portions et Régime", secCookingSub = "Portions, priorité des recettes et filtres",
            householdTitle = "Personnes du foyer", householdSub = "Nombre de portions par défaut",
            priorityLabel = "Priorité des suggestions",
            dietaryLabel = "Préférences alimentaires",
            excludedLabel = "Ingrédients à éviter",
            savePrefsBtn = "Enregistrer les préférences",
            secData = "Habitudes, Sauvegarde et Données", secDataSub = "Vos données restent sur votre appareil",
            exportBackupBtn = "Exporter une sauvegarde JSON",
            importBackupBtn = "Restaurer une sauvegarde",
            deleteAllBtn = "Supprimer toutes les données locales",
            cancelBtn = "Annuler", confirmBtn = "Confirmer", saveBtn = "Enregistrer",
        )
        "de" -> ZingStrings(
            langCode = "de",
            appTagline = "Zing · Dein smarter Vorratsschrank",
            tabHome = "Start", tabPantry = "Vorrat", tabRecipes = "Rezepte", tabShopping = "Einkauf", tabSettings = "Optionen",
            greeting = "WILLKOMMEN BEI ZING",
            homeHero = "Deine Vorräte,\nim Griff.",
            metricItems = "Vorräte", metricItemsSub = "aktuell verfügbar",
            metricExpiring = "Prüfen",
            quickAddTitle = "Schnell hinzufügen", quickAddSub = "Barcode scannen oder manuell eintragen",
            btnScan = "Scannen", btnAdd = "Neu",
            consumeFirstTitle = "Zuerst verbrauchen", consumeFirstSub = "Nächste Ablaufdaten",
            seeAll = "Alle",
            noUrgencyTitle = "Alles frisch", noUrgencyText = "Ablaufende Lebensmittel erscheinen hier.",
            ideasTodayTitle = "Ideen für heute", ideasTodaySub = "Passend zu deinem Bestand",
            emptyRecipesTitle = "Los geht's in der Küche", emptyRecipesText = "Öffne die Rezeptsammlung und entdecke passende Gerichte.",
            openRecipes = "Rezepte öffnen",
            watchOutTitle = "Im Blick behalten",
            routineTitle = "Deine Routine", routineSub = "Vorschläge aus deinem Verlauf",
            expired = "Abgelaufen", today = "Heute", tomorrow = "Morgen", withoutDate = "Ohne Datum",
            inDays = { "In $it T." },
            availablePct = { "$it% vorrätig" },
            pantryTitle = "Dein Vorrat", pantrySub = "Jede Packung hat ihre eigene Menge und Haltbarkeit.",
            searchPantryHint = "Lebensmittel, Marke oder Barcode suchen",
            filterAll = "Alle", filterExpiring = "Bald fällig", filterExpired = "Abgelaufen", filterLowStock = "Wenig Vorrat",
            sortBy = "Sortieren", sortExpiry = "Ablauf", sortName = "Name", sortQty = "Menge", sortLocation = "Ort",
            noResults = "Keine Treffer", emptyPantryTitle = "Dein Vorrat ist leer", emptyPantryText = "Füge ein Lebensmittel hinzu oder scanne einen Barcode.",
            availableLabel = "Verfügbar", expiryLabel = "Ablauf",
            recipesTitle = "Rezepte", recipesSub = "Kochideen mit vorhandenen Zutaten.",
            searchRecipeHint = "Rezept oder Zutat suchen",
            filterReady = "Sofort kochbar", filterFavorites = "Favoriten", filterMyRecipes = "Meine Rezepte",
            personalBadge = "Eigenes",
            allIngredientsReady = "✓ Alle Zutaten vorhanden",
            missingIngredientsLabel = { if (it == 1) "1 Zutat fehlt" else "$it Zutaten fehlen" },
            shoppingTitle = "Einkaufsliste", shoppingSub = "Nur was wirklich fehlt.",
            toBuyCount = { "$it offen" },
            smartShoppingSub = "Vorschläge basieren auf Mindestbestand und Verlauf.",
            btnGenerateSmart = "Smarte Vorschläge erzeugen",
            btnClearChecked = "Erledigte entfernen",
            emptyShoppingTitle = "Liste ist leer", emptyShoppingText = "Produkt hinzufügen oder Vorschläge generieren.",
            doneLabel = "Erledigt", suggestedLabel = "Vorschlag", boughtAddInventory = "Gekauft · zum Vorrat",
            settingsTitle = "Einstellungen", settingsSub = "Passe Zing lokal ohne Benutzerkonto an.",
            secAppearance = "Sprache, Design & Formate", secAppearanceSub = "Oberfläche, dunkles Design und Datum",
            languageLabel = "App-Sprache",
            darkThemeTitle = "Dunkles Design", darkThemeSub = "Angenehmer am Abend",
            dateFormatLabel = "Datumsformat",
            secDefaults = "Standardwerte", secDefaultsSub = "Voreinstellungen für neue Lebensmittel",
            defaultLocationLabel = "Standard-Lagerort",
            defaultUnitLabel = "Standard-Einheit",
            defaultMinStockLabel = "Standard-Mindestbestand",
            secExpiry = "Ablaufdaten & Mitteilungen", secExpirySub = "Vorwarnung + Erinnerung am Vortag",
            notificationsTitle = "Ablauf-Erinnerungen", notificationsSub = "Tägliche lokale Benachrichtigung",
            notifyDayBeforeTitle = "Extra-Erinnerung 1 Tag vorher", notifyDayBeforeSub = "Zusätzliche Benachrichtigung am Tag vor dem Ablaufdatum",
            expiryAdvanceTitle = "Geplante Vorwarnzeit",
            openedAlertTitle = "Hinweis für geöffnete Packungen", openedAlertSub = "Nach N Tagen seit Öffnung warnen (0 = aus)",
            showExpiredHomeTitle = "Abgelaufene auf Startseite zeigen", showExpiredHomeSub = "Bereits abgelaufene Produkte oben anzeigen",
            secScanner = "Barcode-Scanner", secScannerSub = "Kamera und Open Food Facts",
            vibrateScanTitle = "Vibration beim Scannen", vibrateScanSub = "Haptisches Feedback bei Erkennung",
            autoLookupTitle = "Automatische Online-Suche", autoLookupSub = "Open Food Facts direkt nach dem Scan abfragen",
            secCooking = "Kochen, Portionen & Ernährung", secCookingSub = "Haushaltsgröße, Rezept-Priorität und Filter",
            householdTitle = "Personen im Haushalt", householdSub = "Standard-Portionen für Rezepte",
            priorityLabel = "Rezept-Sortierung",
            dietaryLabel = "Ernährungsweise",
            excludedLabel = "Zu vermeidende Zutaten",
            savePrefsBtn = "Ernährungsfilter speichern",
            secData = "Verlauf, Backup & Daten", secDataSub = "Alle Daten bleiben auf deinem Gerät",
            exportBackupBtn = "JSON-Backup exportieren",
            importBackupBtn = "Backup wiederherstellen",
            deleteAllBtn = "Alle lokalen Daten löschen",
            cancelBtn = "Abbrechen", confirmBtn = "Bestätigen", saveBtn = "Speichern",
        )
        "pt" -> ZingStrings(
            langCode = "pt",
            appTagline = "Zing · Organize a sua despensa",
            tabHome = "Início", tabPantry = "Despensa", tabRecipes = "Receitas", tabShopping = "Compras", tabSettings = "Ajustes",
            greeting = "BEM-VINDO AO ZING",
            homeHero = "A sua despensa,\nsob controlo.",
            metricItems = "Alimentos", metricItemsSub = "com stock disponível",
            metricExpiring = "A verificar",
            quickAddTitle = "Adicione num instante", quickAddSub = "Leia o código ou introduza manualmente",
            btnScan = "Ler código", btnAdd = "Adicionar",
            consumeFirstTitle = "Consumir primeiro", consumeFirstSub = "Próximas validades",
            seeAll = "Ver tudo",
            noUrgencyTitle = "Sem urgências", noUrgencyText = "Quando registar validades, aparecerão aqui.",
            ideasTodayTitle = "Ideias para hoje", ideasTodaySub = "Baseadas no que tem em casa",
            emptyRecipesTitle = "Comece a cozinhar", emptyRecipesText = "Abra as receitas para descobrir o que pode preparar.",
            openRecipes = "Abrir receitas",
            watchOutTitle = "Para acompanhar",
            routineTitle = "A sua rotina", routineSub = "Sugestões baseadas no histórico",
            expired = "Expirado", today = "Hoje", tomorrow = "Amanhã", withoutDate = "Sem data",
            inDays = { "Em $it d" },
            availablePct = { "$it% disponíveis" },
            pantryTitle = "A sua despensa", pantrySub = "Cada lote tem a sua quantidade e validade.",
            searchPantryHint = "Procurar alimento, marca ou código",
            filterAll = "Todos", filterExpiring = "A expirar", filterExpired = "Expirados", filterLowStock = "Stock baixo",
            sortBy = "Ordenar por", sortExpiry = "Validade", sortName = "Nome", sortQty = "Quantidade", sortLocation = "Local",
            noResults = "Sem resultados", emptyPantryTitle = "A despensa está vazia", emptyPantryText = "Adicione o primeiro alimento ou leia um código de barras.",
            availableLabel = "Disponível", expiryLabel = "Validade",
            recipesTitle = "Receitas", recipesSub = "Ideias práticas com os ingredientes que já tem.",
            searchRecipeHint = "Procurar receita ou ingrediente",
            filterReady = "Prontas a fazer", filterFavorites = "Favoritas", filterMyRecipes = "Minhas receitas",
            personalBadge = "Pessoal",
            allIngredientsReady = "✓ Todos os ingredientes disponíveis",
            missingIngredientsLabel = { if (it == 1) "Falta 1 ingrediente" else "Faltam $it ingredientes" },
            shoppingTitle = "Lista de compras", shoppingSub = "Apenas o que realmente precisa.",
            toBuyCount = { "$it por comprar" },
            smartShoppingSub = "As sugestões consideram o stock mínimo e compras habituais.",
            btnGenerateSmart = "Gerar sugestões inteligentes",
            btnClearChecked = "Limpar itens marcados",
            emptyShoppingTitle = "A lista está livre", emptyShoppingText = "Adicione um produto ou gere sugestões.",
            doneLabel = "Feito", suggestedLabel = "Sugerido", boughtAddInventory = "Comprado · adicionar ao stock",
            settingsTitle = "Definições", settingsSub = "Personalize o Zing sem criar conta.",
            secAppearance = "Idioma, Aparência e Formatos", secAppearanceSub = "Interface, tema escuro e datas",
            languageLabel = "Idioma da aplicação",
            darkThemeTitle = "Tema escuro", darkThemeSub = "Mais confortável à noite",
            dateFormatLabel = "Formato de data",
            secDefaults = "Valores predefinidos", secDefaultsSub = "Valores iniciais para novos lotes",
            defaultLocationLabel = "Local predefinido",
            defaultUnitLabel = "Unidade predefinida",
            defaultMinStockLabel = "Stock mínimo predefinido",
            secExpiry = "Validades e Notificações", secExpirySub = "Dia predefinido + véspera da validade",
            notificationsTitle = "Notificações de validade", notificationsSub = "Lembrete local diário",
            notifyDayBeforeTitle = "Aviso extra 1 dia antes", notifyDayBeforeSub = "Enviar notificação adicional na véspera da validade",
            expiryAdvanceTitle = "Antecedência do aviso principal",
            openedAlertTitle = "Aviso de embalagem aberta", openedAlertSub = "Avisar após N dias aberto (0 = desativado)",
            showExpiredHomeTitle = "Mostrar expirados no Início", showExpiredHomeSub = "Incluir produtos já expirados no resumo inicial",
            secScanner = "Leitor de Código de Barras", secScannerSub = "Câmara e catálogo Open Food Facts",
            vibrateScanTitle = "Vibrar ao ler código", vibrateScanSub = "Feedback tátil ao detetar código",
            autoLookupTitle = "Pesquisa online automática", autoLookupSub = "Consultar Open Food Facts após leitura",
            secCooking = "Cozinha, Doses e Dieta", secCookingSub = "Pessoas em casa, prioridade de receitas e filtros",
            householdTitle = "Pessoas em casa", householdSub = "Doses iniciais sugeridas nas receitas",
            priorityLabel = "Prioridade das receitas",
            dietaryLabel = "Preferências alimentares",
            excludedLabel = "Ingredientes a evitar",
            savePrefsBtn = "Guardar preferências",
            secData = "Hábitos, Backup e Dados", secDataSub = "Os seus dados ficam no dispositivo",
            exportBackupBtn = "Exportar backup JSON",
            importBackupBtn = "Restaurar de backup",
            deleteAllBtn = "Eliminar todos os dados locais",
            cancelBtn = "Cancelar", confirmBtn = "Confirmar", saveBtn = "Guardar",
        )
        else -> ZingStrings(
            langCode = "it",
            appTagline = "Zing · Organizza la tua dispensa",
            tabHome = "Home", tabPantry = "Dispensa", tabRecipes = "Ricette", tabShopping = "Spesa", tabSettings = "Impostazioni",
            greeting = "BENVENUTO SU ZING",
            homeHero = "La tua dispensa,\nsotto controllo.",
            metricItems = "Alimenti", metricItemsSub = "con quantità disponibile",
            metricExpiring = "Da controllare",
            quickAddTitle = "Aggiungi in un attimo", quickAddSub = "Scansiona o inserisci un alimento",
            btnScan = "Scansiona", btnAdd = "Aggiungi",
            consumeFirstTitle = "Da consumare prima", consumeFirstSub = "Prossime scadenze e confezioni aperte",
            seeAll = "Vedi tutto",
            noUrgencyTitle = "Nessuna urgenza", noUrgencyText = "Quando registrerai delle scadenze, le troverai qui.",
            ideasTodayTitle = "Idee per oggi", ideasTodaySub = "Basate su quello che hai in casa",
            emptyRecipesTitle = "La cucina comincia da qui", emptyRecipesText = "Apri la libreria di ricette e scopri cosa puoi preparare.",
            openRecipes = "Apri ricette",
            watchOutTitle = "Da tenere d'occhio",
            routineTitle = "La tua routine", routineSub = "Suggerimenti basati sulla cronologia",
            expired = "Scaduto", today = "Oggi", tomorrow = "Domani", withoutDate = "Senza data",
            inDays = { "Tra $it gg" },
            availablePct = { "$it% presenti" },
            pantryTitle = "La tua dispensa", pantrySub = "Ogni lotto ha la sua quantità e scadenza.",
            searchPantryHint = "Cerca alimento, marca o codice",
            filterAll = "Tutti", filterExpiring = "In scadenza", filterExpired = "Scaduti", filterLowStock = "Scorte basse",
            sortBy = "Ordina per", sortExpiry = "Scadenza", sortName = "Nome", sortQty = "Quantità", sortLocation = "Posizione",
            noResults = "Nessun risultato", emptyPantryTitle = "La dispensa è vuota", emptyPantryText = "Aggiungi il primo alimento oppure scansiona un codice a barre.",
            availableLabel = "Disponibile", expiryLabel = "Scadenza",
            recipesTitle = "Ricette", recipesSub = "Idee concrete, partendo dagli ingredienti che hai.",
            searchRecipeHint = "Cerca una ricetta o un ingrediente",
            filterReady = "Preparabili", filterFavorites = "Preferite", filterMyRecipes = "Le mie ricette",
            personalBadge = "Personale",
            allIngredientsReady = "✓ Tutti gli ingredienti presenti",
            missingIngredientsLabel = { if (it == 1) "Manca 1 ingrediente" else "Mancano $it ingredienti" },
            shoppingTitle = "Lista della spesa", shoppingSub = "Solo ciò che serve davvero.",
            toBuyCount = { "$it da acquistare" },
            smartShoppingSub = "I suggerimenti tengono conto delle scorte e dei prodotti abituali.",
            btnGenerateSmart = "Genera suggerimenti intelligenti",
            btnClearChecked = "Rimuovi prodotti già spuntati",
            emptyShoppingTitle = "La lista è libera", emptyShoppingText = "Aggiungi un prodotto o genera suggerimenti dalle scorte minime.",
            doneLabel = "Fatto", suggestedLabel = "Suggerito", boughtAddInventory = "Acquistato · aggiungi all'inventario",
            settingsTitle = "Impostazioni", settingsSub = "Personalizza Zing sul tuo dispositivo, senza account.",
            secAppearance = "Lingua, Aspetto e Formati", secAppearanceSub = "Lingua dell'interfaccia, tema visivo e formato date",
            languageLabel = "Lingua dell'applicazione",
            darkThemeTitle = "Tema scuro", darkThemeSub = "Un'interfaccia più riposante la sera",
            dateFormatLabel = "Formato della data",
            secDefaults = "Valori Predefiniti Dispensa", secDefaultsSub = "Precompilazione rapida quando aggiungi un nuovo prodotto",
            defaultLocationLabel = "Posizione predefinita",
            defaultUnitLabel = "Unità di misura predefinita",
            defaultMinStockLabel = "Scorta minima predefinita",
            secExpiry = "Scadenze, Aperture e Notifiche", secExpirySub = "Doppia notifica: giorno preimpostato + giorno prima",
            notificationsTitle = "Notifiche scadenze", notificationsSub = "Attiva gli avvisi locali sulle scadenze",
            notifyDayBeforeTitle = "Notifica anche 1 giorno prima", notifyDayBeforeSub = "Invia un promemoria dedicato la vigilia della scadenza (opzionale)",
            expiryAdvanceTitle = "Giorno preimpostato di preavviso",
            openedAlertTitle = "Avviso confezioni aperte", openedAlertSub = "Evidenzia prodotti aperti da più di N giorni (0 = disattivato)",
            showExpiredHomeTitle = "Mostra scaduti nella Home", showExpiredHomeSub = "Includi i prodotti già scaduti nel riepilogo iniziale",
            secScanner = "Scanner e Fotocamera", secScannerSub = "Comportamento lettore codici e ricerca Open Food Facts",
            vibrateScanTitle = "Vibrazione alla scansione", vibrateScanSub = "Feedback aptico appena il codice viene letto",
            autoLookupTitle = "Ricerca online automatica", autoLookupSub = "Cerca subito il prodotto su Open Food Facts dopo la scansione",
            secCooking = "Cucina, Porzioni e Dieta", secCookingSub = "Porzioni predefinite, priorità suggerimenti e filtri alimentari",
            householdTitle = "Persone in casa", householdSub = "Valore iniziale per le porzioni suggerite",
            priorityLabel = "Priorità ricette consigliate",
            dietaryLabel = "Preferenze alimentari (es. vegetariano, vegano)",
            excludedLabel = "Ingredienti da evitare (separati da virgola)",
            savePrefsBtn = "Salva preferenze alimentari",
            secData = "Abitudini, Backup e Dati", secDataSub = "Tutti i tuoi dati restano salvati localmente",
            exportBackupBtn = "Esporta backup JSON",
            importBackupBtn = "Ripristina da backup",
            deleteAllBtn = "Elimina tutti i dati locali",
            cancelBtn = "Annulla", confirmBtn = "Conferma", saveBtn = "Salva",
        )
    }
}

private val LocalStrings = compositionLocalOf { stringsForLanguage("it") }
private val LocalExtraPrefs = compositionLocalOf { ZingExtraPrefs() }
private val LocalRecipeLocalizer = compositionLocalOf<RecipeLocalizer?> { null }
private val LocalTabletLayout = compositionLocalOf { false }

private fun translateLocation(canonical: String, lang: String): String = when (lang) {
    "en" -> when (canonical) {
        "Dispensa" -> "Pantry"; "Frigorifero" -> "Fridge"; "Congelatore" -> "Freezer"; "Cantina" -> "Cellar"; "Altro" -> "Other"; else -> canonical
    }
    "es" -> when (canonical) {
        "Dispensa" -> "Despensa"; "Frigorifero" -> "Nevera"; "Congelatore" -> "Congelador"; "Cantina" -> "Bodega"; "Altro" -> "Otro"; else -> canonical
    }
    "fr" -> when (canonical) {
        "Dispensa" -> "Placard"; "Frigorifero" -> "Frigo"; "Congelatore" -> "Congélateur"; "Cantina" -> "Cave"; "Altro" -> "Autre"; else -> canonical
    }
    "de" -> when (canonical) {
        "Dispensa" -> "Vorratsschrank"; "Frigorifero" -> "Kühlschrank"; "Congelatore" -> "Gefrierfach"; "Cantina" -> "Keller"; "Altro" -> "Sonstiges"; else -> canonical
    }
    "pt" -> when (canonical) {
        "Dispensa" -> "Despensa"; "Frigorifero" -> "Frigorífico"; "Congelatore" -> "Congelador"; "Cantina" -> "Adega"; "Altro" -> "Outro"; else -> canonical
    }
    else -> canonical
}

private fun translateCategory(canonical: String, lang: String): String = when (lang) {
    "en" -> when (canonical) {
        "Tutte" -> "All"; "Altro" -> "Other"; "Frutta e verdura" -> "Fruit & Veg"; "Pasta e cereali" -> "Pasta & Grains"
        "Latticini" -> "Dairy"; "Carne e pesce" -> "Meat & Fish"; "Bevande" -> "Drinks"; "Surgelati" -> "Frozen"
        "Conserve" -> "Canned"; "Pane e forno" -> "Bakery"; "Snack" -> "Snacks"; "Primi" -> "First courses"
        "Secondi" -> "Main courses"; "Piatti unici" -> "One-pot"; "Insalate" -> "Salads"; "Zuppe" -> "Soups"
        "Contorni" -> "Sides"; "Antipasti" -> "Starters"; "Colazioni" -> "Breakfast"; "Dolci" -> "Desserts"
        else -> canonical
    }
    "es" -> when (canonical) {
        "Tutte" -> "Todas"; "Altro" -> "Otro"; "Frutta e verdura" -> "Fruta y verdura"; "Pasta e cereali" -> "Pasta y cereales"
        "Latticini" -> "Lácteos"; "Carne e pesce" -> "Carne y pescado"; "Bevande" -> "Bebidas"; "Surgelati" -> "Congelados"
        "Conserve" -> "Conservas"; "Pane e forno" -> "Panadería"; "Snack" -> "Snacks"; "Primi" -> "Primeros"
        "Secondi" -> "Segundos"; "Piatti unici" -> "Platos únicos"; "Insalate" -> "Ensaladas"; "Zuppe" -> "Sopas"
        "Contorni" -> "Guarniciones"; "Antipasti" -> "Entrantes"; "Colazioni" -> "Desayunos"; "Dolci" -> "Postres"
        else -> canonical
    }
    "fr" -> when (canonical) {
        "Tutte" -> "Toutes"; "Altro" -> "Autre"; "Frutta e verdura" -> "Fruits & Légumes"; "Pasta e cereali" -> "Pâtes & Céréales"
        "Latticini" -> "Produits laitiers"; "Carne e pesce" -> "Viande & Poisson"; "Bevande" -> "Boissons"; "Surgelati" -> "Surgelés"
        "Conserve" -> "Conserves"; "Pane e forno" -> "Boulangerie"; "Snack" -> "Snacks"; "Primi" -> "Entrées chaudes"
        "Secondi" -> "Plats"; "Piatti unici" -> "Plats complets"; "Insalate" -> "Salades"; "Zuppe" -> "Soupes"
        "Contorni" -> "Accompagnements"; "Antipasti" -> "Apéritifs"; "Colazioni" -> "Petit-déj"; "Dolci" -> "Desserts"
        else -> canonical
    }
    "de" -> when (canonical) {
        "Tutte" -> "Alle"; "Altro" -> "Sonstiges"; "Frutta e verdura" -> "Obst & Gemüse"; "Pasta e cereali" -> "Nudeln & Getreide"
        "Latticini" -> "Milchprodukte"; "Carne e pesce" -> "Fleisch & Fisch"; "Bevande" -> "Getränke"; "Surgelati" -> "Tiefkühlkost"
        "Conserve" -> "Konserven"; "Pane e forno" -> "Backwaren"; "Snack" -> "Snacks"; "Primi" -> "Vorspeisen"
        "Secondi" -> "Hauptgerichte"; "Piatti unici" -> "Eintöpfe"; "Insalate" -> "Salate"; "Zuppe" -> "Suppen"
        "Contorni" -> "Beilagen"; "Antipasti" -> "Antipasti"; "Colazioni" -> "Frühstück"; "Dolci" -> "Desserts"
        else -> canonical
    }
    "pt" -> when (canonical) {
        "Tutte" -> "Todas"; "Altro" -> "Outro"; "Frutta e verdura" -> "Fruta e legumes"; "Pasta e cereali" -> "Massa e cereais"
        "Latticini" -> "Laticínios"; "Carne e peixe" -> "Carne e peixe"; "Bevande" -> "Bebidas"; "Surgelati" -> "Congelados"
        "Conserve" -> "Conservas"; "Pane e forno" -> "Padaria"; "Snack" -> "Snacks"; "Primi" -> "Primeiros pratos"
        "Secondi" -> "Pratos principais"; "Piatti unici" -> "Pratos únicos"; "Insalate" -> "Saladas"; "Zuppe" -> "Sopas"
        "Contorni" -> "Acompanhamentos"; "Antipasti" -> "Entradas"; "Colazioni" -> "Pequeno-almoço"; "Dolci" -> "Sobremesas"
        else -> canonical
    }
    else -> canonical
}

private fun formatUserDate(millis: Long?, pattern: String): String {
    if (millis == null) return "—"
    return runCatching {
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    }.getOrElse { formatItalianDate(millis) }
}

private fun isOpenedTooLong(lot: FoodLotEntity, openedAlertDays: Int): Boolean {
    if (openedAlertDays <= 0 || lot.quantity <= 0.0) return false
    val opened = lot.openedDateMillis ?: return false
    val daysOpen = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - opened).toInt()
    return daysOpen >= openedAlertDays
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryApp(viewModel: PantryViewModel) {
    val context = LocalContext.current
    val view = LocalView.current
    val lots by viewModel.lots.collectAsStateWithLifecycle()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val shopping by viewModel.shopping.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val habitSuggestions by viewModel.habitSuggestions.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    val applicationContext = context.applicationContext
    val activity = remember(context) { context.findActivity() }
    val adsManager = remember(applicationContext) { ZingAdsManager(applicationContext) }
    val adsState by adsManager.state.collectAsStateWithLifecycle()
    val billingManager = remember(applicationContext) { GooglePlayBillingManager(applicationContext) }
    val playProUnlocked by billingManager.proUnlocked.collectAsStateWithLifecycle()
    val playProPrice by billingManager.formattedPrice.collectAsStateWithLifecycle()
    val playProProductAvailable by billingManager.productAvailable.collectAsStateWithLifecycle()
    val playProStatus by billingManager.statusMessage.collectAsStateWithLifecycle()
    val googlePlayActivationTime by billingManager.activationTimeMillis.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val proCodeClient = remember(applicationContext) { ZingProCodeRedemptionClient(applicationContext) }
    val workScope = rememberCoroutineScope()
    var customCodeProActive by remember { mutableStateOf(false) }
    var customCodeActivatedAt by remember { mutableStateOf<Long?>(null) }
    var customCodeStatusKey by remember { mutableStateOf<String?>(null) }
    var usageStreak by remember(applicationContext) { mutableStateOf(UsageStreak.recordToday(applicationContext)) }
    val anyProUnlocked = playProUnlocked || customCodeProActive

    val appUpdateManager = remember(activity) { activity?.let { AppUpdateManagerFactory.create(it) } }
    var appUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateAvailable by remember { mutableStateOf(false) }
    var updateAvailableVersion by remember { mutableIntStateOf(-1) }
    val updatePrefs = remember(applicationContext) { applicationContext.getSharedPreferences("zing_play_update", Context.MODE_PRIVATE) }
    var updateBannerDismissed by remember { mutableStateOf(false) }
    val checkForUpdate: () -> Unit = {
        val manager = appUpdateManager
        if (manager != null) {
            manager.appUpdateInfo
                .addOnSuccessListener { info ->
                    val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    val version = if (available) info.availableVersionCode() else -1
                    appUpdateInfo = if (available) info else null
                    updateAvailableVersion = version
                    updateBannerDismissed = available && updatePrefs.getInt("dismissed_version_code", -2) == version
                    updateAvailable = available && !updateBannerDismissed
                }
                .addOnFailureListener {
                    appUpdateInfo = null
                    updateAvailable = false
                    updateAvailableVersion = -1
                }
        }
    }
    val updateFlowLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        // If the user cancels or Play cannot start the flow, recheck instead of leaving stale UI state.
        if (result.resultCode != Activity.RESULT_OK) checkForUpdate()
    }
    // Choose navigation and pane layouts from available window width, not device brand.
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    val useTabletLayout = windowWidthDp >= 600
    val useDualPaneRecipes = windowWidthDp >= 900

    DisposableEffect(appUpdateManager) {
        val manager = appUpdateManager
        val installListener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                manager?.completeUpdate()
            }
        }
        manager?.registerListener(installListener)
        onDispose { manager?.unregisterListener(installListener) }
    }

    DisposableEffect(billingManager) {
        billingManager.start()
        onDispose { billingManager.endConnection() }
    }
    DisposableEffect(lifecycleOwner, billingManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) billingManager.refreshPurchases()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var storedExtraPrefs by remember { mutableStateOf(loadExtraPrefs(context).copy(supporterProUnlocked = false)) }
    val extraPrefs = storedExtraPrefs.copy(
        supporterProUnlocked = anyProUnlocked,
        keepScreenOnCooking = anyProUnlocked && storedExtraPrefs.keepScreenOnCooking,
    )
    var onboardingVisible by rememberSaveable {
        mutableStateOf(
            !applicationContext.getSharedPreferences("zing_onboarding", Context.MODE_PRIVATE)
                .getBoolean("completed", false),
        )
    }
    val onboardingPreferences = remember(applicationContext) {
        applicationContext.getSharedPreferences("zing_onboarding", Context.MODE_PRIVATE)
    }
    val finishOnboarding: () -> Unit = {
        onboardingPreferences.edit().putBoolean("completed", true).apply()
        onboardingVisible = false
    }

    LaunchedEffect(activity, extraPrefs.voluntarySupportAdsEnabled, onboardingVisible) {
        activity?.let { adsManager.refreshConsent(it, extraPrefs.voluntarySupportAdsEnabled && !onboardingVisible) }
        if (!onboardingVisible) {
            checkForUpdate()
            workScope.launch {
                val result = proCodeClient.refreshEntitlement()
                customCodeProActive = result.active
                if (result.activatedAtMillis != null) customCodeActivatedAt = result.activatedAtMillis
                if (result.active && customCodeStatusKey == null) customCodeStatusKey = "Zing Pro attivo tramite codice Zing."
            }
        }
    }
    DisposableEffect(lifecycleOwner, applicationContext, onboardingVisible, appUpdateManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageStreak = UsageStreak.recordToday(applicationContext)
                if (!onboardingVisible) {
                    checkForUpdate()
                    workScope.launch {
                        val result = proCodeClient.refreshEntitlement()
                        customCodeProActive = result.active
                        if (result.activatedAtMillis != null) customCodeActivatedAt = result.activatedAtMillis
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val updateExtraPrefs: (ZingExtraPrefs) -> Unit = { updated ->
        val stored = updated.copy(
            supporterProUnlocked = false,
            keepScreenOnCooking = if (anyProUnlocked) updated.keepScreenOnCooking else storedExtraPrefs.keepScreenOnCooking,
        )
        storedExtraPrefs = stored
        saveExtraPrefs(context, stored)
    }
    LaunchedEffect(message) {
        if (message?.startsWith("Backup ripristinato:") == true) {
            storedExtraPrefs = loadExtraPrefs(context).copy(supporterProUnlocked = false)
            billingManager.refreshPurchases()
        }
    }
    val strings = remember(extraPrefs.language) { stringsForLanguage(extraPrefs.language) }
    val recipeLocalizer = remember(context) { RecipeLocalizer(context) }
    val activeAccent = if (anyProUnlocked) extraPrefs.supporterAccentTheme else "emerald"
    val dynamicColorScheme = remember(settings.darkTheme, activeAccent) {
        buildZingColorScheme(settings.darkTheme, activeAccent)
    }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var scannerActive by rememberSaveable { mutableStateOf(false) }
    var addLotVisible by rememberSaveable { mutableStateOf(false) }
    var scannedBarcode by rememberSaveable { mutableStateOf("") }
    var lookupResult by remember { mutableStateOf<ProductLookupResult?>(null) }
    var editingLot by remember { mutableStateOf<FoodLotEntity?>(null) }
    var deleteLot by remember { mutableStateOf<FoodLotEntity?>(null) }
    var selectedRecipeId by rememberSaveable { mutableStateOf<Long?>(null) }
    var recipeEditorVisible by rememberSaveable { mutableStateOf(false) }
    var editingRecipe by remember { mutableStateOf<RecipeEntity?>(null) }
    var deleteRecipe by remember { mutableStateOf<RecipeEntity?>(null) }
    var shoppingDialogVisible by rememberSaveable { mutableStateOf(false) }
    var purchaseItem by remember { mutableStateOf<ShoppingItemEntity?>(null) }
    var clearAllConfirm by rememberSaveable { mutableStateOf(false) }

    var activeTimers by remember { mutableStateOf<List<RunningCookingTimer>>(emptyList()) }
    var ringingTimerLabel by remember { mutableStateOf<String?>(null) }
    val lastTimerNotificationRefreshMillis = remember { longArrayOf(0L) }

    val snackbarHostState = remember { SnackbarHostState() }

    val scannerPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scannerActive = true
        else viewModel.reportMessage("Autorizza la fotocamera per usare lo scanner, oppure inserisci il codice a mano.")
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.setNotifications(true)
            activeTimers.minByOrNull { it.endTimeMillis }?.let { next ->
                ExpiryReminderWorker.showLiveCookingTimerNotification(context, next.label, next.endTimeMillis, next.totalSeconds)
                lastTimerNotificationRefreshMillis[0] = System.currentTimeMillis()
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) viewModel.exportBackup(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) viewModel.importBackup(uri)
    }

    LaunchedEffect(activeTimers) {
        while (activeTimers.isNotEmpty()) {
            val currentMillis = System.currentTimeMillis()
            val expiredTimers = activeTimers.filter { it.endTimeMillis <= currentMillis }
            if (expiredTimers.isNotEmpty()) {
                expiredTimers.forEach { done ->
                    runCatching { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
                    cancelBackgroundTimerWork(context, done.id)
                    ringingTimerLabel = done.label
                }
                activeTimers = activeTimers.filter { it.endTimeMillis > currentMillis }
                if (activeTimers.isEmpty()) {
                    ExpiryReminderWorker.cancelLiveCookingTimerNotification(context)
                } else {
                    val next = activeTimers.minByOrNull { it.endTimeMillis }!!
                    ExpiryReminderWorker.showLiveCookingTimerNotification(context, next.label, next.endTimeMillis, next.totalSeconds)
                    lastTimerNotificationRefreshMillis[0] = currentMillis
                }
            }
            if (activeTimers.isNotEmpty() && currentMillis - lastTimerNotificationRefreshMillis[0] >= 10_000L) {
                val next = activeTimers.minByOrNull { it.endTimeMillis }
                if (next != null) {
                    ExpiryReminderWorker.showLiveCookingTimerNotification(context, next.label, next.endTimeMillis, next.totalSeconds)
                    lastTimerNotificationRefreshMillis[0] = currentMillis
                }
            }
            delay(1_000L)
        }
    }

    val startCookingTimer: (String, String, Int) -> Unit = { stepKey, label, seconds ->
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val id = "${stepKey}_${System.currentTimeMillis()}"
        val endTime = System.currentTimeMillis() + seconds * 1_000L
        val newTimer = RunningCookingTimer(
            id = id,
            stepKey = stepKey,
            label = label,
            totalSeconds = seconds,
            endTimeMillis = endTime,
        )
        activeTimers = activeTimers.filterNot { it.stepKey == stepKey } + newTimer
        val primaryTimer = activeTimers.minByOrNull { it.endTimeMillis } ?: newTimer
        ExpiryReminderWorker.showLiveCookingTimerNotification(
            context,
            primaryTimer.label,
            primaryTimer.endTimeMillis,
            primaryTimer.totalSeconds,
        )
        lastTimerNotificationRefreshMillis[0] = System.currentTimeMillis()
        scheduleBackgroundTimerWork(context, id, label, seconds)
    }

    val stopCookingTimer: (String) -> Unit = { timerId ->
        activeTimers = activeTimers.filterNot { it.id == timerId }
        cancelBackgroundTimerWork(context, timerId)
        AlarmSoundController.stopAlarm()
        if (activeTimers.isEmpty()) {
            ExpiryReminderWorker.cancelLiveCookingTimerNotification(context)
        } else {
            val next = activeTimers.minByOrNull { it.endTimeMillis }!!
            ExpiryReminderWorker.showLiveCookingTimerNotification(context, next.label, next.endTimeMillis, next.totalSeconds)
            lastTimerNotificationRefreshMillis[0] = System.currentTimeMillis()
        }
    }

    val addMinuteToTimer: (String) -> Unit = { timerId ->
        val now = System.currentTimeMillis()
        activeTimers = activeTimers.map { timer ->
            if (timer.id == timerId) {
                val updatedEnd = timer.endTimeMillis + 60_000L
                val remainingSec = ((updatedEnd - now) / 1000L).toInt().coerceAtLeast(1)
                scheduleBackgroundTimerWork(context, timer.id, timer.label, remainingSec)
                timer.copy(endTimeMillis = updatedEnd, totalSeconds = timer.totalSeconds + 60)
            } else timer
        }
        activeTimers.minByOrNull { it.endTimeMillis }?.let { primaryTimer ->
            ExpiryReminderWorker.showLiveCookingTimerNotification(
                context,
                primaryTimer.label,
                primaryTimer.endTimeMillis,
                primaryTimer.totalSeconds,
            )
            lastTimerNotificationRefreshMillis[0] = now
        }
    }

    BackHandler(enabled = !onboardingVisible && (scannerActive || selectedRecipeId != null || selectedTab != MainTab.HOME)) {
        when {
            scannerActive -> scannerActive = false
            selectedRecipeId != null -> selectedRecipeId = null
            selectedTab != MainTab.HOME -> selectedTab = MainTab.HOME
        }
    }

    LaunchedEffect(message) {
        val currentMsg = message
        if (!currentMsg.isNullOrBlank()) {
            val isImportant = currentMsg.contains("backup", ignoreCase = true) ||
                    currentMsg.contains("errore", ignoreCase = true) ||
                    currentMsg.contains("autorizz", ignoreCase = true) ||
                    currentMsg.contains("eliminat", ignoreCase = true) ||
                    currentMsg.contains("lista della spesa aggiornata", ignoreCase = true) ||
                    currentMsg.contains("nessun ingrediente", ignoreCase = true)
            if (extraPrefs.showActionSnackbars || isImportant) {
                snackbarHostState.showSnackbar(currentMsg)
            }
            viewModel.dismissMessage()
        }
    }

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = MaterialTheme.typography,
    ) {
        CompositionLocalProvider(
            LocalStrings provides strings,
            LocalExtraPrefs provides extraPrefs,
            LocalRecipeLocalizer provides recipeLocalizer,
            LocalTabletLayout provides useTabletLayout,
        ) {
            if (onboardingVisible) {
                OnboardingScreen(
                    langCode = strings.langCode,
                    onFinish = finishOnboarding,
                )
                return@CompositionLocalProvider
            }

            if (scannerActive) {
                CameraScannerScreen(
                    onCodeDetected = { value ->
                        if (extraPrefs.vibrateOnScan) {
                            runCatching { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) }
                        }
                        scannerActive = false
                        scannedBarcode = value
                        lookupResult = null
                        addLotVisible = true
                        if (extraPrefs.autoLookupBarcode) {
                            viewModel.lookupBarcode(value) { lookupResult = it }
                        }
                    },
                    onClose = { scannerActive = false },
                )
                return@CompositionLocalProvider
            }

            val selectedRecipe = recipes.firstOrNull { it.id == selectedRecipeId }

            Scaffold(
                contentWindowInsets = if (useTabletLayout) WindowInsets.safeDrawing else WindowInsets.statusBars,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Surface(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1B2E25),
                            contentColor = Color.White,
                            shadowElevation = 6.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF7EE19E), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = data.visuals.message,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    Column {
                        if (
                            activity != null &&
                            extraPrefs.voluntarySupportAdsEnabled &&
                            adsState.adsOptedIn &&
                            adsState.consentFlowComplete &&
                            adsState.canRequestAds &&
                            adsState.sdkReady
                        ) {
                            ZingBannerAd(activity)
                        }
                        if (!useTabletLayout) {
                            NavigationBar(windowInsets = WindowInsets.navigationBars) {
                                MainTab.values().forEach { tab ->
                                    val (icon, label) = when (tab) {
                                        MainTab.HOME -> Icons.Outlined.Home to strings.tabHome
                                        MainTab.PANTRY -> Icons.Outlined.Inventory2 to strings.tabPantry
                                        MainTab.RECIPES -> Icons.Outlined.RestaurantMenu to strings.tabRecipes
                                        MainTab.SHOPPING -> Icons.Outlined.ShoppingBasket to strings.tabShopping
                                        MainTab.SETTINGS -> Icons.Outlined.Settings to strings.tabSettings
                                    }
                                    NavigationBarItem(
                                        selected = selectedTab == tab,
                                        onClick = {
                                            selectedTab = tab
                                            if (tab != MainTab.RECIPES) selectedRecipeId = null
                                        },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        alwaysShowLabel = true,
                                    )
                                }
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Row(Modifier.fillMaxSize().padding(innerPadding)) {
                    if (useTabletLayout) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            header = {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).size(48.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Kitchen, contentDescription = "Zing", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                    }
                                }
                            },
                        ) {
                            MainTab.values().forEach { tab ->
                                val (icon, label) = when (tab) {
                                    MainTab.HOME -> Icons.Outlined.Home to strings.tabHome
                                    MainTab.PANTRY -> Icons.Outlined.Inventory2 to strings.tabPantry
                                    MainTab.RECIPES -> Icons.Outlined.RestaurantMenu to strings.tabRecipes
                                    MainTab.SHOPPING -> Icons.Outlined.ShoppingBasket to strings.tabShopping
                                    MainTab.SETTINGS -> Icons.Outlined.Settings to strings.tabSettings
                                }
                                NavigationRailItem(
                                    selected = selectedTab == tab,
                                    onClick = {
                                        selectedTab = tab
                                        if (tab != MainTab.RECIPES) selectedRecipeId = null
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                                    alwaysShowLabel = true,
                                )
                            }
                        }
                        Box(
                            Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                        )
                    }
                    Box(
                        Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(Modifier.widthIn(max = 1500.dp).fillMaxSize()) {
                            when (selectedTab) {
                                MainTab.HOME -> HomeScreen(
                                    lots = lots,
                                    recipes = recipes,
                                    shopping = shopping,
                                    events = events,
                                    habits = habitSuggestions,
                                    settings = settings,
                                    streakDays = usageStreak.days,
                                    updateAvailable = updateAvailable,
                                    onStartUpdate = {
                                        val manager = appUpdateManager
                                        val info = appUpdateInfo
                                        if (manager != null && info != null) {
                                            val flexible = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                                            val immediate = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                                            updateAvailable = false
                                            runCatching {
                                                when {
                                                    info.isUpdateTypeAllowed(flexible) ->
                                                        manager.startUpdateFlowForResult(info, updateFlowLauncher, flexible)
                                                    info.isUpdateTypeAllowed(immediate) ->
                                                        manager.startUpdateFlowForResult(info, updateFlowLauncher, immediate)
                                                }
                                            }.onFailure { checkForUpdate() }
                                        }
                                    },
                                    onDismissUpdate = {
                                        updateBannerDismissed = true
                                        updatePrefs.edit().putInt("dismissed_version_code", updateAvailableVersion).apply()
                                        updateAvailable = false
                                    },
                                    onAddLot = { editingLot = null; scannedBarcode = ""; lookupResult = null; addLotVisible = true },
                                    onScan = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) scannerActive = true
                                        else scannerPermission.launch(Manifest.permission.CAMERA)
                                    },
                                    onOpenPantry = { selectedTab = MainTab.PANTRY },
                                    onOpenRecipes = { selectedTab = MainTab.RECIPES },
                                    onOpenRecipe = { selectedTab = MainTab.RECIPES; selectedRecipeId = it.id },
                                )
                                MainTab.PANTRY -> PantryScreen(
                                    lots = lots,
                                    onAdd = { editingLot = null; scannedBarcode = ""; lookupResult = null; addLotVisible = true },
                                    onScan = {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) scannerActive = true
                                        else scannerPermission.launch(Manifest.permission.CAMERA)
                                    },
                                    onEdit = { editingLot = it; scannedBarcode = it.barcode; lookupResult = null; addLotVisible = true },
                                    onDelete = { deleteLot = it },
                                    onAdjust = viewModel::adjustLot,
                                )
                                MainTab.RECIPES -> {
                                    if (useDualPaneRecipes) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                                        ) {
                                            Box(Modifier.weight(0.44f).fillMaxHeight()) {
                                                RecipesScreen(
                                                    recipes = recipes,
                                                    lots = lots,
                                                    settings = settings,
                                                    onAddPersonal = { editingRecipe = null; recipeEditorVisible = true },
                                                    onOpenRecipe = { selectedRecipeId = it.id },
                                                    onFavorite = { recipe, favorite -> viewModel.setFavorite(recipe.id, favorite) },
                                                    onAddNeeds = { recipe -> viewModel.addRecipeNeeds(listOf(recipe.id to settings.householdSize)) },
                                                    onEdit = { editingRecipe = it; recipeEditorVisible = true },
                                                    onDuplicate = { viewModel.duplicateRecipe(it.id) },
                                                    onDelete = { deleteRecipe = it },
                                                )
                                            }
                                            Box(
                                                Modifier.width(1.dp).fillMaxHeight()
                                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                                            )
                                            Box(Modifier.weight(0.56f).fillMaxHeight()) {
                                                if (selectedRecipe == null) {
                                                    Box(
                                                        Modifier.fillMaxSize().padding(28.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        EmptyCard(
                                                            icon = Icons.Outlined.RestaurantMenu,
                                                            title = strings.recipesTitle,
                                                            text = uiCopy("Seleziona una ricetta per visualizzare qui ingredienti e procedimento.", strings.langCode),
                                                        )
                                                    }
                                                } else {
                                                    val recipeCookEvents = remember(events, selectedRecipe.id) {
                                                        events.filter { it.type == "cooked" && it.recipeId == selectedRecipe.id }
                                                    }
                                                    RecipeDetailScreen(
                                                        recipe = selectedRecipe,
                                                        lots = lots,
                                                        settings = settings,
                                                        activeTimers = activeTimers,
                                                        cookedCount = recipeCookEvents.size,
                                                        lastCookedAtMillis = recipeCookEvents.maxOfOrNull { it.occurredAtMillis },
                                                        onBack = { selectedRecipeId = null },
                                                        onFavorite = { viewModel.setFavorite(selectedRecipe.id, !selectedRecipe.favorite) },
                                                        onAddNeeds = { servings -> viewModel.addRecipeNeeds(listOf(selectedRecipe.id to servings)) },
                                                        onCook = { servings, callback -> viewModel.consumeRecipe(selectedRecipe.id, servings, callback) },
                                                        onIgnore = { viewModel.ignoreRecipeSuggestion(selectedRecipe.id); selectedRecipeId = null },
                                                        onStartTimer = startCookingTimer,
                                                        onAddMinuteTimer = addMinuteToTimer,
                                                        onStopTimer = stopCookingTimer,
                                                        onUpdatePhoto = { newUri ->
                                                            viewModel.saveRecipe(selectedRecipe.copy(imageUri = newUri, updatedAtMillis = System.currentTimeMillis()))
                                                        },
                                                        onUpdateInstructions = { newInstructions ->
                                                            viewModel.saveRecipe(selectedRecipe.copy(instructions = newInstructions, updatedAtMillis = System.currentTimeMillis()))
                                                        },
                                                        onEdit = { editingRecipe = selectedRecipe; recipeEditorVisible = true },
                                                        onDuplicate = { viewModel.duplicateRecipe(selectedRecipe.id) },
                                                        onDelete = if (selectedRecipe.isSeed) null else ({ deleteRecipe = selectedRecipe }),
                                                    )
                                                }
                                            }
                                        }
                                    } else if (selectedRecipe == null) {
                                        RecipesScreen(
                                            recipes = recipes,
                                            lots = lots,
                                            settings = settings,
                                            onAddPersonal = { editingRecipe = null; recipeEditorVisible = true },
                                            onOpenRecipe = { selectedRecipeId = it.id },
                                            onFavorite = { recipe, favorite -> viewModel.setFavorite(recipe.id, favorite) },
                                            onAddNeeds = { recipe -> viewModel.addRecipeNeeds(listOf(recipe.id to settings.householdSize)) },
                                            onEdit = { editingRecipe = it; recipeEditorVisible = true },
                                            onDuplicate = { viewModel.duplicateRecipe(it.id) },
                                            onDelete = { deleteRecipe = it },
                                        )
                                    } else {
                                        val recipeCookEvents = remember(events, selectedRecipe.id) {
                                            events.filter { it.type == "cooked" && it.recipeId == selectedRecipe.id }
                                        }
                                        RecipeDetailScreen(
                                            recipe = selectedRecipe,
                                            lots = lots,
                                            settings = settings,
                                            activeTimers = activeTimers,
                                            cookedCount = recipeCookEvents.size,
                                            lastCookedAtMillis = recipeCookEvents.maxOfOrNull { it.occurredAtMillis },
                                            onBack = { selectedRecipeId = null },
                                            onFavorite = { viewModel.setFavorite(selectedRecipe.id, !selectedRecipe.favorite) },
                                            onAddNeeds = { servings -> viewModel.addRecipeNeeds(listOf(selectedRecipe.id to servings)) },
                                            onCook = { servings, callback -> viewModel.consumeRecipe(selectedRecipe.id, servings, callback) },
                                            onIgnore = { viewModel.ignoreRecipeSuggestion(selectedRecipe.id); selectedRecipeId = null },
                                            onStartTimer = startCookingTimer,
                                            onAddMinuteTimer = addMinuteToTimer,
                                            onStopTimer = stopCookingTimer,
                                            onUpdatePhoto = { newUri ->
                                                viewModel.saveRecipe(selectedRecipe.copy(imageUri = newUri, updatedAtMillis = System.currentTimeMillis()))
                                            },
                                            onUpdateInstructions = { newInstructions ->
                                                viewModel.saveRecipe(selectedRecipe.copy(instructions = newInstructions, updatedAtMillis = System.currentTimeMillis()))
                                            },
                                            onEdit = { editingRecipe = selectedRecipe; recipeEditorVisible = true },
                                            onDuplicate = { viewModel.duplicateRecipe(selectedRecipe.id) },
                                            onDelete = if (selectedRecipe.isSeed) null else ({ deleteRecipe = selectedRecipe }),
                                        )
                                    }
                                }
                                MainTab.SHOPPING -> ShoppingScreen(
                                    items = shopping,
                                    onAdd = { shoppingDialogVisible = true },
                                    onToggle = viewModel::toggleShopping,
                                    onDelete = viewModel::deleteShopping,
                                    onIgnore = viewModel::ignoreShopping,
                                    onRegisterPurchase = { purchaseItem = it },
                                    onRefresh = { viewModel.refreshHabits() },
                                    onGenerate = { viewModel.generateSmartShoppingList() },
                                    onClearChecked = {
                                        shopping.filter { it.isChecked }.forEach { viewModel.deleteShopping(it.id) }
                                    },
                                )
                                MainTab.SETTINGS -> SettingsScreen(
                                    settings = settings,
                                    extraPrefs = extraPrefs,
                                    lots = lots,
                                    events = events,
                                    proPrice = playProPrice,
                                    proProductAvailable = playProProductAvailable,
                                    proBillingStatus = playProStatus,
                                    adsPrivacyOptionsRequired = adsState.privacyOptionsRequired,
                                    onManageAdsPrivacy = {
                                        activity?.let { adsManager.showPrivacyOptions(it) }
                                    },
                                    onBuyPro = {
                                        val activity = context as? Activity
                                        if (activity != null) billingManager.launchPurchase(activity)
                                        else Toast.makeText(context, uiCopy("Impossibile aprire Google Play da questa schermata.", strings.langCode), Toast.LENGTH_LONG).show()
                                    },
                                    onRedeemPromoCode = { code -> billingManager.openPromoRedemption(context, code) },
                                    onRedeemZingCode = { code ->
                                        workScope.launch {
                                            val result = proCodeClient.redeem(code)
                                            customCodeStatusKey = result.messageKey
                                            if (result.active) {
                                                customCodeProActive = true
                                                customCodeActivatedAt = result.activatedAtMillis
                                            }
                                        }
                                    },
                                    zingProCodeStatus = customCodeStatusKey,
                                    proActivationTimeMillis = customCodeActivatedAt ?: googlePlayActivationTime,
                                    onRefreshProPurchases = { billingManager.refreshPurchases() },
                                    onUpdateExtraPrefs = updateExtraPrefs,
                                    onDarkTheme = viewModel::setDarkTheme,
                                    onNotificationToggle = { enabled ->
                                        if (!enabled) viewModel.setNotifications(false)
                                        else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else viewModel.setNotifications(true)
                                    },
                                    onExpiryDays = viewModel::setExpiryDays,
                                    onHouseholdSize = viewModel::setHouseholdSize,
                                    onDietary = viewModel::setDietaryPreferences,
                                    onExcluded = viewModel::setExcludedIngredients,
                                    onExport = { exportLauncher.launch("Zing-backup.json") },
                                    onImport = { importLauncher.launch(arrayOf("application/json", "text/json", "*/*")) },
                                    onClearAll = { clearAllConfirm = true },
                                    onShowOnboarding = { onboardingVisible = true },
                                )
                            }

                            if (activeTimers.isNotEmpty()) {
                                DynamicIslandTimerOverlay(
                                    timers = activeTimers,
                                    langCode = strings.langCode,
                                    onAddMinute = addMinuteToTimer,
                                    onStop = stopCookingTimer,
                                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                                )
                            }

                            if (busy) {
                                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
                            }
                        }
                    }
                }
            }

            if (ringingTimerLabel != null) {
                AlertDialog(
                    onDismissRequest = {
                        AlarmSoundController.stopAlarm()
                        ringingTimerLabel = null
                    },
                    title = { Text(uiCopy("⏰ Tempo scaduto!", strings.langCode)) },
                    text = { Text(uiCopy("Il passaggio {label} è pronto.", strings.langCode).replace("{label}", ringingTimerLabel.orEmpty())) },
                    confirmButton = {
                        Button(
                            onClick = {
                                AlarmSoundController.stopAlarm()
                                ringingTimerLabel = null
                            },
                        ) {
                            Text(uiCopy("🔇 Ferma suoneria", strings.langCode))
                        }
                    },
                )
            }

            if (addLotVisible) {
                LotEditorDialog(
                    existing = editingLot,
                    initialBarcode = scannedBarcode,
                    lookupResult = lookupResult,
                    onDismiss = { addLotVisible = false; editingLot = null; scannedBarcode = ""; lookupResult = null },
                    onLookup = { barcode -> viewModel.lookupBarcode(barcode) { lookupResult = it } },
                    onSave = { item ->
                        if (editingLot == null) viewModel.addLot(item) else viewModel.updateLot(item)
                        addLotVisible = false; editingLot = null; scannedBarcode = ""; lookupResult = null
                    },
                )
            }

            if (deleteLot != null) {
                RemovalReasonDialog(
                    lotName = deleteLot?.name.orEmpty(),
                    onDismiss = { deleteLot = null },
                    onConfirm = { reason ->
                        deleteLot?.let { viewModel.removeLot(it.id, reason) }
                        deleteLot = null
                    },
                )
            }

            if (recipeEditorVisible) {
                RecipeEditorDialog(
                    existing = editingRecipe,
                    onDismiss = { recipeEditorVisible = false; editingRecipe = null },
                    onSave = { viewModel.saveRecipe(it); recipeEditorVisible = false; editingRecipe = null },
                )
            }

            if (deleteRecipe != null) {
                ConfirmDialog(
                    title = uiCopy("Eliminare la ricetta?", strings.langCode),
                    message = uiCopy("\"{name}\" sarà eliminata dal dispositivo.", strings.langCode).replace("{name}", deleteRecipe?.name.orEmpty()),
                    confirmLabel = strings.confirmBtn,
                    onDismiss = { deleteRecipe = null },
                    onConfirm = { deleteRecipe?.let { viewModel.deleteRecipe(it.id) }; selectedRecipeId = null; deleteRecipe = null },
                )
            }

            if (shoppingDialogVisible) {
                ShoppingItemDialog(
                    onDismiss = { shoppingDialogVisible = false },
                    onSave = { viewModel.addShoppingItem(it); shoppingDialogVisible = false },
                )
            }

            if (purchaseItem != null) {
                RegisterPurchaseDialog(
                    item = purchaseItem!!,
                    onDismiss = { purchaseItem = null },
                    onConfirm = { quantity, unit, location, expiry ->
                        viewModel.registerPurchase(purchaseItem!!.id, quantity, unit, location, expiry)
                        purchaseItem = null
                    },
                )
            }

            if (clearAllConfirm) {
                ConfirmDialog(
                    title = strings.deleteAllBtn,
                    message = "Saranno cancellati inventario, ricette personali, lista della spesa, cronologia e preferenze. La libreria iniziale di ricette rimarrà disponibile.",
                    confirmLabel = strings.confirmBtn,
                    onDismiss = { clearAllConfirm = false },
                    onConfirm = { viewModel.deleteAllData(); clearAllConfirm = false; selectedRecipeId = null },
                )
            }
        }
    }
}

@Composable
private fun DynamicIslandTimerOverlay(
    timers: List<RunningCookingTimer>,
    langCode: String,
    onAddMinute: (String) -> Unit,
    onStop: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timers) {
        while (timers.isNotEmpty()) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val primaryTimer = timers.minByOrNull { it.endTimeMillis } ?: return
    val primaryRemaining = ((primaryTimer.endTimeMillis - nowMillis) / 1000L).coerceAtLeast(0L)

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { expanded = !expanded },
        color = Color(0xFF111714),
        contentColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = Color(0xFF28A766).copy(alpha = 0.25f), modifier = Modifier.size(28.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Timer, contentDescription = null, tint = Color(0xFF7EE19E), modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = primaryTimer.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF22352B)) {
                    Text(
                        text = formatCountdown(primaryRemaining),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7EE19E),
                    )
                }
                if (timers.size > 1) {
                    Text("+${timers.size - 1}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    timers.forEach { t ->
                        val rem = ((t.endTimeMillis - nowMillis) / 1000L).coerceAtLeast(0L)
                        val progress = (rem.toFloat() / t.totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "${t.label} · ${formatCountdown(rem)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { onAddMinute(t.id) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                        Text("+1m", color = Color(0xFF7EE19E), style = MaterialTheme.typography.labelMedium)
                                    }
                                    TextButton(onClick = { onStop(t.id) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                        Text(uiCopy("Stop", langCode), color = Color(0xFFFF8A80), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                                color = Color(0xFF7EE19E),
                                trackColor = Color.White.copy(alpha = 0.18f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    lots: List<FoodLotEntity>,
    recipes: List<RecipeEntity>,
    shopping: List<ShoppingItemEntity>,
    events: List<FoodEventEntity>,
    habits: List<HabitSuggestion>,
    settings: AppSettings,
    streakDays: Int,
    updateAvailable: Boolean,
    onStartUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onAddLot: () -> Unit,
    onScan: () -> Unit,
    onOpenPantry: () -> Unit,
    onOpenRecipes: () -> Unit,
    onOpenRecipe: (RecipeEntity) -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val today = todayLocalDate()
    val expiring = lots.filter { lot ->
        lot.quantity > 0 && (
                isDueOrExpiringDate(lot.expiryDateMillis, settings.daysBeforeExpiry, today) ||
                        isOpenedTooLong(lot, extraPrefs.openedAlertDays)
                ) && (extraPrefs.showExpiredInHome || !isExpiredDate(lot.expiryDateMillis, today))
    }.sortedBy { it.expiryDateMillis ?: Long.MAX_VALUE }

    val expiredCount = expiring.count { isExpiredDate(it.expiryDateMillis, today) }
    val lowStock = lots.filter { it.minQuantity > 0 && it.quantity <= it.minQuantity }
    val lotAmounts = lots.map { LotAmount(it.name, it.quantity, it.unit, it.expiryDateMillis) }
    val historySince = System.currentTimeMillis() - 120L * 24 * 60 * 60 * 1000
    val cookedFrequency = events.filter { it.type == "cooked" && it.recipeId != null && it.occurredAtMillis >= historySince }
        .groupingBy { it.recipeId!! }.eachCount()
    val ignoredRecently = events.filter { it.type == "ignored_recipe" && it.recipeId != null && it.occurredAtMillis >= System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
        .mapNotNull { it.recipeId }.toSet()

    val expiringIngredientNames = remember(lots) {
        lots.filter { isExpiringDate(it.expiryDateMillis, 3, today) && it.quantity > 0 }.map { it.name }
    }
    val suggested = recipes.filterNot {
        hasExcludedIngredient(it, settings.excludedIngredients) ||
                hasDietaryMismatch(it, settings.dietaryPreferences) ||
                it.id in ignoredRecently
    }.map { it to recipeCoverage(it, settings.householdSize, lotAmounts) }
        .map { pair ->
            val recipe = pair.first
            val coverage = pair.second
            val expBoost = expiryBoost(recipe, expiringIngredientNames)
            val baseScore = coverage.percentage +
                    (if (recipe.favorite) 12 else 0) +
                    recipe.rating.coerceIn(0, 5) * 2 +
                    (cookedFrequency[recipe.id] ?: 0).coerceAtMost(5) * 2
            val score = when (extraPrefs.recipePriority) {
                "expiry" -> baseScore + expBoost * 3
                "fast" -> baseScore + expBoost - (recipe.prepMinutes + recipe.cookMinutes)
                "coverage" -> coverage.percentage * 2 + expBoost
                else -> baseScore + expBoost
            }
            Triple(recipe, coverage, score)
        }
        .sortedWith(
            compareByDescending<Triple<RecipeEntity, RecipeCoverage, Int>> { it.third }
                .thenBy { it.first.prepMinutes + it.first.cookMinutes },
        )
        .take(3)
        .map { it.first to it.second }

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (LocalTabletLayout.current && maxWidth >= 900.dp) {
            HomeTabletDashboard(
                lots = lots,
                shopping = shopping,
                events = events,
                habits = habits,
                settings = settings,
                streakDays = streakDays,
                updateAvailable = updateAvailable,
                onStartUpdate = onStartUpdate,
                onDismissUpdate = onDismissUpdate,
                expiring = expiring,
                expiredCount = expiredCount,
                lowStock = lowStock,
                suggested = suggested,
                onAddLot = onAddLot,
                onScan = onScan,
                onOpenPantry = onOpenPantry,
                onOpenRecipes = onOpenRecipes,
                onOpenRecipe = onOpenRecipe,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (updateAvailable) {
                    item { UpdateAvailableCard(langCode = strings.langCode, onUpdate = onStartUpdate, onDismiss = onDismissUpdate) }
                }
                item { UsageStreakCard(days = streakDays, langCode = strings.langCode) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(strings.greeting, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (extraPrefs.supporterProUnlocked) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text("PRO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(strings.homeHero, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Kitchen, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(strings.metricItems, lots.count { it.quantity > 0.0 }.toString(), strings.metricItemsSub, Icons.Outlined.Inventory2, Modifier.weight(1f))
                        MetricCard(
                            strings.metricExpiring,
                            expiring.size.toString(),
                            when {
                                expiredCount > 0 -> "$expiredCount ${strings.expired.lowercase(Locale.ROOT)}"
                                settings.daysBeforeExpiry == 0 -> strings.today
                                else -> strings.inDays(settings.daysBeforeExpiry)
                            },
                            Icons.Outlined.CalendarMonth,
                            Modifier.weight(1f),
                            alert = expiring.isNotEmpty(),
                        )
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f), modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(strings.quickAddTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(strings.quickAddSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onScan, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(7.dp))
                                    Text(strings.btnScan)
                                }
                                OutlinedButton(onClick = onAddLot, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.btnAdd)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        SectionHeading(strings.consumeFirstTitle, strings.consumeFirstSub)
                        TextButton(onClick = onOpenPantry) { Text(strings.seeAll); Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(16.dp)) }
                    }
                    if (expiring.isEmpty()) {
                        EmptyCard(icon = Icons.Outlined.CheckCircle, title = strings.noUrgencyTitle, text = strings.noUrgencyText)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            expiring.take(3).forEach { lot -> ExpiryRow(lot = lot) }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        SectionHeading(strings.ideasTodayTitle, strings.ideasTodaySub)
                        TextButton(onClick = onOpenRecipes) { Text(strings.tabRecipes); Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(16.dp)) }
                    }
                    if (suggested.isEmpty()) {
                        EmptyCard(icon = Icons.Outlined.Restaurant, title = strings.emptyRecipesTitle, text = strings.emptyRecipesText, actionLabel = strings.openRecipes, onAction = onOpenRecipes)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            suggested.forEach { (recipe, coverage) ->
                                CompactRecipeSuggestion(recipe, coverage.percentage, expiryBoost(recipe, lots), onClick = { onOpenRecipe(recipe) })
                            }
                        }
                    }
                }
                if (lowStock.isNotEmpty() || shopping.any { !it.isChecked }) {
                    item {
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.watchOutTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                }
                                if (lowStock.isNotEmpty()) Text("${lowStock.size} · ${strings.filterLowStock}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (shopping.any { !it.isChecked }) Text(strings.toBuyCount(shopping.count { !it.isChecked }), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (habits.isNotEmpty()) {
                    item {
                        SectionHeading(strings.routineTitle, strings.routineSub)
                        habits.take(2).forEach { suggestion ->
                            Card(Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(18.dp)) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(suggestion.name, fontWeight = FontWeight.SemiBold)
                                        Text(suggestion.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTabletDashboard(
    lots: List<FoodLotEntity>,
    shopping: List<ShoppingItemEntity>,
    events: List<FoodEventEntity>,
    habits: List<HabitSuggestion>,
    settings: AppSettings,
    streakDays: Int,
    updateAvailable: Boolean,
    onStartUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    expiring: List<FoodLotEntity>,
    expiredCount: Int,
    lowStock: List<FoodLotEntity>,
    suggested: List<Pair<RecipeEntity, RecipeCoverage>>,
    onAddLot: () -> Unit,
    onScan: () -> Unit,
    onOpenPantry: () -> Unit,
    onOpenRecipes: () -> Unit,
    onOpenRecipe: (RecipeEntity) -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (updateAvailable) UpdateAvailableCard(langCode = strings.langCode, onUpdate = onStartUpdate, onDismiss = onDismissUpdate)
        UsageStreakCard(days = streakDays, langCode = strings.langCode)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(0.9f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.greeting, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    if (extraPrefs.supporterProUnlocked) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("PRO", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(strings.homeHero, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.weight(1.1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(strings.metricItems, lots.count { it.quantity > 0.0 }.toString(), strings.metricItemsSub, Icons.Outlined.Inventory2, Modifier.weight(1f))
                MetricCard(
                    strings.metricExpiring,
                    expiring.size.toString(),
                    when {
                        expiredCount > 0 -> "$expiredCount ${strings.expired.lowercase(Locale.ROOT)}"
                        settings.daysBeforeExpiry == 0 -> strings.today
                        else -> strings.inDays(settings.daysBeforeExpiry)
                    },
                    Icons.Outlined.CalendarMonth,
                    Modifier.weight(1f),
                    alert = expiring.isNotEmpty(),
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.quickAddTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(strings.quickAddSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onScan) {
                    Icon(Icons.Outlined.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(strings.btnScan)
                }
                Button(onClick = onAddLot) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.btnAdd)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    SectionHeading(strings.consumeFirstTitle, strings.consumeFirstSub)
                    TextButton(onClick = onOpenPantry) { Text(strings.seeAll); Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(16.dp)) }
                }
                if (expiring.isEmpty()) {
                    EmptyCard(icon = Icons.Outlined.CheckCircle, title = strings.noUrgencyTitle, text = strings.noUrgencyText)
                } else {
                    expiring.take(4).forEach { lot -> ExpiryRow(lot = lot) }
                }
                if (lowStock.isNotEmpty() || shopping.any { !it.isChecked }) {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(strings.watchOutTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            if (lowStock.isNotEmpty()) Text("${lowStock.size} · ${strings.filterLowStock}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (shopping.any { !it.isChecked }) Text(strings.toBuyCount(shopping.count { !it.isChecked }), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (habits.isNotEmpty()) {
                    SectionHeading(strings.routineTitle, strings.routineSub)
                    habits.take(3).forEach { suggestion ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(suggestion.name, fontWeight = FontWeight.SemiBold)
                                    Text(suggestion.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    SectionHeading(strings.ideasTodayTitle, strings.ideasTodaySub)
                    TextButton(onClick = onOpenRecipes) { Text(strings.tabRecipes); Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(16.dp)) }
                }
                if (suggested.isEmpty()) {
                    EmptyCard(icon = Icons.Outlined.Restaurant, title = strings.emptyRecipesTitle, text = strings.emptyRecipesText, actionLabel = strings.openRecipes, onAction = onOpenRecipes)
                } else {
                    suggested.forEach { (recipe, coverage) ->
                        CompactRecipeSuggestion(recipe, coverage.percentage, expiryBoost(recipe, lots), onClick = { onOpenRecipe(recipe) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, alert: Boolean = false) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = if (alert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = if (alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ExpiryRow(lot: FoodLotEntity) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val days = lot.expiryDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).toInt() }
    val openedAlert = isOpenedTooLong(lot, extraPrefs.openedAlertDays)
    val label = when {
        days != null && days < 0 -> strings.expired
        days == 0 -> strings.today
        days == 1 -> strings.tomorrow
        days != null -> strings.inDays(days)
        openedAlert -> "Aperto"
        else -> strings.withoutDate
    }
    val alert = (days != null && days <= 2) || openedAlert
    Surface(shape = RoundedCornerShape(17.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = if (alert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.CalendarMonth, null, tint = if (alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(lot.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatQuantityForSystem(lot.quantity, lot.unit, extraPrefs.measurementSystem)} · ${translateLocation(lot.location, strings.langCode)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = if (alert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                Text(label, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = if (alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CompactRecipeSuggestion(recipe: RecipeEntity, percentage: Int, expiringBoost: Int, onClick: () -> Unit) {
    val strings = LocalStrings.current
    val localized = requireNotNull(LocalRecipeLocalizer.current).text(recipe, strings.langCode)
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            RecipeThumb(recipe, Modifier.size(70.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(localized.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${recipe.prepMinutes + recipe.cookMinutes} min · ${translateCategory(recipe.category, strings.langCode)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(7.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(strings.availablePct(percentage), modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    if (expiringBoost > 0) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp)) }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (actionLabel != null && onAction != null) Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
private fun RecipeThumb(recipe: RecipeEntity, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val localizedName = LocalRecipeLocalizer.current?.text(recipe, strings.langCode)?.name ?: recipe.name
    if (!recipe.imageUri.isNullOrBlank()) {
        AsyncImage(
            model = recipe.imageUri,
            contentDescription = localizedName,
            modifier = modifier.clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(15.dp),
            color = when (recipe.category) {
                "Dolci", "Colazioni" -> Color(0xFFF4E0CF)
                "Zuppe", "Contorni" -> Color(0xFFDCEED9)
                "Insalate" -> Color(0xFFD4EBDD)
                else -> MaterialTheme.colorScheme.primaryContainer
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun recipeCoverage(recipe: RecipeEntity, servings: Int, lots: List<LotAmount>): RecipeCoverage {
    val raw = parseRecipeIngredients(recipe.ingredientsJson)
    val adjusted = raw.map { it.copy(quantity = QuantityMath.scale(it.quantity, recipe.servings, servings)) }
    return QuantityMath.coverage(adjusted, lots)
}

private fun parseRecipeIngredients(json: String): List<IngredientAmount> = try {
    val array = org.json.JSONArray(json)
    (0 until array.length()).map { index ->
        val item = array.getJSONObject(index)
        QuantityMath.normalizeIngredient(
            IngredientAmount(
                name = item.optString("name", "Ingrediente"),
                quantity = if (item.isNull("quantity")) null else item.optDouble("quantity"),
                unit = item.optString("unit", ""),
                note = item.optString("note", "").takeIf { it.isNotBlank() },
            ),
        )
    }
} catch (_: Exception) {
    emptyList()
}

private fun hasExcludedIngredient(recipe: RecipeEntity, excluded: String): Boolean {
    val words = excluded.split(',', ';', '\n').map { QuantityMath.normalizeName(it) }.filter { it.isNotBlank() }
    if (words.isEmpty()) return false
    return parseRecipeIngredients(recipe.ingredientsJson).any { ing ->
        words.any { word -> QuantityMath.namesMatch(ing.name, word) || QuantityMath.normalizeName(ing.name).contains(word) }
    }
}

private fun hasDietaryMismatch(recipe: RecipeEntity, preferences: String): Boolean {
    val prefs = QuantityMath.normalizeName(preferences)
    if (prefs.isBlank()) return false
    val names = parseRecipeIngredients(recipe.ingredientsJson).map { QuantityMath.normalizeName(it.name) }
    fun containsAny(vararg terms: String): Boolean = names.any { name -> terms.any { term -> name.contains(QuantityMath.normalizeName(term)) } }
    val vegetarian = listOf("vegetariano", "vegetariana", "vegetarian").any { prefs.contains(it) }
    val vegan = listOf("vegano", "vegana", "vegan").any { prefs.contains(it) }
    val glutenFree = listOf("senza glutine", "gluten free", "celiachia", "celiaco", "celiaca").any { prefs.contains(it) }
    val lactoseFree = listOf("senza lattosio", "lactose free", "intolleranza al lattosio").any { prefs.contains(it) }
    if (vegan && containsAny("carne", "pollo", "manzo", "vitello", "maiale", "prosciutto", "pancetta", "salsiccia", "speck", "salame", "tonno", "salmone", "acciuga", "acciughe", "pesce", "gamberi", "frutti di mare", "latte", "mozzarella", "ricotta", "parmigiano", "formaggio", "burro", "yogurt", "panna", "uovo", "uova", "miele")) return true
    if (vegetarian && !vegan && containsAny("carne", "pollo", "manzo", "vitello", "maiale", "prosciutto", "pancetta", "salsiccia", "speck", "salame", "tonno", "salmone", "acciuga", "acciughe", "pesce", "gamberi", "frutti di mare")) return true
    if (glutenFree && containsAny("pasta", "spaghetti", "penne", "farina", "pangrattato", "pane", "cous cous", "farro", "orzo", "seitan", "focaccia", "pizza", "biscotti")) return true
    if (lactoseFree && containsAny("latte", "mozzarella", "ricotta", "burro", "panna", "yogurt", "mascarpone", "formaggio fresco", "besciamella")) return true
    return false
}

private fun expiryBoost(recipe: RecipeEntity, lots: List<FoodLotEntity>): Int {
    val today = todayLocalDate()
    val expiringNames = lots.filter { isExpiringDate(it.expiryDateMillis, 3, today) && it.quantity > 0 }.map { it.name }
    return expiryBoost(recipe, expiringNames)
}

private fun expiryBoost(recipe: RecipeEntity, expiringNames: Collection<String>): Int {
    if (expiringNames.isEmpty()) return 0
    val ingredients = parseRecipeIngredients(recipe.ingredientsJson)
    return ingredients.count { item -> expiringNames.any { QuantityMath.namesMatch(it, item.name) } } * 7
}

@Composable
private fun PantryScreen(
    lots: List<FoodLotEntity>,
    onAdd: () -> Unit,
    onScan: () -> Unit,
    onEdit: (FoodLotEntity) -> Unit,
    onDelete: (FoodLotEntity) -> Unit,
    onAdjust: (Long, Double) -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("ALL") }
    var sort by rememberSaveable { mutableStateOf("EXPIRY") }
    var visible by remember { mutableStateOf<List<FoodLotEntity>>(emptyList()) }
    var isComputing by remember { mutableStateOf(true) }

    LaunchedEffect(lots, search, filter, sort, extraPrefs.openedAlertDays) {
        isComputing = true
        val computed = withContext(Dispatchers.Default) {
            val today = todayLocalDate()
            lots.asSequence()
                .filter { search.isBlank() || it.name.contains(search, true) || it.brand.contains(search, true) || it.category.contains(search, true) || it.barcode.contains(search) }
                .filter { item ->
                    when (filter) {
                        "EXPIRING" -> (isExpiringDate(item.expiryDateMillis, 3, today) || isOpenedTooLong(item, extraPrefs.openedAlertDays)) && item.quantity > 0
                        "EXPIRED" -> isExpiredDate(item.expiryDateMillis, today) && item.quantity > 0
                        "LOW" -> item.minQuantity > 0 && item.quantity <= item.minQuantity
                        else -> true
                    }
                }
                .let { sequence ->
                    when (sort) {
                        "NAME" -> sequence.sortedBy { it.name.lowercase(Locale.ROOT) }
                        "QTY" -> sequence.sortedBy { it.quantity }
                        "LOC" -> sequence.sortedWith(compareBy<FoodLotEntity> { it.location }.thenBy { it.name })
                        else -> sequence.sortedWith(compareBy<FoodLotEntity> { it.expiryDateMillis ?: Long.MAX_VALUE }.thenBy { it.name })
                    }
                }.toList()
        }
        visible = computed
        isComputing = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.pantryTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(strings.pantrySub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onScan) { Icon(Icons.Outlined.QrCodeScanner, contentDescription = strings.btnScan, tint = MaterialTheme.colorScheme.primary) }
                FloatingActionButton(onClick = onAdd, modifier = Modifier.size(48.dp), shape = CircleShape) { Icon(Icons.Outlined.Add, contentDescription = strings.btnAdd) }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(strings.searchPantryHint) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FilterPill(strings.filterAll, selected = filter == "ALL", onClick = { filter = "ALL" })
                FilterPill(strings.filterExpiring, selected = filter == "EXPIRING", onClick = { filter = "EXPIRING" })
                FilterPill(strings.filterExpired, selected = filter == "EXPIRED", onClick = { filter = "EXPIRED" })
                FilterPill(strings.filterLowStock, selected = filter == "LOW", onClick = { filter = "LOW" })
            }
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${visible.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(strings.sortBy, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(7.dp))
                val sortOptions = listOf(
                    "EXPIRY" to strings.sortExpiry,
                    "NAME" to strings.sortName,
                    "QTY" to strings.sortQty,
                    "LOC" to strings.sortLocation,
                )
                val currentSortLabel = sortOptions.firstOrNull { it.first == sort }?.second ?: strings.sortExpiry
                SortSelector(
                    selected = currentSortLabel,
                    options = sortOptions.map { it.second },
                    onSelected = { label -> sort = sortOptions.firstOrNull { it.second == label }?.first ?: "EXPIRY" },
                )
            }
        }
        if (isComputing) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        if (isComputing && visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Text(uiCopy("Caricamento dispensa…", strings.langCode), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                EmptyCard(
                    icon = if (search.isNotBlank() || filter != "ALL") Icons.Outlined.Search else Icons.Outlined.Inventory2,
                    title = if (search.isNotBlank() || filter != "ALL") strings.noResults else strings.emptyPantryTitle,
                    text = strings.emptyPantryText,
                    actionLabel = if (search.isNotBlank() || filter != "ALL") null else strings.btnAdd,
                    onAction = if (search.isNotBlank() || filter != "ALL") null else onAdd,
                )
            }
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val useGrid = LocalTabletLayout.current && maxWidth >= 680.dp
                if (useGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 320.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(visible, key = { it.id }) { lot ->
                            FoodLotCard(
                                lot = lot,
                                onEdit = { onEdit(lot) },
                                onDelete = { onDelete(lot) },
                                onDecrease = { onAdjust(lot.id, -1.0) },
                                onIncrease = { onAdjust(lot.id, 1.0) },
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(visible, key = { it.id }) { lot ->
                            FoodLotCard(
                                lot = lot,
                                onEdit = { onEdit(lot) },
                                onDelete = { onDelete(lot) },
                                onDecrease = { onAdjust(lot.id, -1.0) },
                                onIncrease = { onAdjust(lot.id, 1.0) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(30.dp)).clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = if (selected) 0.dp else 1.dp,
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SortSelector(selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButtonCompact(label = selected, onClick = { expanded = true })
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
private fun TextButtonCompact(label: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Icon(Icons.Outlined.Tune, null, modifier = Modifier.padding(start = 5.dp).size(14.dp))
        }
    }
}

@Composable
private fun FoodLotCard(
    lot: FoodLotEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val now = System.currentTimeMillis()
    val days = lot.expiryDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it - now).toInt() }
    val openedAlert = isOpenedTooLong(lot, extraPrefs.openedAlertDays)
    val expireAlert = (days != null && days <= 3) || openedAlert
    val expired = days != null && days < 0
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = if (expireAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(categoryIcon(lot.category), contentDescription = null, tint = if (expireAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(lot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val metadata = listOf(lot.brand, translateCategory(lot.category, strings.langCode), translateLocation(lot.location, strings.langCode))
                        .filter { it.isNotBlank() }.distinct().joinToString(" · ")
                    Text(metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = uiCopy("Modifica", strings.langCode), modifier = Modifier.size(19.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, contentDescription = uiCopy("Elimina", strings.langCode), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.availableLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatQuantityForSystem(lot.quantity, lot.unit, extraPrefs.measurementSystem), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(31.dp).clickable(onClick = onDecrease)) {
                        Box(contentAlignment = Alignment.Center) { Text("−", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(31.dp).clickable(onClick = onIncrease)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp)) }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(strings.expiryLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        when {
                            lot.expiryDateMillis == null -> "—"
                            expired -> strings.expired
                            days == 0 -> strings.today
                            days == 1 -> strings.tomorrow
                            else -> formatUserDate(lot.expiryDateMillis, extraPrefs.dateFormat)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (expireAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (lot.notes.isNotBlank()) {
                Text(lot.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun categoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    "Frutta e verdura" -> Icons.Outlined.LocalDining
    "Latticini" -> Icons.Outlined.Kitchen
    "Carne e pesce" -> Icons.Outlined.Restaurant
    "Pasta e cereali" -> Icons.Outlined.Inventory2
    "Bevande" -> Icons.Outlined.ShoppingBasket
    else -> Icons.Outlined.Kitchen
}

@Composable
private fun RecipesScreen(
    recipes: List<RecipeEntity>,
    lots: List<FoodLotEntity>,
    settings: AppSettings,
    onAddPersonal: () -> Unit,
    onOpenRecipe: (RecipeEntity) -> Unit,
    onFavorite: (RecipeEntity, Boolean) -> Unit,
    onAddNeeds: (RecipeEntity) -> Unit,
    onEdit: (RecipeEntity) -> Unit,
    onDuplicate: (RecipeEntity) -> Unit,
    onDelete: (RecipeEntity) -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val recipeLocalizer = requireNotNull(LocalRecipeLocalizer.current)
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Tutte") }
    var topMode by rememberSaveable { mutableStateOf("ALL") }
    var showOnlineRecipeFinder by rememberSaveable { mutableStateOf(false) }
    val categories = listOf("Tutte", "Primi", "Secondi", "Piatti unici", "Insalate", "Zuppe", "Contorni", "Colazioni", "Snack", "Dolci")
    val myRecipesCount = remember(recipes) { recipes.count { !it.isSeed } }
    var visible by remember { mutableStateOf<List<Pair<RecipeEntity, RecipeCoverage>>>(emptyList()) }
    var isComputing by remember { mutableStateOf(true) }

    LaunchedEffect(
        recipes,
        lots,
        settings.householdSize,
        settings.excludedIngredients,
        settings.dietaryPreferences,
        search,
        category,
        topMode,
        extraPrefs.recipePriority,
        strings.langCode,
    ) {
        isComputing = true
        val computed = withContext(Dispatchers.Default) {
            val lotAmounts = lots.map { LotAmount(it.name, it.quantity, it.unit, it.expiryDateMillis) }
            val today = todayLocalDate()
            val expiringNames = lots.filter { isExpiringDate(it.expiryDateMillis, 3, today) && it.quantity > 0 }.map { it.name }
            recipes.asSequence()
                .filter { topMode == "MY" || (!hasExcludedIngredient(it, settings.excludedIngredients) && !hasDietaryMismatch(it, settings.dietaryPreferences)) }
                .filter {
                    if (search.isBlank()) true else {
                        val shown = recipeLocalizer.text(it, strings.langCode)
                        it.name.contains(search, true) || it.description.contains(search, true) || it.category.contains(search, true) ||
                                shown.name.contains(search, true) || shown.description.contains(search, true) ||
                                runCatching { parseRecipeIngredients(it.ingredientsJson).any { ingredient -> recipeLocalizer.ingredientName(ingredient.name, strings.langCode).contains(search, true) } }.getOrDefault(false)
                    }
                }
                .filter { category == "Tutte" || it.category == category }
                .filter {
                    when (topMode) {
                        "MY" -> !it.isSeed
                        "FAV" -> it.favorite
                        else -> true
                    }
                }
                .map { recipe -> recipe to recipeCoverage(recipe, settings.householdSize, lotAmounts) }
                .filter { topMode != "READY" || it.second.fullyAvailable }
                .map { pair ->
                    val recipe = pair.first
                    val coverage = pair.second
                    val boost = expiryBoost(recipe, expiringNames)
                    val score = when (extraPrefs.recipePriority) {
                        "expiry" -> coverage.percentage + boost * 3
                        "fast" -> coverage.percentage + boost - (recipe.prepMinutes + recipe.cookMinutes)
                        "coverage" -> coverage.percentage * 2
                        else -> coverage.percentage + boost
                    }
                    Triple(recipe, coverage, score)
                }
                .sortedWith(
                    compareByDescending<Triple<RecipeEntity, RecipeCoverage, Int>> { it.third }
                        .thenBy { it.first.prepMinutes + it.first.cookMinutes },
                )
                .map { it.first to it.second }
                .toList()
        }
        visible = computed
        isComputing = false
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.recipesTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(strings.recipesSub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FloatingActionButton(onClick = onAddPersonal, modifier = Modifier.size(48.dp), shape = CircleShape) { Icon(Icons.Outlined.Add, contentDescription = strings.btnAdd) }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(strings.searchRecipeHint) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
                shape = RoundedCornerShape(17.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill(strings.filterAll, selected = topMode == "ALL", onClick = { topMode = "ALL" })
                FilterPill("${strings.filterMyRecipes} ($myRecipesCount)", selected = topMode == "MY", onClick = { topMode = "MY" })
                FilterPill(strings.filterReady, selected = topMode == "READY", onClick = { topMode = "READY" })
                FilterPill(strings.filterFavorites, selected = topMode == "FAV", onClick = { topMode = "FAV" })
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                categories.forEach { name ->
                    FilterPill(translateCategory(name, strings.langCode), selected = category == name, onClick = { category = name })
                }
            }
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { showOnlineRecipeFinder = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(uiCopy("Trova ricette online con la mia dispensa", strings.langCode))
            }
        }
        if (isComputing) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        if (isComputing && visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Text(uiCopy("Caricamento ricette…", strings.langCode), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                EmptyCard(
                    icon = Icons.Outlined.RestaurantMenu,
                    title = if (topMode == "MY") "Nessuna ricetta personale" else strings.noResults,
                    text = if (topMode == "MY") "Qui compariranno solo le ricette create o duplicate da te." else strings.emptyRecipesText,
                    actionLabel = if (topMode == "MY") "Crea la tua ricetta" else if (topMode != "ALL") strings.filterAll else null,
                    onAction = if (topMode == "MY") onAddPersonal else if (topMode != "ALL") ({ topMode = "ALL" }) else null,
                )
            }
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val useGrid = LocalTabletLayout.current && maxWidth >= 680.dp
                if (useGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 340.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(visible, key = { it.first.id }) { (recipe, coverage) ->
                            RecipeListCard(
                                recipe = recipe,
                                percentage = coverage.percentage,
                                missingCount = coverage.missing.size,
                                onOpen = { onOpenRecipe(recipe) },
                                onFavorite = { onFavorite(recipe, !recipe.favorite) },
                                onAddNeeds = { onAddNeeds(recipe) },
                                onEdit = { onEdit(recipe) },
                                onDuplicate = { onDuplicate(recipe) },
                                onDelete = { onDelete(recipe) },
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(visible, key = { it.first.id }) { (recipe, coverage) ->
                            RecipeListCard(
                                recipe = recipe,
                                percentage = coverage.percentage,
                                missingCount = coverage.missing.size,
                                onOpen = { onOpenRecipe(recipe) },
                                onFavorite = { onFavorite(recipe, !recipe.favorite) },
                                onAddNeeds = { onAddNeeds(recipe) },
                                onEdit = { onEdit(recipe) },
                                onDuplicate = { onDuplicate(recipe) },
                                onDelete = { onDelete(recipe) },
                            )
                        }
                    }
                }
            }
        }
    }
    if (showOnlineRecipeFinder) {
        OnlineRecipeFinderDialog(lots = lots, onDismiss = { showOnlineRecipeFinder = false })
    }
}

@Composable
private fun RecipeListCard(
    recipe: RecipeEntity,
    percentage: Int,
    missingCount: Int,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onAddNeeds: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalStrings.current
    val localized = requireNotNull(LocalRecipeLocalizer.current).text(recipe, strings.langCode)
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmAddNeeds by rememberSaveable(recipe.id) { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column {
            Row(Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                RecipeThumb(recipe, Modifier.size(92.dp))
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(localized.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        IconButton(onClick = onFavorite, modifier = Modifier.size(35.dp)) {
                            Icon(if (recipe.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (recipe.favorite) Color(0xFFD85D67) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(localized.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Timer, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("${recipe.prepMinutes + recipe.cookMinutes} min", style = MaterialTheme.typography.labelSmall)
                        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(translateCategory(recipe.category, strings.langCode), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!recipe.isSeed) {
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(strings.personalBadge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.availableLabel, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$percentage%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(progress = { percentage / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (missingCount == 0) strings.allIngredientsReady else strings.missingIngredientsLabel(missingCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (missingCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        androidx.compose.material3.DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            androidx.compose.material3.DropdownMenuItem(text = { Text("+ ${strings.tabShopping}") }, onClick = { confirmAddNeeds = true; menuExpanded = false })
                            androidx.compose.material3.DropdownMenuItem(text = { Text(uiCopy("Duplica in Le mie ricette", strings.langCode)) }, onClick = { onDuplicate(); menuExpanded = false })
                            androidx.compose.material3.DropdownMenuItem(text = { Text(uiCopy("Modifica", strings.langCode)) }, onClick = { onEdit(); menuExpanded = false })
                            if (!recipe.isSeed) {
                                androidx.compose.material3.DropdownMenuItem(text = { Text(uiCopy("Elimina", strings.langCode), color = MaterialTheme.colorScheme.error) }, onClick = { onDelete(); menuExpanded = false })
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmAddNeeds) {
        AlertDialog(
            onDismissRequest = { confirmAddNeeds = false },
            title = { Text(uiCopy("Aggiungere alla lista della spesa?", strings.langCode)) },
            text = {
                Text(
                    uiCopy("Verranno aggiunti gli ingredienti mancanti e quantificabili di \"{recipe}\". Le quantità non specificate come q.b. non possono essere calcolate automaticamente.", strings.langCode)
                        .replace("{recipe}", localized.name),
                )
            },
            confirmButton = {
                Button(onClick = {
                    onAddNeeds()
                    confirmAddNeeds = false
                }) { Text(strings.confirmBtn) }
            },
            dismissButton = {
                TextButton(onClick = { confirmAddNeeds = false }) { Text(strings.cancelBtn) }
            },
        )
    }
}

@Composable
private fun RecipeDetailScreen(
    recipe: RecipeEntity,
    lots: List<FoodLotEntity>,
    settings: AppSettings,
    activeTimers: List<RunningCookingTimer>,
    cookedCount: Int,
    lastCookedAtMillis: Long?,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onAddNeeds: (Int) -> Unit,
    onCook: (Int, (List<String>) -> Unit) -> Unit,
    onIgnore: () -> Unit,
    onStartTimer: (String, String, Int) -> Unit,
    onAddMinuteTimer: (String) -> Unit,
    onStopTimer: (String) -> Unit,
    onUpdatePhoto: (String?) -> Unit,
    onUpdateInstructions: (String) -> Unit,
    onEdit: (() -> Unit)?,
    onDuplicate: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val recipeLocalizer = requireNotNull(LocalRecipeLocalizer.current)
    val localized = recipeLocalizer.text(recipe, strings.langCode)
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeTimers) {
        while (activeTimers.isNotEmpty()) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    DisposableEffect(extraPrefs.keepScreenOnCooking) {
        val previous = view.keepScreenOn
        if (extraPrefs.keepScreenOnCooking) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = previous
        }
    }

    var servings by rememberSaveable(recipe.id) { mutableIntStateOf(recipe.servings.coerceAtLeast(1)) }
    var confirmCook by rememberSaveable(recipe.id) { mutableStateOf(false) }
    var editTimesDialog by rememberSaveable(recipe.id) { mutableStateOf(false) }
    var addNeedsDialog by rememberSaveable(recipe.id) { mutableStateOf(false) }
    val canonicalIngredients = parseRecipeIngredients(recipe.ingredientsJson).map { it.copy(quantity = QuantityMath.scale(it.quantity, recipe.servings, servings)) }
    val ingredients = canonicalIngredients.map { ingredient ->
        if (recipe.isSeed) ingredient.copy(name = recipeLocalizer.ingredientName(ingredient.name, strings.langCode)) else ingredient
    }
    val lotAmounts = lots.map { LotAmount(it.name, it.quantity, it.unit, it.expiryDateMillis) }
    // Compare the stored canonical Italian names, not their translated presentation labels.
    val coverage = QuantityMath.coverage(canonicalIngredients, lotAmounts)

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            onUpdatePhoto(uri.toString())
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val savedUri = saveBitmapToInternalStorage(context, bitmap)
            if (savedUri != null) onUpdatePhoto(savedUri)
        }
    }

    val steps = remember(localized.instructions) {
        localized.instructions.lines().map { it.trim() }.filter { it.isNotBlank() }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) }
            Text(localized.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    val shareText = buildString {
                        append("🍽 ${localized.name} ($servings ${uiCopy("porzioni", strings.langCode)})\n\n")
                        append("${uiCopy("Ingredienti", strings.langCode)}:\n")
                        ingredients.forEach { ingredient -> append("• ${ingredient.name}: ${formatQuantityForSystem(ingredient.quantity, ingredient.unit, extraPrefs.measurementSystem, ingredient.note)}\n") }
                        append("\n${uiCopy("Procedimento:", strings.langCode)}\n${localized.instructions}\n\n— Zing")
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, localized.name))
                },
            ) {
                Icon(Icons.Outlined.Share, contentDescription = uiCopy("Condividi ricetta", strings.langCode))
            }
            IconButton(onClick = onFavorite) { Icon(if (recipe.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (recipe.favorite) Color(0xFFD85D67) else MaterialTheme.colorScheme.onSurface) }
            if (onEdit != null) IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = null) }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                RecipeThumb(recipe, Modifier.fillMaxWidth().height(190.dp))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { galleryPicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                        Text(uiCopy("📷 Foto da Galleria", strings.langCode))
                    }
                    OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) {
                        Text(uiCopy("📸 Scatta Foto", strings.langCode))
                    }
                    if (!recipe.imageUri.isNullOrBlank()) {
                        IconButton(onClick = { onUpdatePhoto(null) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = uiCopy("Rimuovi foto", strings.langCode), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(localized.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(localized.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTag(Icons.Outlined.Timer, "${recipe.prepMinutes + recipe.cookMinutes} min")
                    InfoTag(Icons.Outlined.Restaurant, translateCategory(recipe.category, strings.langCode))
                    InfoTag(Icons.Outlined.Inventory2, "$servings ${uiCopy("porzioni", strings.langCode)}")
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(uiCopy("Cronologia preparazioni", strings.langCode), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            if (cookedCount == 0) uiCopy("Ancora nessuna preparazione registrata.", strings.langCode)
                            else uiCopy("Preparata {count} volte", strings.langCode).replace("{count}", cookedCount.toString()),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (lastCookedAtMillis != null) {
                            Text(
                                uiCopy("Ultima preparazione: {date}", strings.langCode).replace("{date}", formatUserDate(lastCookedAtMillis, extraPrefs.dateFormat)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(uiCopy("Porzioni", strings.langCode), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { if (servings > 1) servings-- }, enabled = servings > 1) { Text("−", style = MaterialTheme.typography.titleLarge) }
                            Text(servings.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (servings < 30) servings++ }, enabled = servings < 30) { Icon(Icons.Outlined.Add, contentDescription = null) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.availableLabel, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            Text("${coverage.percentage}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        LinearProgressIndicator(progress = { coverage.percentage / 100f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape))
                    }
                }
            }
            item {
                SectionHeading(uiCopy("Ingredienti", strings.langCode), uiCopy("Ricalcolati per {servings} porzioni", strings.langCode).replace("{servings}", servings.toString()))
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(14.dp)) {
                        ingredients.forEachIndexed { index, ingredient ->
                            val row = coverage.details.getOrNull(index)
                            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(ingredient.name, fontWeight = FontWeight.Medium)
                                    if (ingredient.note != null && ingredient.note != "q.b." && !QuantityMath.isRangeNote(ingredient.note)) {
                                        Text(ingredient.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatQuantityForSystem(ingredient.quantity, ingredient.unit, extraPrefs.measurementSystem, ingredient.note), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    if (row != null && ingredient.quantity != null) {
                                        Text(
                                            when {
                                                row.ratio >= 0.999 -> uiCopy("Disponibile", strings.langCode)
                                                row.missing != null -> uiCopy("Mancano {quantity}", strings.langCode).replace("{quantity}", formatQuantityForSystem(row.missing, ingredient.unit, extraPrefs.measurementSystem))
                                                else -> uiCopy("Da acquistare", strings.langCode)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (row.ratio >= 0.999) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                        )
                                    }
                                }
                            }
                            if (index < ingredients.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { addNeedsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.ShoppingCart, null)
                    Spacer(Modifier.width(8.dp))
                    Text("+ ${strings.tabShopping}")
                }
            }
            item {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeading(uiCopy("Procedimento passo-passo", strings.langCode), uiCopy("Timer sincronizzato con Zing e con l’indicatore di sistema, se supportato", strings.langCode))
                    if (onEdit != null) {
                        FilledTonalButton(
                            onClick = { editTimesDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(uiCopy("Modifica tempi", strings.langCode), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    steps.forEachIndexed { idx, stepText ->
                        val stepKey = "recipe_${recipe.id}_step_$idx"
                        val timersForStep = remember(stepText) { extractStepDurations(stepText) }
                        val stepTitle = stepText.substringBefore(':').takeIf { it.length in 3..42 && stepText.contains(':') } ?: uiCopy("Passo {number}", strings.langCode).replace("{number}", (idx + 1).toString())
                        val runningTimer = activeTimers.firstOrNull { it.stepKey == stepKey }
                        val remainingSec = runningTimer?.let { ((it.endTimeMillis - nowMillis) / 1000L).coerceAtLeast(0L) }

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (runningTimer != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (runningTimer != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${idx + 1}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (runningTimer != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (runningTimer != null && remainingSec != null) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                            ) {
                                                Text(
                                                    text = "⏱ ${uiCopy("Tempo rimanente: {time}", strings.langCode).replace("{time}", formatCountdown(remainingSec))}",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            }
                                        }
                                        Text(stepText, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }

                                if (runningTimer != null && remainingSec != null) {
                                    val progress = (remainingSec.toFloat() / runningTimer.totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Outlined.Timer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        uiCopy("In cottura: {time}", strings.langCode).replace("{time}", formatCountdown(remainingSec)),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    FilledTonalButton(
                                                        onClick = { onAddMinuteTimer(runningTimer.id) },
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    ) {
                                                        Text("+1 min", style = MaterialTheme.typography.labelMedium)
                                                    }
                                                    OutlinedButton(
                                                        onClick = { onStopTimer(runningTimer.id) },
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    ) {
                                                        Text(uiCopy("Stop", strings.langCode), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                                                    }
                                                }
                                            }
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            )
                                        }
                                    }
                                }

                                if (timersForStep.isNotEmpty()) {
                                    Row(
                                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        timersForStep.forEach { option ->
                                            FilledTonalButton(
                                                onClick = { onStartTimer(stepKey, stepTitle, option.seconds) },
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(if (runningTimer != null) uiCopy("Riavvia {label}", strings.langCode).replace("{label}", option.label) else uiCopy("Avvia {label}", strings.langCode).replace("{label}", option.label))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                uiCopy("Esempio illustrativo", strings.langCode),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                uiCopy("Prendi il primo passaggio: leggi la riga, prepara ciò che serve e, se compare un tempo tra parentesi o nel testo, premi il pulsante del timer per avviarlo. Adatta i minuti al tuo fornello.", strings.langCode),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                Button(onClick = { confirmCook = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                    Icon(Icons.Outlined.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text(uiCopy("Ho preparato questa ricetta", strings.langCode))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDuplicate, modifier = Modifier.weight(1f)) { Text(uiCopy("Duplica in Le mie ricette", strings.langCode)) }
                    if (onDelete != null) OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text(uiCopy("Elimina", strings.langCode), color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onIgnore, modifier = Modifier.fillMaxWidth()) { Text(uiCopy("Ignora per 30 giorni", strings.langCode)) }
            }
        }
    }

    if (addNeedsDialog) {
        AlertDialog(
            onDismissRequest = { addNeedsDialog = false },
            title = { Text(uiCopy("Aggiungere alla lista della spesa?", strings.langCode)) },
            text = {
                Text(
                    uiCopy(
                        "Verranno aggiunti gli ingredienti mancanti e quantificabili di {servings} porzioni. Gli ingredienti già presenti nella lista verranno aggiornati senza duplicati.",
                        strings.langCode,
                    ).replace("{servings}", servings.toString()),
                )
            },
            confirmButton = {
                Button(onClick = {
                    onAddNeeds(servings)
                    addNeedsDialog = false
                }) { Text(strings.confirmBtn) }
            },
            dismissButton = {
                TextButton(onClick = { addNeedsDialog = false }) { Text(strings.cancelBtn) }
            },
        )
    }

    if (editTimesDialog) {
        EditStepTimesDialog(
            recipe = recipe,
            onDismiss = { editTimesDialog = false },
            onSave = { newInstructions ->
                onUpdateInstructions(newInstructions)
                editTimesDialog = false
            },
        )
    }

    if (confirmCook) {
        AlertDialog(
            onDismissRequest = { confirmCook = false },
            title = { Text(uiCopy("Aggiornare la dispensa?", strings.langCode)) },
            text = { Text(uiCopy("Verranno sottratte le quantità disponibili per {servings} porzioni.", strings.langCode).replace("{servings}", servings.toString())) },
            confirmButton = { Button(onClick = { onCook(servings) {}; confirmCook = false }) { Text(strings.confirmBtn) } },
            dismissButton = { TextButton(onClick = { confirmCook = false }) { Text(strings.cancelBtn) } },
        )
    }
}

@Composable
private fun EditStepTimesDialog(
    recipe: RecipeEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val localized = requireNotNull(LocalRecipeLocalizer.current).text(recipe, strings.langCode)
    val originalSteps = remember(recipe.id, strings.langCode, localized.instructions) {
        localized.instructions
            .lines()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
    }

    val initialMinutes = remember(recipe.id) {
        originalSteps.mapIndexed { index, step ->
            index to (extractStepDurations(step).firstOrNull()?.seconds?.div(60) ?: -1)
        }.toMap()
    }
    var pendingMinutes by remember(recipe.id) { mutableStateOf(initialMinutes) }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 720.dp),
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    uiCopy("Modifica tempo", strings.langCode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    uiCopy("Imposta la durata dei timer. La descrizione dei passaggi non viene modificata: aggiorniamo solo le indicazioni di tempo già presenti.", strings.langCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                originalSteps.forEachIndexed { index, step ->
                    val currentValue = pendingMinutes[index] ?: -1
                    val hasTimer = currentValue > 0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        ),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                uiCopy("Passo {number}", strings.langCode).replace("{number}", (index + 1).toString()),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                step,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedTextField(
                                    value = if (hasTimer) currentValue.toString() else "",
                                    onValueChange = { input ->
                                        val filtered = input.filter(Char::isDigit).take(3)
                                        val minutes = filtered.toIntOrNull() ?: 0
                                        pendingMinutes = pendingMinutes.toMutableMap().also {
                                            it[index] = if (minutes <= 0) 0 else minutes
                                        }
                                        error = null
                                    },
                                    label = { Text(uiCopy("Minuti", strings.langCode)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedButton(
                                    onClick = {
                                        pendingMinutes = pendingMinutes.toMutableMap().also { it[index] = 0 }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    Text(uiCopy("Rimuovi", strings.langCode), maxLines = 1)
                                }
                            }
                            Text(
                                when {
                                    currentValue > 0 -> uiCopy("Timer: {minutes} min", strings.langCode).replace("{minutes}", currentValue.toString())
                                    currentValue == 0 -> uiCopy("Nessun timer per questo passaggio", strings.langCode)
                                    else -> uiCopy("Nessun timer rilevato", strings.langCode)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(strings.cancelBtn) }

                    Button(
                        onClick = {
                            val rebuilt = originalSteps.mapIndexed { index, step ->
                                val minutes = pendingMinutes[index] ?: -1
                                applyTimerToStep(step, minutes)
                            }
                            val newInstructions = rebuilt.joinToString("\n")
                            if (newInstructions.isBlank()) {
                                error = uiCopy("Il procedimento non può restare vuoto.", strings.langCode)
                            } else {
                                onSave(newInstructions)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(strings.saveBtn) }
                }
            }
        }
    }
}

@Composable
private fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(uiCopy("Valutazione", LocalStrings.current.langCode), fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { star ->
                IconButton(
                    onClick = {
                        onRatingChange(if (rating == star) 0 else star)
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "$star stelle",
                        tint = if (star <= rating) Color(0xFFD85D67) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            if (rating == 0) "Nessuna valutazione" else "$rating / 5",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoTag(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ShoppingScreen(
    items: List<ShoppingItemEntity>,
    onAdd: () -> Unit,
    onToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onIgnore: (Long) -> Unit,
    onRegisterPurchase: (ShoppingItemEntity) -> Unit,
    onRefresh: () -> Unit,
    onGenerate: () -> Unit = onRefresh,
    onClearChecked: () -> Unit = {},
) {
    val strings = LocalStrings.current
    val openCount = items.count { !it.isChecked }
    val checkedCount = items.count { it.isChecked }
    val grouped = items.groupBy { it.category.ifBlank { "Altro" } }.toSortedMap()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.shoppingTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(strings.shoppingSub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FloatingActionButton(onClick = onAdd, modifier = Modifier.size(48.dp), shape = CircleShape) {
                    Icon(Icons.Outlined.Add, contentDescription = strings.btnAdd)
                }
            }
            Spacer(Modifier.height(13.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f), modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.ShoppingBasket, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(strings.toBuyCount(openCount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(strings.smartShoppingSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onGenerate) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGenerate, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.btnGenerateSmart, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (checkedCount > 0) {
                    OutlinedButton(onClick = onClearChecked, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                EmptyCard(Icons.Outlined.ShoppingBasket, strings.emptyShoppingTitle, strings.emptyShoppingText, strings.btnAdd, onAdd)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                grouped.forEach { (category, categoryItems) ->
                    item(key = "heading-$category") {
                        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(translateCategory(category, strings.langCode), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(categoryItems.count { !it.isChecked }.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(categoryItems, key = { it.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggle = { onToggle(item.id, !item.isChecked) },
                            onDelete = { onDelete(item.id) },
                            onRegisterPurchase = { onRegisterPurchase(item) },
                            onIgnore = { onIgnore(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onRegisterPurchase: () -> Unit,
    onIgnore: () -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val recipeLocalizer = requireNotNull(LocalRecipeLocalizer.current)
    val displayedIngredientName = recipeLocalizer.ingredientName(item.name, strings.langCode)
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(5.dp))
                Column(Modifier.weight(1f)) {
                    Text(displayedIngredientName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(formatQuantityForSystem(item.quantity, item.unit, extraPrefs.measurementSystem), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.isChecked) {
                    Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(strings.doneLabel, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                } else if (!item.isManual) {
                    Surface(shape = RoundedCornerShape(9.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(strings.suggestedLabel, modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    androidx.compose.material3.DropdownMenuItem(text = { Text(strings.boughtAddInventory) }, onClick = { onRegisterPurchase(); expanded = false })
                    if (!item.isManual) {
                        androidx.compose.material3.DropdownMenuItem(text = { Text(uiCopy("Ignora", strings.langCode)) }, onClick = { onIgnore(); expanded = false })
                    }
                    androidx.compose.material3.DropdownMenuItem(text = { Text(uiCopy("Rimuovi", strings.langCode), color = MaterialTheme.colorScheme.error) }, onClick = { onDelete(); expanded = false })
                }
            }
            if (!item.isChecked) {
                Spacer(Modifier.height(5.dp))
                FilledTonalButton(
                    onClick = onRegisterPurchase,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(strings.boughtAddInventory)
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    extraPrefs: ZingExtraPrefs,
    lots: List<FoodLotEntity>,
    events: List<FoodEventEntity>,
    proPrice: String?,
    proProductAvailable: Boolean,
    proBillingStatus: String?,
    adsPrivacyOptionsRequired: Boolean,
    onManageAdsPrivacy: () -> Unit,
    onBuyPro: () -> Unit,
    onRedeemPromoCode: (String) -> Unit,
    onRedeemZingCode: (String) -> Unit,
    zingProCodeStatus: String?,
    proActivationTimeMillis: Long?,
    onRefreshProPurchases: () -> Unit,
    onUpdateExtraPrefs: (ZingExtraPrefs) -> Unit,
    onDarkTheme: (Boolean) -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onExpiryDays: (Int) -> Unit,
    onHouseholdSize: (Int) -> Unit,
    onDietary: (String) -> Unit,
    onExcluded: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit,
    onShowOnboarding: () -> Unit,
) {
    val strings = LocalStrings.current
    val onboardingCopy = remember(strings.langCode) { OnboardingCopy.forLanguage(strings.langCode) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var redeemCodeInput by rememberSaveable { mutableStateOf("") }
    var redeemCodeMessage by rememberSaveable { mutableStateOf("") }
    var dietary by rememberSaveable { mutableStateOf(settings.dietaryPreferences) }
    var excluded by rememberSaveable { mutableStateOf(settings.excludedIngredients) }
    var feedbackDialog by remember { mutableStateOf(false) }
    LaunchedEffect(settings.dietaryPreferences) { dietary = settings.dietaryPreferences }
    LaunchedEffect(settings.excludedIngredients) { excluded = settings.excludedIngredients }

    val purchases = events.count { it.type == "purchase" }
    val consumed = events.count { it.type == "consume" }
    val cooked = events.count { it.type == "cooked" }

    val languages = listOf(
        "system" to uiCopy("Automatico (sistema)", strings.langCode),
        "it" to "🇮🇹 Italiano",
        "en" to "🇺🇸 English (US)",
        "es" to "🇪🇸 Español",
        "fr" to "🇫🇷 Français",
        "de" to "🇩🇪 Deutsch",
        "pt" to "🇵🇹 Português",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(strings.settingsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(strings.settingsSub, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(onboardingCopy.reviewTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(onboardingCopy.reviewDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onShowOnboarding) { Text(onboardingCopy.reviewButton) }
                }
            }
        }

        item {
            SectionHeading(strings.secAppearance, strings.secAppearanceSub)
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Text(strings.languageLabel, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        languages.forEach { (code, label) ->
                            FilterPill(label, selected = extraPrefs.language == code, onClick = { onUpdateExtraPrefs(extraPrefs.copy(language = code)) })
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsSwitchRow(Icons.Outlined.Brightness4, strings.darkThemeTitle, strings.darkThemeSub, settings.darkTheme, onDarkTheme)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(strings.dateFormatLabel, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd").forEach { fmt ->
                            FilterPill(fmt, selected = extraPrefs.dateFormat == fmt, onClick = { onUpdateExtraPrefs(extraPrefs.copy(dateFormat = fmt)) })
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsSwitchRow(
                        icon = Icons.Outlined.CheckCircle,
                        title = uiCopy("Mostra conferme rapide in basso", strings.langCode),
                        description = uiCopy("Se disattivato, mostra solo avvisi importanti (backup, errori, fine timer) evitando pop-up a ogni tocco.", strings.langCode),
                        checked = extraPrefs.showActionSnackbars,
                        onChanged = { onUpdateExtraPrefs(extraPrefs.copy(showActionSnackbars = it)) },
                    )
                }
            }
        }

        item {
            SectionHeading(strings.secExpiry, strings.secExpirySub)
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    SettingsSwitchRow(Icons.Outlined.NotificationsActive, strings.notificationsTitle, strings.notificationsSub, settings.notificationsEnabled, onNotificationToggle)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsSwitchRow(
                        Icons.Outlined.CalendarMonth,
                        strings.notifyDayBeforeTitle,
                        strings.notifyDayBeforeSub,
                        extraPrefs.notifyDayBeforeExpiry,
                        { onUpdateExtraPrefs(extraPrefs.copy(notifyDayBeforeExpiry = it)) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(strings.expiryAdvanceTitle, fontWeight = FontWeight.Medium)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf(0, 1, 2, 3, 5, 7, 14).forEach { days ->
                            FilterPill(if (days == 0) strings.today else "$days d", settings.daysBeforeExpiry == days, { onExpiryDays(days) })
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(strings.openedAlertTitle, fontWeight = FontWeight.Medium)
                    Text(strings.openedAlertSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf(0, 2, 3, 5, 7).forEach { days ->
                            FilterPill(if (days == 0) "Off" else "$days d", extraPrefs.openedAlertDays == days, { onUpdateExtraPrefs(extraPrefs.copy(openedAlertDays = days)) })
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsSwitchRow(
                        Icons.Outlined.WarningAmber,
                        strings.showExpiredHomeTitle,
                        strings.showExpiredHomeSub,
                        extraPrefs.showExpiredInHome,
                        { onUpdateExtraPrefs(extraPrefs.copy(showExpiredInHome = it)) },
                    )
                }
            }
        }

        item {
            SectionHeading(strings.secDefaults, strings.secDefaultsSub)
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(uiCopy("Sistema di misurazione", strings.langCode), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterPill(
                            uiCopy("Metrico · g, kg, ml, l", strings.langCode),
                            selected = extraPrefs.measurementSystem == "metric",
                            onClick = { onUpdateExtraPrefs(extraPrefs.copy(measurementSystem = "metric", defaultUnit = if (extraPrefs.defaultUnit in setOf("oz", "lb", "fl oz", "cup")) "g" else extraPrefs.defaultUnit)) },
                        )
                        FilterPill(
                            uiCopy("Imperiale · oz, lb, fl oz, cup", strings.langCode),
                            selected = extraPrefs.measurementSystem == "imperial",
                            onClick = { onUpdateExtraPrefs(extraPrefs.copy(measurementSystem = "imperial", defaultUnit = if (extraPrefs.defaultUnit in setOf("g", "kg", "ml", "l")) "oz" else extraPrefs.defaultUnit)) },
                        )
                    }
                    Text(uiCopy("Le quantità vengono convertite nella visualizzazione quando l'unità è compatibile; i dati salvati non vengono riscritti.", strings.langCode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ChoiceField(
                            strings.defaultLocationLabel,
                            extraPrefs.defaultLocation,
                            listOf("Dispensa", "Frigorifero", "Congelatore", "Cantina"),
                            { onUpdateExtraPrefs(extraPrefs.copy(defaultLocation = it)) },
                            Modifier.weight(1f),
                        )
                        ChoiceField(
                            strings.defaultUnitLabel,
                            extraPrefs.defaultUnit,
                            if (extraPrefs.measurementSystem == "imperial") listOf("pz", "oz", "lb", "fl oz", "cup", "confezioni") else listOf("pz", "g", "kg", "ml", "l", "confezioni"),
                            { onUpdateExtraPrefs(extraPrefs.copy(defaultUnit = it)) },
                            Modifier.weight(1f),
                        )
                    }
                    ChoiceField(
                        strings.defaultMinStockLabel,
                        extraPrefs.defaultMinStock,
                        listOf("0", "1", "2", "3", "5"),
                        { onUpdateExtraPrefs(extraPrefs.copy(defaultMinStock = it)) },
                        Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            SectionHeading(strings.secScanner, strings.secScannerSub)
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    SettingsSwitchRow(
                        Icons.Outlined.QrCodeScanner,
                        strings.vibrateScanTitle,
                        strings.vibrateScanSub,
                        extraPrefs.vibrateOnScan,
                        { onUpdateExtraPrefs(extraPrefs.copy(vibrateOnScan = it)) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    SettingsSwitchRow(
                        Icons.Outlined.Search,
                        strings.autoLookupTitle,
                        strings.autoLookupSub,
                        extraPrefs.autoLookupBarcode,
                        { onUpdateExtraPrefs(extraPrefs.copy(autoLookupBarcode = it)) },
                    )
                }
            }
        }

        item {
            SectionHeading(strings.secCooking, strings.secCookingSub)
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PersonOutline, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(strings.householdTitle, fontWeight = FontWeight.Medium)
                            Text(strings.householdSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { onHouseholdSize((settings.householdSize - 1).coerceAtLeast(1)) }, enabled = settings.householdSize > 1) { Text("−") }
                        Text("${settings.householdSize}", modifier = Modifier.width(42.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        OutlinedButton(onClick = { onHouseholdSize((settings.householdSize + 1).coerceAtMost(20)) }, enabled = settings.householdSize < 20) { Icon(Icons.Outlined.Add, null) }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(strings.priorityLabel, fontWeight = FontWeight.Medium)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf(
                            "balanced" to "Bilanciata",
                            "expiry" to "Salva-scadenze",
                            "fast" to "Più veloci",
                            "coverage" to "Copertura %",
                        ).forEach { (key, label) ->
                            FilterPill(label, selected = extraPrefs.recipePriority == key, onClick = { onUpdateExtraPrefs(extraPrefs.copy(recipePriority = key)) })
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("Vegetariano", "Vegano", "Senza glutine", "Senza lattosio").forEach { tag ->
                            val active = dietary.contains(tag, ignoreCase = true)
                            FilterPill(
                                label = tag,
                                selected = active,
                                onClick = {
                                    val parts = dietary.split(',').map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
                                    if (active) parts.removeAll { it.equals(tag, ignoreCase = true) } else parts.add(tag)
                                    dietary = parts.joinToString(", ")
                                    onDietary(dietary)
                                },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = dietary, onValueChange = { dietary = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.dietaryLabel) }, minLines = 2, shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = excluded, onValueChange = { excluded = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text(strings.excludedLabel) }, minLines = 2, shape = RoundedCornerShape(14.dp),
                    )
                    Button(onClick = { onDietary(dietary); onExcluded(excluded) }, modifier = Modifier.fillMaxWidth()) { Text(strings.savePrefsBtn) }
                }
            }
        }

        item {
            NutritionProfileSettingsCard(
                prefs = extraPrefs,
                onSave = { updated -> onUpdateExtraPrefs(updated) },
                langCode = strings.langCode,
            )
        }

        item {
            SectionHeading(uiCopy("Supporta Zing", strings.langCode), uiCopy("Tutte le funzioni base restano gratuite. Lo sblocco Pro sosterrà lo sviluppo.", strings.langCode))
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                uiCopy("Zing Supporter Pro", strings.langCode),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                uiCopy("Include: 6 temi colore dinamici, schermo sempre acceso in cucina, riepilogo avanzato dispensa e nuove feature in arrivo.", strings.langCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                uiCopy("Prezzo", strings.langCode),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                proPrice ?: uiCopy("2,99 € · una tantum", strings.langCode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                uiCopy(
                                    when {
                                        extraPrefs.supporterProUnlocked -> "Attivo"
                                        proProductAvailable -> "Disponibile su Google Play"
                                        else -> "Prossimamente"
                                    },
                                    strings.langCode,
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }

                    Button(
                        onClick = onBuyPro,
                        enabled = proProductAvailable && !extraPrefs.supporterProUnlocked,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            when {
                                extraPrefs.supporterProUnlocked -> uiCopy("Zing Pro attivo", strings.langCode)
                                proProductAvailable -> uiCopy("Acquista", strings.langCode) + " · " + (proPrice ?: "2,99 €")
                                else -> uiCopy("Disponibile prossimamente", strings.langCode)
                            },
                        )
                    }
                    localizeBillingStatus(proBillingStatus, strings.langCode)?.let { status ->
                        Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (proProductAvailable || extraPrefs.supporterProUnlocked) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Text(
                            uiCopy("Riscatta codice promozionale Google Play", strings.langCode),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (extraPrefs.supporterProUnlocked) {
                            Text(
                                uiCopy("Zing Pro è attivo.", strings.langCode),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (proActivationTimeMillis != null) {
                                Text(
                                    uiCopy("Zing Pro attivato il", strings.langCode) + " " + formatProActivationDate(proActivationTimeMillis, strings.langCode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = redeemCodeInput,
                                onValueChange = { redeemCodeInput = it.trimStart().take(64); redeemCodeMessage = "" },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(uiCopy("Codice promozionale", strings.langCode)) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                            )
                            Button(
                                onClick = {
                                    onRedeemPromoCode(redeemCodeInput)
                                    redeemCodeMessage = uiCopy("Riscatto avviato su Google Play.", strings.langCode)
                                    redeemCodeInput = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                enabled = redeemCodeInput.isNotBlank(),
                            ) { Text(uiCopy("Riscatta codice", strings.langCode)) }
                            TextButton(onClick = onRefreshProPurchases, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(uiCopy("Verifica acquisti", strings.langCode))
                            }
                            if (redeemCodeMessage.isNotBlank()) {
                                Text(redeemCodeMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(
                        uiCopy("Riscatta un codice Zing", strings.langCode),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        uiCopy("I codici Zing personali sono monouso e devono essere verificati online.", strings.langCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    var zingCodeInput by rememberSaveable { mutableStateOf("") }
                    OutlinedTextField(
                        value = zingCodeInput,
                        onValueChange = { zingCodeInput = it.uppercase().take(64) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(uiCopy("Codice Zing", strings.langCode)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        enabled = !extraPrefs.supporterProUnlocked,
                    )
                    Button(
                        onClick = { onRedeemZingCode(zingCodeInput); zingCodeInput = "" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = zingCodeInput.isNotBlank() && !extraPrefs.supporterProUnlocked,
                    ) { Text(uiCopy("Verifica e riscatta codice Zing", strings.langCode)) }
                    if (!zingProCodeStatus.isNullOrBlank()) {
                        Text(uiCopy(zingProCodeStatus, strings.langCode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(
                        onClick = {
                            runCatching {
                                uriHandler.openUri("mailto:studio44z.info@gmail.com?subject=Zing%20Pro%20support")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(uiCopy("Hai cambiato telefono? Scrivi a studio44z.info@gmail.com per assistenza.", strings.langCode))
                    }
                    if (extraPrefs.supporterProUnlocked) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Text(
                            uiCopy("Supporter attivo", strings.langCode),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(uiCopy("Palette colore applicata subito:", strings.langCode), fontWeight = FontWeight.Medium)
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                "emerald" to uiCopy("🌿 Smeraldo", strings.langCode),
                                "citrus" to uiCopy("🍋 Agrumi", strings.langCode),
                                "berry" to uiCopy("🫐 Frutti di bosco", strings.langCode),
                                "ocean" to uiCopy("🌊 Oceano", strings.langCode),
                                "terracotta" to uiCopy("🏺 Terracotta", strings.langCode),
                                "lavender" to uiCopy("🪻 Lavanda", strings.langCode),
                            ).forEach { (key, label) ->
                                FilterPill(
                                    label = label,
                                    selected = extraPrefs.supporterAccentTheme == key,
                                    onClick = { onUpdateExtraPrefs(extraPrefs.copy(supporterAccentTheme = key)) },
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Timer,
                            title = uiCopy("Schermo sempre acceso durante le ricette", strings.langCode),
                            description = uiCopy("Impedisce allo schermo di spegnersi mentre segui i passaggi in cucina.", strings.langCode),
                            checked = extraPrefs.keepScreenOnCooking,
                            onChanged = { onUpdateExtraPrefs(extraPrefs.copy(keepScreenOnCooking = it)) },
                        )
                        val byLocation = lots.filter { it.quantity > 0 }.groupBy { it.location }.mapValues { it.value.size }
                        if (byLocation.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            ) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        uiCopy("📊 Analisi Rapida Dispensa (Pro)", strings.langCode),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        byLocation.entries.joinToString(" · ") { "${it.key}: ${it.value}" },
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    SettingsSwitchRow(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = uiCopy("Pubblicità volontaria (Opt-in)", strings.langCode),
                        description = uiCopy(
                            "Mostra un banner in fondo all’app per sostenere lo sviluppo. Gli annunci vengono richiesti solo quando attivi questa opzione e dopo le scelte privacy richieste.",
                            strings.langCode,
                        ),
                        checked = extraPrefs.voluntarySupportAdsEnabled,
                        onChanged = { enabled ->
                            onUpdateExtraPrefs(extraPrefs.copy(voluntarySupportAdsEnabled = enabled))
                        },
                    )
                    if (adsPrivacyOptionsRequired) {
                        OutlinedButton(
                            onClick = onManageAdsPrivacy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(uiCopy("Gestisci le preferenze privacy degli annunci", strings.langCode))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // The repository URL will be added after the official GitHub repository is created.
                }
            }
        }

        item {
            SectionHeading(uiCopy("Assistenza", strings.langCode), uiCopy("Segnala un problema o proponi una nuova funzione", strings.langCode))
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(41.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(uiCopy("Segnala Bug o Proponi Feature", strings.langCode), fontWeight = FontWeight.SemiBold)
                            Text(
                                uiCopy("Apre un dialog per scrivere il messaggio e inviarlo via email.", strings.langCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Button(
                        onClick = { feedbackDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(uiCopy("Apri segnalazione", strings.langCode))
                    }
                }
            }
        }

        item {
            SectionHeading(strings.secData, strings.secDataSub)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Acquisti", purchases.toString(), "registrati", Icons.Outlined.ShoppingBasket, Modifier.weight(1f))
                MetricCard("Consumi", consumed.toString(), "registrati", Icons.Outlined.LocalDining, Modifier.weight(1f))
                MetricCard("Ricette", cooked.toString(), "preparate", Icons.Outlined.Restaurant, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Backup, null); Spacer(Modifier.width(8.dp)); Text(strings.exportBackupBtn)
                    }
                    OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(8.dp)); Text(strings.importBackupBtn)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    OutlinedButton(
                        onClick = onClearAll,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Outlined.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text(strings.deleteAllBtn)
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(strings.appTagline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(uiCopy("Version 1.2.0 · by Studio 44z · Offline-first", strings.langCode), style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(
                        onClick = {
                            runCatching { uriHandler.openUri(ZING_PRIVACY_POLICY_URL) }
                                .onFailure { Toast.makeText(context, uiCopy("Impossibile aprire l’informativa privacy.", strings.langCode), Toast.LENGTH_LONG).show() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(uiCopy("Informativa privacy", strings.langCode))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$ZING_FEEDBACK_EMAIL")
                                    putExtra(Intent.EXTRA_SUBJECT, "Zing · Contatto sviluppatore")
                                }
                                runCatching { context.startActivity(emailIntent) }
                                    .onFailure {
                                        Toast.makeText(context, uiCopy("Nessuna app email disponibile.", strings.langCode), Toast.LENGTH_LONG).show()
                                    }
                            }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "studio44z.info@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    if (feedbackDialog) {
        FeedbackDialog(onDismiss = { feedbackDialog = false })
    }
}

@Composable
private fun FeedbackDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var kind by remember { mutableStateOf("bug") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.94f),
        ) {
            Column(
                Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    uiCopy("Segnala Bug o Proponi Feature", strings.langCode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(uiCopy("Segnala bug", strings.langCode), selected = kind == "bug", onClick = { kind = "bug" })
                    FilterPill(uiCopy("Proponi feature", strings.langCode), selected = kind == "feature", onClick = { kind = "feature" })
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiCopy("Oggetto", strings.langCode)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiCopy("Messaggio", strings.langCode)) },
                    minLines = 5,
                    shape = RoundedCornerShape(14.dp),
                )

                Text(
                    uiCopy("Il messaggio verrà aperto nell'app email del dispositivo. Non viene inviato automaticamente da Zing.", strings.langCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(uiCopy("Annulla", strings.langCode))
                    }
                    Button(
                        onClick = {
                            if (body.isBlank()) {
                                Toast.makeText(context, uiCopy("Scrivi il messaggio prima di inviare.", strings.langCode), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalSubject = when {
                                subject.isNotBlank() -> subject.trim()
                                kind == "bug" -> uiCopy("Zing · Segnalazione bug", strings.langCode)
                                else -> uiCopy("Zing · Proposta di funzionalità", strings.langCode)
                            }
                            val finalBody = buildString {
                                append(body.trim())
                                if (body.isNotBlank()) append("\n\n")
                                append(uiCopy("— Inviato dall'app Zing", strings.langCode))
                            }
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$ZING_FEEDBACK_EMAIL")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(ZING_FEEDBACK_EMAIL))
                                putExtra(Intent.EXTRA_SUBJECT, finalSubject)
                                putExtra(Intent.EXTRA_TEXT, finalBody)
                            }
                            try {
                                context.startActivity(intent)
                                onDismiss()
                            } catch (_: android.content.ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    uiCopy("Nessuna app email disponibile. Installa o configura un client email.", strings.langCode),
                                    Toast.LENGTH_LONG,
                                ).show()
                            } catch (_: Exception) {
                                Toast.makeText(context, uiCopy("Impossibile aprire l'app email.", strings.langCode), Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(uiCopy("Invia", strings.langCode))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(41.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@Composable
private fun LotEditorDialog(
    existing: FoodLotEntity?,
    initialBarcode: String,
    lookupResult: ProductLookupResult?,
    onDismiss: () -> Unit,
    onLookup: (String) -> Unit,
    onSave: (FoodLotEntity) -> Unit,
) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val standardLocations = listOf("Dispensa", "Frigorifero", "Congelatore", "Cantina")
    var name by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.name.orEmpty()) }
    var brand by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.brand.orEmpty()) }
    var barcode by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.barcode?.takeIf { it.isNotBlank() } ?: initialBarcode) }
    var category by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.category ?: "Altro") }
    var quantity by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.quantity?.toString() ?: "1") }
    var unit by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.unit ?: extraPrefs.defaultUnit) }
    var location by remember(existing?.id, initialBarcode) {
        mutableStateOf(existing?.location?.takeIf { it in standardLocations } ?: if (existing == null) extraPrefs.defaultLocation else "Altro")
    }
    var customLocation by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.location?.takeIf { it !in standardLocations }.orEmpty()) }
    var purchaseDate by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.purchaseDateMillis?.let(::formatItalianDate)?.let(::dateInputDigits).orEmpty()) }
    var expiryDate by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.expiryDateMillis?.let(::formatItalianDate)?.let(::dateInputDigits).orEmpty()) }
    var openedDate by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.openedDateMillis?.let(::formatItalianDate)?.let(::dateInputDigits).orEmpty()) }
    var minimum by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.minQuantity?.toString() ?: extraPrefs.defaultMinStock) }
    var notes by remember(existing?.id, initialBarcode) { mutableStateOf(existing?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var lookupFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lookupResult) {
        when (val result = lookupResult) {
            is ProductLookupResult.Found -> {
                if (name.isBlank()) name = result.product.name
                if (brand.isBlank()) brand = result.product.brand
                if (category == "Altro") category = result.product.category
                lookupFeedback = buildString {
                    append("Prodotto trovato: ${result.product.name}")
                    if (result.product.quantityLabel.isNotBlank()) append(" (${result.product.quantityLabel})")
                }
            }
            ProductLookupResult.NotFound -> lookupFeedback = "Codice non presente su Open Food Facts: compila i dati una volta per salvarlo in cache."
            is ProductLookupResult.Unavailable -> lookupFeedback = result.reason
            null -> Unit
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.94f).heightIn(max = 720.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (existing == null) strings.btnAdd else "Modifica", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null) }
                }
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Nome *", strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, modifier = Modifier.weight(1f), label = { Text(uiCopy("Marca", strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                    ChoiceField(uiCopy("Categoria", strings.langCode), category, listOf("Altro", "Frutta e verdura", "Pasta e cereali", "Latticini", "Carne e pesce", "Bevande", "Surgelati", "Conserve", "Pane e forno", "Snack"), { category = it }, Modifier.weight(1.15f))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = barcode, onValueChange = { barcode = it.trim(); lookupFeedback = null },
                        modifier = Modifier.weight(1f), label = { Text(uiCopy("Barcode", strings.langCode)) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedButton(onClick = { if (barcode.isNotBlank()) onLookup(barcode) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp)) {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    }
                }
                if (!lookupFeedback.isNullOrBlank()) {
                    Text(lookupFeedback!!, style = MaterialTheme.typography.bodySmall, color = if (lookupResult is ProductLookupResult.Found) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                }
                val remoteProduct = (lookupResult as? ProductLookupResult.Found)?.product
                if (remoteProduct != null) {
                    RemoteProductNutritionCard(remoteProduct = remoteProduct, prefs = extraPrefs)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it.replace(',', '.'); error = null }, modifier = Modifier.weight(1f), label = { Text(uiCopy("Quantità *", strings.langCode)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(14.dp))
                    ChoiceField(
                        uiCopy("Unità", strings.langCode), unit,
                        ((if (extraPrefs.measurementSystem == "imperial") listOf("pz", "oz", "lb", "fl oz", "cup", "confezioni", "fette", "porzioni") else listOf("pz", "g", "kg", "ml", "l", "confezioni", "fette", "foglie", "porzioni")) + unit).distinct(),
                        { unit = it }, Modifier.weight(0.8f),
                    )
                }
                ChoiceField(uiCopy("Posizione", strings.langCode), location, standardLocations + "Altro", { location = it }, Modifier.fillMaxWidth())
                if (location == "Altro") {
                    OutlinedTextField(value = customLocation, onValueChange = { customLocation = it }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Posizione personalizzata", strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = { purchaseDate = dateInputDigits(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text(uiCopy("Acquisto (gg/mm/aaaa)", strings.langCode)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ItalianDateVisualTransformation,
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = dateInputDigits(it); error = null },
                        modifier = Modifier.weight(1f),
                        label = { Text(uiCopy("Scadenza (gg/mm/aaaa)", strings.langCode)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ItalianDateVisualTransformation,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
                OutlinedTextField(
                    value = openedDate,
                    onValueChange = { openedDate = dateInputDigits(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiCopy("Data apertura (gg/mm/aaaa)", strings.langCode)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ItalianDateVisualTransformation,
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(value = minimum, onValueChange = { minimum = it.replace(',', '.') }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Scorta minima", strings.langCode)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Note", strings.langCode)) }, minLines = 2, shape = RoundedCornerShape(14.dp))
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text(strings.cancelBtn) }
                    Button(
                        onClick = {
                            val amount = quantity.toDoubleOrNull()
                            val minAmount = minimum.toDoubleOrNull()
                            val datesOk = listOf(purchaseDate, expiryDate, openedDate).all { it.isBlank() || parseItalianDate(it) != null }
                            when {
                                name.isBlank() -> error = uiCopy("Inserisci il nome del prodotto.", strings.langCode)
                                amount == null || amount < 0 || !amount.isFinite() -> error = uiCopy("Quantità non valida.", strings.langCode)
                                minAmount == null || minAmount < 0 || !minAmount.isFinite() -> error = uiCopy("Scorta minima non valida.", strings.langCode)
                                !datesOk -> error = uiCopy("Usa il formato data gg/mm/aaaa.", strings.langCode)
                                location == "Altro" && customLocation.isBlank() -> error = uiCopy("Indica la posizione.", strings.langCode)
                                else -> onSave(
                                    FoodLotEntity(
                                        id = existing?.id ?: 0,
                                        name = name.trim(), brand = brand.trim(), barcode = barcode.trim(), category = category,
                                        quantity = amount, unit = unit, location = if (location == "Altro") customLocation.trim() else location,
                                        purchaseDateMillis = parseItalianDate(purchaseDate), expiryDateMillis = parseItalianDate(expiryDate),
                                        openedDateMillis = parseItalianDate(openedDate), minQuantity = minAmount, notes = notes.trim(),
                                        createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
                                    ),
                                )
                            }
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                    ) { Text(strings.saveBtn) }
                }
            }
        }
    }
}

@Composable
private fun ChoiceField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current
    Box(modifier) {
        OutlinedTextField(
            value = uiCopy(value, strings.langCode), onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            label = { Text(uiCopy(label, strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp),
        )
        Box(Modifier.matchParentSize().clickable { expanded = true })
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(uiCopy(option, strings.langCode)) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
private fun RecipeEditorDialog(existing: RecipeEntity?, onDismiss: () -> Unit, onSave: (RecipeEntity) -> Unit) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var description by remember(existing?.id) { mutableStateOf(existing?.description.orEmpty()) }
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: "Primi") }
    var prep by remember(existing?.id) { mutableStateOf(existing?.prepMinutes?.toString() ?: "10") }
    var cook by remember(existing?.id) { mutableStateOf(existing?.cookMinutes?.toString() ?: "15") }
    var servings by remember(existing?.id) { mutableStateOf(existing?.servings?.toString() ?: "2") }
    var rating by remember(existing?.id) { mutableIntStateOf(existing?.rating ?: 0) }
    var ingredientText by remember(existing?.id) { mutableStateOf(existing?.let { ingredientsToEditorText(parseRecipeIngredients(it.ingredientsJson)) }.orEmpty()) }
    var instructions by remember(existing?.id) { mutableStateOf(existing?.instructions.orEmpty()) }
    var notes by remember(existing?.id) { mutableStateOf(existing?.notes.orEmpty()) }
    var imageUri by remember(existing?.id) { mutableStateOf(existing?.imageUri.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imageUri = uri.toString()
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val saved = saveBitmapToInternalStorage(context, bitmap)
            if (saved != null) imageUri = saved
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.94f).heightIn(max = 760.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (existing == null) uiCopy("Nuova ricetta personale", strings.langCode) else uiCopy("Modifica ricetta", strings.langCode), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (imageUri.isNotBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = name,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                        Text(uiCopy("📷 Galleria", strings.langCode))
                    }
                    OutlinedButton(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) {
                        Text(uiCopy("📸 Scatta foto", strings.langCode))
                    }
                    if (imageUri.isNotBlank()) {
                        IconButton(onClick = { imageUri = "" }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = uiCopy("Rimuovi foto", strings.langCode), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Nome *", strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Descrizione", strings.langCode)) }, minLines = 2, shape = RoundedCornerShape(14.dp))
                ChoiceField(uiCopy("Categoria", strings.langCode), category, listOf("Primi", "Secondi", "Piatti unici", "Insalate", "Zuppe", "Contorni", "Antipasti", "Colazioni", "Snack", "Dolci", "Bevande", "Altro"), { category = it }, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = prep, onValueChange = { prep = it.filter(Char::isDigit) }, modifier = Modifier.weight(1f), label = { Text(uiCopy("Prep (min)", strings.langCode)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = cook, onValueChange = { cook = it.filter(Char::isDigit) }, modifier = Modifier.weight(1f), label = { Text(uiCopy("Cottura (min)", strings.langCode)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = servings, onValueChange = { servings = it.filter(Char::isDigit) }, modifier = Modifier.weight(0.8f), label = { Text(uiCopy("Porzioni", strings.langCode)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(14.dp))
                }
                OutlinedTextField(
                    value = ingredientText, onValueChange = { ingredientText = it; error = null }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiCopy("Ingredienti (nome; quantità; unità) *", strings.langCode)) }, placeholder = { Text(uiCopy("Gnocchi; 200; g\nStracchino; 100; g\nSale; q.b.;", strings.langCode)) }, minLines = 5, shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = instructions, onValueChange = { instructions = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiCopy("Procedimento (1 passaggio per riga · i minuti creano timer automatici) *", strings.langCode)) },
                    minLines = 5, shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Note", strings.langCode)) }, minLines = 2, shape = RoundedCornerShape(14.dp))
                StarRatingInput(
                    rating = rating,
                    onRatingChange = { rating = it },
                )
                if (error != null) Text(error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text(strings.cancelBtn) }
                    Button(
                        onClick = {
                            val originalServings = servings.toIntOrNull()
                            val prepValue = prep.toIntOrNull()
                            val cookValue = cook.toIntOrNull()
                            val parsedIngredients = parseEditorIngredients(ingredientText)
                            when {
                                name.isBlank() -> error = uiCopy("Inserisci il nome.", strings.langCode)
                                originalServings == null || originalServings !in 1..30 -> error = uiCopy("Porzioni tra 1 e 30.", strings.langCode)
                                prepValue == null || cookValue == null -> error = uiCopy("Tempi non validi.", strings.langCode)
                                parsedIngredients.isEmpty() -> error = uiCopy("Inserisci almeno un ingrediente.", strings.langCode)
                                instructions.isBlank() -> error = uiCopy("Inserisci il procedimento.", strings.langCode)
                                else -> onSave(
                                    RecipeEntity(
                                        id = existing?.id ?: 0,
                                        name = name.trim(), description = description.trim(), category = category,
                                        prepMinutes = prepValue, cookMinutes = cookValue, servings = originalServings,
                                        ingredientsJson = encodeEditorIngredients(parsedIngredients), instructions = instructions.trim(),
                                        notes = notes.trim(), rating = rating.coerceIn(0, 5), favorite = existing?.favorite ?: false,
                                        imageUri = imageUri.ifBlank { null }, isSeed = false,
                                        createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(), updatedAtMillis = System.currentTimeMillis(),
                                    ),
                                )
                            }
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                    ) { Text(strings.saveBtn) }
                }
            }
        }
    }
}

private fun ingredientsToEditorText(ingredients: List<IngredientAmount>): String = ingredients.joinToString("\n") { rawIngredient ->
    val ingredient = QuantityMath.normalizeIngredient(rawIngredient)
    val amount = when {
        QuantityMath.isRangeNote(ingredient.note) -> QuantityMath.formatRangeAmount(ingredient.quantity, ingredient.note) ?: "q.b."
        ingredient.quantity != null -> QuantityMath.clean(ingredient.quantity).toString()
        else -> ingredient.note?.takeUnless { QuantityMath.isRangeNote(it) } ?: "q.b."
    }
    val unit = ingredient.unit.takeUnless { it.equals("q.b.", ignoreCase = true) }.orEmpty()
    "${ingredient.name}; $amount; $unit"
}

private fun parseEditorIngredients(raw: String): List<IngredientAmount> = raw.lines().mapNotNull { line ->
    val cleanLine = line.trim()
    if (cleanLine.isBlank()) return@mapNotNull null
    val parts = cleanLine.split(';')
    val name = parts.getOrNull(0).orEmpty().trim()
    if (name.isBlank()) return@mapNotNull null

    if (parts.size == 1) {
        return@mapNotNull QuantityMath.normalizeIngredient(IngredientAmount(name, null, "", null))
    }

    val amountText = parts.getOrNull(1).orEmpty().trim()
    val unitRaw = parts.getOrNull(2).orEmpty().trim()
    val range = Regex("""^(\d+(?:[.,]\d+)?)\s*[-–]\s*(\d+(?:[.,]\d+)?)$""").find(amountText)
    if (range != null) {
        val low = range.groupValues[1].replace(',', '.').toDoubleOrNull()
        val high = range.groupValues[2].replace(',', '.').toDoubleOrNull()
        if (low != null && high != null && low >= 0.0 && high >= low && unitRaw.isNotBlank()) {
            return@mapNotNull QuantityMath.normalizeIngredient(
                IngredientAmount(name, (low + high) / 2.0, unitRaw, QuantityMath.rangeNote(low, high)),
            )
        }
    }

    val quantity = amountText.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 && it.isFinite() }
    val unit = unitRaw.ifBlank { if (quantity == null) "q.b." else "" }
    val note = if (quantity == null) amountText.ifBlank { "q.b." } else null
    QuantityMath.normalizeIngredient(IngredientAmount(name, quantity, unit, note))
}

private fun encodeEditorIngredients(ingredients: List<IngredientAmount>): String = org.json.JSONArray().apply {
    ingredients.forEach { ingredient ->
        put(
            org.json.JSONObject().apply {
                put("name", ingredient.name)
                if (ingredient.quantity == null) put("quantity", org.json.JSONObject.NULL) else put("quantity", ingredient.quantity)
                put("unit", ingredient.unit)
                put("note", ingredient.note ?: "")
            },
        )
    }
}.toString()

@Composable
private fun ShoppingItemDialog(onDismiss: () -> Unit, onSave: (ShoppingItemEntity) -> Unit) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(extraPrefs.defaultUnit) }
    var category by remember { mutableStateOf("Altro") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(strings.btnAdd, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Prodotto *", strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it.replace(',', '.'); error = null }, modifier = Modifier.weight(1f), label = { Text(uiCopy("Quantità", strings.langCode)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(14.dp))
                    ChoiceField(uiCopy("Unità", strings.langCode), unit, listOf("pz", "g", "kg", "ml", "l", "confezioni", "fette"), { unit = it }, Modifier.weight(0.8f))
                }
                ChoiceField(uiCopy("Categoria", strings.langCode), category, listOf("Altro", "Frutta e verdura", "Pasta e cereali", "Latticini", "Carne e pesce", "Bevande", "Surgelati", "Conserve", "Pane e forno", "Snack"), { category = it }, Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Note", strings.langCode)) }, minLines = 2, shape = RoundedCornerShape(14.dp))
                if (error != null) Text(error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(strings.cancelBtn) }
                    Button(
                        onClick = {
                            val amount = quantity.toDoubleOrNull()
                            when {
                                name.isBlank() -> error = uiCopy("Inserisci il prodotto.", strings.langCode)
                                amount == null || amount < 0 || !amount.isFinite() -> error = uiCopy("Quantità non valida.", strings.langCode)
                                else -> onSave(ShoppingItemEntity(name = name.trim(), quantity = amount, unit = unit, category = category, notes = notes.trim(), isManual = true))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(strings.saveBtn) }
                }
            }
        }
    }
}

@Composable
private fun RegisterPurchaseDialog(item: ShoppingItemEntity, onDismiss: () -> Unit, onConfirm: (Double, String, String, Long?) -> Unit) {
    val strings = LocalStrings.current
    val extraPrefs = LocalExtraPrefs.current
    val standardLocations = listOf("Dispensa", "Frigorifero", "Congelatore", "Cantina")
    var quantity by remember(item.id) { mutableStateOf(item.quantity.toString()) }
    var unit by remember(item.id) { mutableStateOf(item.unit) }
    var location by remember(item.id) { mutableStateOf(extraPrefs.defaultLocation) }
    var customLocation by remember(item.id) { mutableStateOf("") }
    var expiry by remember(item.id) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(strings.boughtAddInventory, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it.replace(',', '.'); error = null }, modifier = Modifier.weight(1f), label = { Text(uiCopy("Quantità", strings.langCode)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(14.dp))
                    ChoiceField(uiCopy("Unità", strings.langCode), unit, listOf("pz", "g", "kg", "ml", "l", "confezioni", "fette"), { unit = it }, Modifier.weight(0.8f))
                }
                ChoiceField(uiCopy("Posizione", strings.langCode), location, standardLocations + "Altro", { location = it }, Modifier.fillMaxWidth())
                if (location == "Altro") OutlinedTextField(value = customLocation, onValueChange = { customLocation = it }, modifier = Modifier.fillMaxWidth(), label = { Text(uiCopy("Posizione personalizzata", strings.langCode)) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { expiry = dateInputDigits(it); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiCopy("Scadenza (gg/mm/aaaa)", strings.langCode)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ItalianDateVisualTransformation,
                    shape = RoundedCornerShape(14.dp),
                )
                if (error != null) Text(error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(strings.cancelBtn) }
                    Button(
                        onClick = {
                            val amount = quantity.toDoubleOrNull()
                            when {
                                amount == null || amount < 0 || !amount.isFinite() -> error = uiCopy("Quantità non valida.", strings.langCode)
                                expiry.isNotBlank() && parseItalianDate(expiry) == null -> error = "Formato gg/mm/aaaa."
                                location == "Altro" && customLocation.isBlank() -> error = "Inserisci posizione."
                                else -> onConfirm(amount, unit, if (location == "Altro") customLocation.trim() else location, parseItalianDate(expiry))
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(strings.saveBtn) }
                }
            }
        }
    }
}

@Composable
private fun RemovalReasonDialog(
    lotName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val strings = LocalStrings.current
    var selectedReason by rememberSaveable(lotName) { mutableStateOf("used") }
    val reasons = listOf(
        "used" to "L'ho utilizzato",
        "discarded" to "L'ho buttato",
        "expired" to "Era scaduto",
        "donated" to "L'ho regalato o ceduto",
        "other" to "Altro motivo",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(uiCopy("Perché rimuovi questo alimento?", strings.langCode)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    uiCopy("{name} verrà rimosso dalla dispensa e la motivazione sarà registrata nello storico.", strings.langCode).replace("{name}", lotName),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(4.dp))
                reasons.forEach { (value, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { selectedReason = value }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedReason == value,
                            onClick = { selectedReason = value },
                        )
                        Text(uiCopy(label, strings.langCode), modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text(uiCopy("Conferma rimozione", strings.langCode)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancelBtn) } },
    )
}

@Composable
private fun ConfirmDialog(title: String, message: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancelBtn) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutritionProfileSettingsCard(
    prefs: ZingExtraPrefs,
    onSave: (ZingExtraPrefs) -> Unit,
    langCode: String,
) {
    val imperial = prefs.measurementSystem == "imperial"
    var birthDateText by rememberSaveable(prefs.nutritionBirthDate) { mutableStateOf(prefs.nutritionBirthDate) }
    var showBirthDatePicker by rememberSaveable { mutableStateOf(false) }
    var weightInput by rememberSaveable(prefs.nutritionWeightKg, prefs.measurementSystem) {
        mutableStateOf(prefs.nutritionWeightKg.toDoubleOrNull()?.let { value -> if (imperial) String.format(Locale.ROOT, "%.1f", value * 2.2046226218) else value.toString() }.orEmpty())
    }
    var heightInput by rememberSaveable(prefs.nutritionHeightCm, prefs.measurementSystem) {
        mutableStateOf(prefs.nutritionHeightCm.toDoubleOrNull()?.let { value -> if (imperial) String.format(Locale.ROOT, "%.1f", value / 2.54) else value.toString() }.orEmpty())
    }
    var sex by rememberSaveable(prefs.nutritionSex) { mutableStateOf(prefs.nutritionSex) }
    var days by rememberSaveable(prefs.nutritionTrainingDays) { mutableIntStateOf(prefs.nutritionTrainingDays.coerceIn(0, 7)) }
    var focus by rememberSaveable(prefs.nutritionFocus) { mutableStateOf(prefs.nutritionFocus) }
    var error by remember { mutableStateOf<String?>(null) }

    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(uiCopy("Profilo nutrizionale (facoltativo)", langCode), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                uiCopy("I dati del profilo restano sul dispositivo. Servono solo a contestualizzare le informazioni nutrizionali: non sono una diagnosi né una dieta medica.", langCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val selectedBirthDate = parseBirthDateOrNull(birthDateText)
            OutlinedButton(
                onClick = { showBirthDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    selectedBirthDate?.let { "${uiCopy("Data di nascita", langCode)} · ${formatBirthDate(it, langCode)}" }
                        ?: uiCopy("Seleziona la data di nascita", langCode),
                )
            }
            if (selectedBirthDate != null) {
                val calculatedAge = it.studio44z.zing.domain.ageFromBirthDate(selectedBirthDate)
                if (calculatedAge != null) {
                    Text(
                        uiCopy("Età calcolata automaticamente: {age} anni", langCode).replace("{age}", calculatedAge.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else if (prefs.nutritionAge.isNotBlank()) {
                Text(
                    uiCopy("Il profilo precedente salvava solo l'età. Inserisci la data di nascita per continuare a usare la stima energetica.", langCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showBirthDatePicker) {
                val initialDate = selectedBirthDate ?: LocalDate.now().minusYears(18)
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialDate.toUtcDatePickerMillis(),
                    yearRange = 1900..LocalDate.now().year,
                )
                DatePickerDialog(
                    onDismissRequest = { showBirthDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val chosen = datePickerState.selectedDateMillis?.let(::datePickerMillisToLocalDate)
                            when {
                                chosen == null -> error = uiCopy("Seleziona una data di nascita valida.", langCode)
                                chosen.isAfter(LocalDate.now()) -> error = uiCopy("La data di nascita non può essere futura.", langCode)
                                else -> {
                                    birthDateText = chosen.toString()
                                    error = null
                                    showBirthDatePicker = false
                                }
                            }
                        }) { Text(uiCopy("Conferma", langCode)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBirthDatePicker = false }) { Text(uiCopy("Annulla", langCode)) }
                    },
                ) {
                    DatePicker(state = datePickerState, showModeToggle = false)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' }.take(7); error = null },
                    modifier = Modifier.weight(1f),
                    label = { Text(if (imperial) uiCopy("Peso (lb)", langCode) else uiCopy("Peso (kg)", langCode)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it.replace(',', '.').filter { ch -> ch.isDigit() || ch == '.' }.take(7); error = null },
                    modifier = Modifier.weight(1f),
                    label = { Text(if (imperial) uiCopy("Altezza (in)", langCode) else uiCopy("Altezza (cm)", langCode)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                )
            }
            Text(uiCopy("Sesso utilizzato solo per la stima energetica", langCode), fontWeight = FontWeight.SemiBold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("female" to "Donna", "male" to "Uomo", "unspecified" to "Non specificare").forEach { (key, label) ->
                    FilterPill(uiCopy(label, langCode), selected = sex == key, onClick = { sex = key })
                }
            }
            Text(uiCopy("Allenamenti a settimana", langCode), fontWeight = FontWeight.SemiBold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0..7).forEach { value -> FilterPill("$value", selected = days == value, onClick = { days = value }) }
            }
            Text(uiCopy("Priorità nutrizionale", langCode), fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "balanced" to "Equilibrio generale",
                    "lower_sugar" to "Meno zuccheri",
                    "lower_salt" to "Meno sale",
                    "higher_protein" to "Più proteine",
                ).forEach { (key, label) ->
                    FilterPill(uiCopy(label, langCode), selected = focus == key, onClick = { focus = key })
                }
            }
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    val enteredBirthDate = parseBirthDateOrNull(birthDateText)
                    val enteredWeight = weightInput.toDoubleOrNull()
                    val enteredHeight = heightInput.toDoubleOrNull()
                    when {
                        birthDateText.isNotBlank() && enteredBirthDate == null -> error = uiCopy("Seleziona una data di nascita valida.", langCode)
                        enteredBirthDate != null && enteredBirthDate.isAfter(LocalDate.now()) -> error = uiCopy("La data di nascita non può essere futura.", langCode)
                        weightInput.isNotBlank() && (enteredWeight == null || enteredWeight <= 0.0 || !enteredWeight.isFinite()) -> error = uiCopy("Peso non valido.", langCode)
                        heightInput.isNotBlank() && (enteredHeight == null || enteredHeight <= 0.0 || !enteredHeight.isFinite()) -> error = uiCopy("Altezza non valida.", langCode)
                        else -> {
                            val weightKg = enteredWeight?.let { if (imperial) it / 2.2046226218 else it }
                            val heightCm = enteredHeight?.let { if (imperial) it * 2.54 else it }
                            if (weightKg != null && weightKg !in 30.0..350.0) error = uiCopy("Per la stima inserisci un peso tra 30 e 350 kg.", langCode)
                            else if (heightCm != null && heightCm !in 120.0..230.0) error = uiCopy("Per la stima inserisci un'altezza tra 120 e 230 cm.", langCode)
                            else {
                                error = null
                                onSave(prefs.copy(
                                    nutritionBirthDate = enteredBirthDate?.toString().orEmpty(),
                                    nutritionAge = if (enteredBirthDate == null) prefs.nutritionAge else "",
                                    nutritionWeightKg = weightKg?.let { String.format(Locale.ROOT, "%.2f", it) }.orEmpty(),
                                    nutritionHeightCm = heightCm?.let { String.format(Locale.ROOT, "%.2f", it) }.orEmpty(),
                                    nutritionSex = sex,
                                    nutritionTrainingDays = days,
                                    nutritionFocus = focus,
                                ))
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) { Text(uiCopy("Salva profilo", langCode)) }
            val profile = NutritionProfile(
                birthDate = parseBirthDateOrNull(prefs.nutritionBirthDate),
                weightKg = prefs.nutritionWeightKg.toDoubleOrNull(),
                heightCm = prefs.nutritionHeightCm.toDoubleOrNull(),
                sexForEnergyEstimate = prefs.nutritionSex,
                trainingDaysPerWeek = prefs.nutritionTrainingDays,
                focus = prefs.nutritionFocus,
            )
            val dailyEnergy = NutritionAdvisor.estimateDailyEnergyKcal(profile)
            if (dailyEnergy != null) {
                Text(
                    uiCopy("Stima energetica indicativa: {kcal} kcal/giorno. È un valore orientativo, non una prescrizione.", langCode).replace("{kcal}", dailyEnergy.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RemoteProductNutritionCard(remoteProduct: it.studio44z.zing.data.RemoteProduct, prefs: ZingExtraPrefs) {
    val strings = LocalStrings.current
    val profile = NutritionProfile(
        birthDate = parseBirthDateOrNull(prefs.nutritionBirthDate),
        weightKg = prefs.nutritionWeightKg.toDoubleOrNull(),
        heightCm = prefs.nutritionHeightCm.toDoubleOrNull(),
        sexForEnergyEstimate = prefs.nutritionSex,
        trainingDaysPerWeek = prefs.nutritionTrainingDays,
        focus = prefs.nutritionFocus,
    )
    val nutritionFacts = NutritionFacts(
        energyKcal100g = remoteProduct.energyKcal100g,
        sugarsG100g = remoteProduct.sugarsG100g,
        saturatedFatG100g = remoteProduct.saturatedFatG100g,
        saltG100g = remoteProduct.saltG100g,
        fiberG100g = remoteProduct.fiberG100g,
        proteinG100g = remoteProduct.proteinG100g,
        nutriScoreGrade = remoteProduct.nutritionGrade,
        novaGroup = remoteProduct.novaGroup,
        ingredientsText = remoteProduct.ingredientsText,
    )
    val assessment = NutritionAdvisor.assess(nutritionFacts, profile)
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                val image = remoteProduct.imageUrl
                if (!image.isNullOrBlank()) {
                    AsyncImage(
                        model = image,
                        contentDescription = uiCopy("Foto del prodotto", strings.langCode),
                        modifier = Modifier.size(width = 88.dp, height = 104.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(uiCopy("Analisi nutrizionale indicativa", strings.langCode), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (assessment.score != null) {
                        Text("${assessment.score}/100 · ${uiCopy(assessment.scoreLabel, strings.langCode)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { assessment.score / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = when { assessment.score >= 70 -> Color(0xFF2E7D32); assessment.score >= 45 -> Color(0xFFE3A329); else -> MaterialTheme.colorScheme.error },
                        )
                    } else Text(uiCopy(assessment.scoreLabel, strings.langCode), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (assessment.officialNutriScoreGrade != null) Text(uiCopy("Nutri-Score dichiarato: {grade}", strings.langCode).replace("{grade}", assessment.officialNutriScoreGrade.uppercase(Locale.ROOT)))
                    if (assessment.novaGroup != null) Text(uiCopy("Gruppo NOVA: {group}/4", strings.langCode).replace("{group}", assessment.novaGroup.toString()))
                    Text(uiCopy("Dati disponibili: {percent}%", strings.langCode).replace("{percent}", assessment.completenessPercent.toString()), style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                localizedNutritionRecommendation(assessment, nutritionFacts, prefs.nutritionFocus, strings.langCode),
                style = MaterialTheme.typography.bodySmall,
            )
            if (!remoteProduct.ingredientsText.isNullOrBlank()) {
                Text(uiCopy("Ingredienti da catalogo", strings.langCode), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                Text(remoteProduct.ingredientsText, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun localizedNutritionRecommendation(
    assessment: NutritionAssessment,
    facts: NutritionFacts,
    focus: String,
    langCode: String,
): String {
    if (assessment.score == null) {
        return uiCopy(
            "Il catalogo non riporta valori nutrizionali sufficienti per stimare il punteggio. Controlla l'etichetta del prodotto.",
            langCode,
        )
    }

    val focusAdvice = when (focus) {
        "lower_sugar" -> "Focus personale: confronta soprattutto gli zuccheri per 100 g."
        "lower_salt" -> "Focus personale: confronta soprattutto il sale per 100 g."
        "higher_protein" -> "Focus personale: confronta le proteine per porzione e l'equilibrio complessivo."
        else -> "Confronta i valori per 100 g e la porzione che consumi abitualmente."
    }
    val nutrientDetails = buildList {
        facts.sugarsG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { add("Zuccheri: {value} g/100 g" to it) }
        facts.saltG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { add("Sale: {value} g/100 g" to it) }
        facts.saturatedFatG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { add("Grassi saturi: {value} g/100 g" to it) }
        facts.proteinG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { add("Proteine: {value} g/100 g" to it) }
        facts.fiberG100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { add("Fibre: {value} g/100 g" to it) }
        facts.energyKcal100g?.takeIf { it.isFinite() && it >= 0.0 }?.let { add("Energia: {value} kcal/100 g" to it) }
    }.map { (template, value) ->
        uiCopy(template, langCode).replace("{value}", String.format(Locale.getDefault(), "%.1f", value))
    }

    return buildString {
        append(uiCopy(focusAdvice, langCode))
        if (nutrientDetails.isNotEmpty()) {
            append(' ').append(uiCopy("Dati nutrizionali: {details}.", langCode).replace("{details}", nutrientDetails.joinToString(", ")))
        }
        if (assessment.completenessPercent < 67) {
            append(' ').append(uiCopy("I dati online sono parziali: verifica l'etichetta.", langCode))
        }
        assessment.kcalShareOfDailyEstimatePercent?.let { percent ->
            append(' ').append(uiCopy("100 g apportano circa il {percent}% della stima energetica giornaliera personale.", langCode).replace("{percent}", percent.toString()))
        }
        append(' ').append(uiCopy("Il punteggio è indicativo e non è una valutazione medica né il Nutri-Score ufficiale.", langCode))
    }
}

private fun formatQuantityForSystem(quantity: Double?, unit: String, measurementSystem: String, note: String? = null): String {
    if (quantity == null || !quantity.isFinite()) return QuantityMath.format(quantity, unit, note)
    val imperial = measurementSystem == "imperial"
    val massGrams = QuantityMath.convert(quantity, unit, "g")
    if (massGrams != null) {
        val target = if (imperial) {
            if (massGrams >= 453.59237) "lb" else "oz"
        } else {
            if (massGrams >= 1000.0) "kg" else "g"
        }
        val converted = QuantityMath.convert(quantity, unit, target) ?: quantity
        return QuantityMath.format(QuantityMath.clean(converted), target, note)
    }
    val volumeMl = QuantityMath.convert(quantity, unit, "ml")
    if (volumeMl != null) {
        val target = if (imperial) {
            if (volumeMl >= 236.5882365) "cup" else "fl oz"
        } else {
            if (volumeMl >= 1000.0) "l" else "ml"
        }
        val converted = QuantityMath.convert(quantity, unit, target) ?: quantity
        return QuantityMath.format(QuantityMath.clean(converted), target, note)
    }
    return QuantityMath.format(quantity, unit, note)
}

@Composable
private fun OnlineRecipeFinderDialog(lots: List<FoodLotEntity>, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<RemoteRecipeSuggestion>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val pantryNames = remember(lots) {
        lots.filter { it.quantity > 0.0 && !isExpiredDate(it.expiryDateMillis) }
            .map { it.name.trim() }.filter { it.isNotBlank() }.distinct()
    }
    val repository = remember { RecipeDiscoveryRepository() }
    val recipeLocalizer = requireNotNull(LocalRecipeLocalizer.current)

    fun search() {
        scope.launch {
            loading = true
            error = null
            try {
                results = repository.searchWithPantry(pantryNames)
                if (results.isEmpty()) error = uiCopy("Non ho trovato ricette compatibili. Prova con altri ingredienti o riprova più tardi.", strings.langCode)
            } catch (e: Exception) {
                results = emptyList()
                error = uiCopy("Ricerca online non disponibile in questo momento. Riprova tra poco.", strings.langCode)
            } finally {
                loading = false
            }
        }
    }
    LaunchedEffect(pantryNames) { search() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(0.96f).heightIn(max = 760.dp), shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(uiCopy("Ricette dalla tua dispensa", strings.langCode), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(uiCopy("Ricerca online · risultati classificati in base agli ingredienti disponibili", strings.langCode), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = uiCopy("Chiudi", strings.langCode)) }
                }
                if (pantryNames.isEmpty()) {
                    Text(uiCopy("La dispensa è vuota: aggiungi prima alcuni ingredienti.", strings.langCode), color = MaterialTheme.colorScheme.error)
                }
                if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(uiCopy("Cerco ricette usando gli ingredienti della dispensa…", strings.langCode), style = MaterialTheme.typography.bodyMedium)
                }
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 6.dp),
                ) {
                    items(results, key = { it.id }) { recipe ->
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                                if (!recipe.imageUrl.isNullOrBlank()) {
                                    AsyncImage(model = recipe.imageUrl, contentDescription = recipe.name, modifier = Modifier.size(86.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(recipeLocalizer.sourceRecipeTitle(recipe.name, strings.langCode), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    val metadata = listOf(recipe.category, recipe.area).filter { it.isNotBlank() }.joinToString(" · ")
                                    if (metadata.isNotBlank()) Text(metadata, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        uiCopy("Ingredienti disponibili: {matched} · mancanti: {missing}", strings.langCode)
                                            .replace("{matched}", recipe.matchedIngredients.size.toString())
                                            .replace("{missing}", recipe.missingIngredients.size.toString()),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    if (recipe.missingIngredients.isNotEmpty()) {
                                        Text(
                                            uiCopy("Da procurare: {items}", strings.langCode)
                                                .replace("{items}", recipe.missingIngredients.joinToString(", ") { recipeLocalizer.ingredientName(it, strings.langCode) }),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    } else {
                                        Text(
                                            uiCopy("Hai tutti gli ingredienti riconosciuti!", strings.langCode),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    TextButton(onClick = {
                                        val url = recipe.sourceUrl ?: return@TextButton
                                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                            .onFailure { Toast.makeText(context, uiCopy("Impossibile aprire la ricetta online.", strings.langCode), Toast.LENGTH_LONG).show() }
                                    }) { Text(uiCopy("Apri ricetta online", strings.langCode)) }
                                }
                            }
                        }
                    }
                }
                Text(
                    uiCopy("Le pagine Wikibooks indicano la propria licenza, generalmente CC BY-SA/GFDL. Il confronto degli ingredienti è automatico: controlla quantità, allergeni e preparazione sulla fonte originale.", strings.langCode),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    uiCopy("Per la ricerca vengono inviati a Wikibooks termini generici e varianti lessicali di massimo quattro ingredienti. Il profilo nutrizionale non viene condiviso.", strings.langCode),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://it.wikibooks.org/wiki/Wikibooks:Copyright")))
                        }.onFailure {
                            Toast.makeText(context, uiCopy("Impossibile aprire la licenza online.", strings.langCode), Toast.LENGTH_LONG).show()
                        }
                    }) { Text(uiCopy("Licenze Wikibooks", strings.langCode)) }
                    TextButton(onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://creativecommons.org/licenses/by-sa/4.0/")))
                        }.onFailure {
                            Toast.makeText(context, uiCopy("Impossibile aprire la licenza online.", strings.langCode), Toast.LENGTH_LONG).show()
                        }
                    }) { Text("CC BY-SA 4.0") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text(uiCopy("Chiudi", strings.langCode)) }
                    Button(onClick = { search() }, enabled = !loading && pantryNames.isNotEmpty(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text(uiCopy("Riprova", strings.langCode)) }
                }
            }
        }
    }
}


@Composable
private fun UsageStreakCard(days: Int, langCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("🔥", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f)) {
                Text(uiCopy("Giorni di fila", langCode), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    if (days == 1) uiCopy("Hai aperto Zing 1 giorno di fila.", langCode)
                    else uiCopy("Hai aperto Zing {days} giorni di fila.", langCode).replace("{days}", days.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun UpdateAvailableCard(langCode: String, onUpdate: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(uiCopy("Aggiornamento disponibile", langCode), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(uiCopy("È disponibile una nuova versione di Zing.", langCode), style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onUpdate) { Text(uiCopy("Aggiorna", langCode)) }
            IconButton(onClick = onDismiss) { Icon(Icons.Outlined.Close, contentDescription = uiCopy("Chiudi", langCode)) }
        }
    }
}

private fun formatProActivationDate(timeMillis: Long, langCode: String): String {
    val locale = when (langCode.lowercase(Locale.ROOT).substringBefore('-').substringBefore('_')) {
        "en" -> Locale.US
        "es" -> Locale.forLanguageTag("es-ES")
        "fr" -> Locale.FRENCH
        "de" -> Locale.GERMAN
        "pt" -> Locale.forLanguageTag("pt-PT")
        else -> Locale.ITALIAN
    }
    return DateFormat.getDateInstance(DateFormat.LONG, locale).format(Date(timeMillis))
}


private fun localizeBillingStatus(status: String?, langCode: String): String? {
    if (status.isNullOrBlank()) return null
    val key = when {
        status.startsWith("Google Play Billing non disponibile") -> "Google Play Billing non disponibile."
        status.startsWith("Impossibile caricare il prodotto Google Play") -> "Impossibile caricare il prodotto Google Play."
        status.startsWith("Prodotto non ancora attivo") || status.startsWith("Zing Pro non è ancora configurato") -> "Zing Pro non è ancora attivo in Google Play."
        status.startsWith("Impossibile verificare gli acquisti Google Play") -> "Impossibile verificare gli acquisti Google Play."
        status.startsWith("Prodotto non disponibile") || status.startsWith("Impossibile avviare l'acquisto") -> "Acquisto non disponibile."
        status == "Acquisto annullato." -> "Acquisto annullato."
        status.startsWith("Acquisto non completato") -> "Acquisto non completato. Riprova più tardi."
        status.startsWith("Google Play ha confermato") -> "Google Play ha confermato Zing Pro."
        status.startsWith("Acquisto riconosciuto") -> "Acquisto riconosciuto; conferma in attesa."
        status.startsWith("Connessione a Google Play in corso") -> "Connessione a Google Play in corso. Riprova tra poco."
        status.startsWith("L'offerta Google Play non contiene un token valido") -> "L'offerta Google Play non è configurata correttamente."
        status.startsWith("Inserisci un codice promozionale Google Play") -> "Inserisci un codice promozionale Google Play."
        status.startsWith("Completa il riscatto in Google Play, poi torna qui") -> "Completa il riscatto in Google Play, poi torna qui: controlleremo l'acquisto."
        status.startsWith("Completa il riscatto in Google Play e poi torna in Zing") -> "Completa il riscatto in Google Play e poi torna in Zing per aggiornare lo stato Pro."
        status.startsWith("Non riesco ad aprire Google Play") -> "Non riesco ad aprire Google Play. Aggiorna Play Store e riprova."
        else -> return status
    }
    return uiCopy(key, langCode)
}
