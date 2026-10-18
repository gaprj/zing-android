package it.studio44z.zing.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun localDateForMillis(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

fun todayLocalDate(nowMillis: Long = System.currentTimeMillis(), zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    localDateForMillis(nowMillis, zoneId)

fun isExpiredDate(expiryMillis: Long?, today: LocalDate = todayLocalDate()): Boolean =
    expiryMillis != null && localDateForMillis(expiryMillis).isBefore(today)

fun isExpiringDate(expiryMillis: Long?, days: Int, today: LocalDate = todayLocalDate()): Boolean {
    if (expiryMillis == null) return false
    val date = localDateForMillis(expiryMillis)
    return !date.isBefore(today) && !date.isAfter(today.plusDays(days.coerceIn(0, 30).toLong()))
}

fun isDueOrExpiringDate(expiryMillis: Long?, days: Int, today: LocalDate = todayLocalDate()): Boolean {
    if (expiryMillis == null) return false
    return !localDateForMillis(expiryMillis).isAfter(today.plusDays(days.coerceIn(0, 30).toLong()))
}
