package it.studio44z.zing.ui

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StreakSnapshot(val days: Int, val lastActiveDate: LocalDate)

/** Pure, deterministic streak calculation using local calendar dates (not 24-hour durations). */
internal fun calculateNextStreak(previousDays: Int, lastActiveDate: LocalDate?, today: LocalDate): StreakSnapshot {
    if (lastActiveDate == null || previousDays < 1) return StreakSnapshot(1, today)
    return when {
        today == lastActiveDate -> StreakSnapshot(previousDays.coerceAtLeast(1), lastActiveDate)
        today.isBefore(lastActiveDate) -> StreakSnapshot(previousDays.coerceAtLeast(1), lastActiveDate)
        ChronoUnit.DAYS.between(lastActiveDate, today) == 1L -> StreakSnapshot(previousDays + 1, today)
        else -> StreakSnapshot(1, today)
    }
}

internal object UsageStreak {
    private const val PREFS = "zing_usage_streak"
    private const val KEY_DAYS = "days"
    private const val KEY_DATE = "last_active_date"

    fun recordToday(context: Context, today: LocalDate = LocalDate.now()): StreakSnapshot {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_DATE, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val previousDays = prefs.getInt(KEY_DAYS, 0)
        val result = calculateNextStreak(previousDays, lastDate, today)
        if (lastDate == null || today.isAfter(lastDate)) {
            prefs.edit().putInt(KEY_DAYS, result.days).putString(KEY_DATE, result.lastActiveDate.toString()).apply()
        } else if (lastDate == today && previousDays < 1) {
            prefs.edit().putInt(KEY_DAYS, result.days).putString(KEY_DATE, result.lastActiveDate.toString()).apply()
        }
        return result
    }
}
