package it.studio44z.zing.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import it.studio44z.zing.MainActivity
import it.studio44z.zing.data.AppDatabase
import it.studio44z.zing.data.SettingsRepository
import it.studio44z.zing.data.todayLocalDate
import it.studio44z.zing.ui.notificationCopy
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

object AlarmSoundController {
    private var activeRingtone: Ringtone? = null

    @Synchronized
    fun startAlarm(context: Context) {
        stopAlarm()
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.isLooping = true
            }
            ringtone.play()
            activeRingtone = ringtone
        }
    }

    @Synchronized
    fun stopAlarm() {
        runCatching {
            activeRingtone?.stop()
        }
        activeRingtone = null
    }
}

class ExpiryReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val timerLabel = inputData.getString(KEY_TIMER_LABEL)
        if (!timerLabel.isNullOrBlank()) {
            val notificationId = NOTIFICATION_ID_TIMER_DONE + (timerLabel.hashCode() and 0xFFFF)
            showTimerFinishedNotification(applicationContext, timerLabel, notificationId)
            return Result.success()
        }

        val settings = SettingsRepository(applicationContext).settings.first()
        if (!settings.notificationsEnabled) return Result.success()

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val extraPrefs = applicationContext.getSharedPreferences("zest_extra_prefs", Context.MODE_PRIVATE)
        val notifyDayBefore = extraPrefs.getBoolean("notifyDayBeforeExpiry", true)

        val db = AppDatabase.get(applicationContext)
        val lots = db.foodLotDao().observeAll().first().filter { it.quantity > 0.0 && it.expiryDateMillis != null }
        if (lots.isEmpty()) return Result.success()

        val today = todayLocalDate()
        val presetDays = settings.daysBeforeExpiry.coerceAtLeast(0)

        val expiringTomorrow = lots.filter { lot ->
            calculateDaysUntil(lot.expiryDateMillis, today) == 1L
        }

        val expiringPreset = lots.filter { lot ->
            val d = calculateDaysUntil(lot.expiryDateMillis, today) ?: return@filter false
            if (notifyDayBefore && d == 1L && presetDays != 1) {
                false
            } else {
                d in 0L..presetDays.toLong()
            }
        }

        ensureChannels(applicationContext)

        if (notifyDayBefore && expiringTomorrow.isNotEmpty()) {
            val names = expiringTomorrow.take(4).joinToString(", ") { it.name }
            val title = if (expiringTomorrow.size == 1) {
                localized(applicationContext, "Scade domani: {name}", "name" to expiringTomorrow.first().name)
            } else {
                localized(applicationContext, "{count} alimenti scadono domani", "count" to expiringTomorrow.size.toString())
            }
            val body = localized(applicationContext, "Controlla in dispensa: {names}. Apri Zing per vedere le ricette consigliate.", "names" to names)
            postNotification(applicationContext, NOTIFICATION_ID_DAY_BEFORE, title, body)
        }

        if (expiringPreset.isNotEmpty()) {
            val names = expiringPreset.take(4).joinToString(", ") { it.name }
            val title = if (presetDays == 0) {
                localized(applicationContext, "Alimenti in scadenza oggi ({count})", "count" to expiringPreset.size.toString())
            } else {
                localized(applicationContext, "{count} alimenti in scadenza entro {days} giorni", "count" to expiringPreset.size.toString(), "days" to presetDays.toString())
            }
            val body = localized(applicationContext, "Da consumare prima: {names}.", "names" to names)
            postNotification(applicationContext, NOTIFICATION_ID_PRESET, title, body)
        }

        return Result.success()
    }

    private fun calculateDaysUntil(expiryDateMillis: Long?, today: LocalDate): Long? {
        if (expiryDateMillis == null) return null
        val expiryDate = Instant.ofEpochMilli(expiryDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return ChronoUnit.DAYS.between(today, expiryDate)
    }

    companion object {
        const val CHANNEL_EXPIRY = "zest_expiry_channel"
        const val CHANNEL_TIMER_LIVE = "zest_timer_live_v3"
        const val CHANNEL_TIMER_DONE = "zest_timer_alarm_v3"
        const val KEY_TIMER_LABEL = "timer_label"

        const val ACTION_STOP_TIMER_SOUND = "it.studio44z.zing.ACTION_STOP_TIMER_SOUND"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        private const val NOTIFICATION_ID_PRESET = 4101
        private const val NOTIFICATION_ID_DAY_BEFORE = 4102
        const val NOTIFICATION_ID_TIMER_LIVE = 4200
        const val NOTIFICATION_ID_TIMER_DONE = 4201

        private fun localized(context: Context, template: String, vararg substitutions: Pair<String, String>): String {
            val preferences = context.getSharedPreferences("zest_extra_prefs", Context.MODE_PRIVATE)
            val selected = preferences.getString("language", "system") ?: "system"
            val language = if (selected == "system") Locale.getDefault().language else selected
            return substitutions.fold(notificationCopy(template, language)) { result, (key, value) ->
                result.replace("{$key}", value)
            }
        }

        fun ensureChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val expiryChannel = NotificationChannel(
                CHANNEL_EXPIRY,
                localized(context, "Scadenze alimenti"),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = localized(context, "Avvisi per il giorno preimpostato e per il giorno prima della scadenza")
            }

            val timerLiveChannel = NotificationChannel(
                CHANNEL_TIMER_LIVE,
                localized(context, "Timer di cucina attivo (Live)"),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = localized(context, "Mostra il conto alla rovescia in tempo reale nella barra di stato")
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val alarmAudioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val timerDoneChannel = NotificationChannel(
                CHANNEL_TIMER_DONE,
                localized(context, "Sveglia fine timer di cucina"),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = localized(context, "Suoneria quando un passaggio della ricetta è terminato")
                setSound(alarmUri, alarmAudioAttrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 800)
            }

            manager.createNotificationChannel(expiryChannel)
            manager.createNotificationChannel(timerLiveChannel)
            manager.createNotificationChannel(timerDoneChannel)
        }

        private fun contentPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun postNotification(context: Context, id: Int, title: String, body: String) {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return

            val notification = NotificationCompat.Builder(context, CHANNEL_EXPIRY)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentPendingIntent(context))
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(id, notification)
        }

        private fun buildHyperIslandExtras(
            context: Context,
            label: String,
            endTimeMillis: Long,
        ): Bundle? {
            return runCatching {
                if (!HyperIslandNotification.isSupported(context)) return null

                val pictureKey = "zest_timer"
                val island = HyperIslandNotification.Builder(
                    context,
                    "zest_cooking_timer",
                    "Timer Zing",
                )
                    .addPicture(
                        HyperPicture(
                            pictureKey,
                            context,
                            android.R.drawable.ic_lock_idle_alarm,
                        ),
                    )
                    .setBaseInfo(
                        title = label.take(60),
                        content = localized(context, "Timer di cucina in corso"),
                        subTitle = "Zing",
                        pictureKey = pictureKey,
                    )
                    .setBigIslandCountdown((endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L), pictureKey)

                Bundle().apply {
                    putAll(island.buildResourceBundle())
                    putString("miui.focus.param", island.buildJsonParam())
                }
            }.getOrNull()
        }

        fun showLiveCookingTimerNotification(
            context: Context,
            label: String,
            endTimeMillis: Long,
            totalSeconds: Int = (((endTimeMillis - System.currentTimeMillis()) / 1_000L).toInt().coerceAtLeast(1)),
        ) {
            ensureChannels(context)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return

            val now = System.currentTimeMillis()
            val safeTotalSeconds = totalSeconds.coerceAtLeast(1)
            val remainingSeconds = ((endTimeMillis - now) / 1_000L).toInt().coerceAtLeast(0)
            val progress = (safeTotalSeconds - remainingSeconds).coerceIn(0, safeTotalSeconds)
            val progressPercent = (progress * 100 / safeTotalSeconds).coerceIn(0, 100)
            val remainingMin = ((remainingSeconds + 59) / 60).coerceAtLeast(1)

            val extras = Bundle().apply {
                putBoolean("android.requestPromotedOngoing", true)
            }

            val notification = if (Build.VERSION.SDK_INT >= 36) {
                val progressStyle = android.app.Notification.ProgressStyle()
                    .setStyledByProgress(true)
                    .setProgress(progress)
                    .setProgressSegments(
                        listOf(
                            android.app.Notification.ProgressStyle.Segment(safeTotalSeconds)
                                .setColor(0xFF3D8065.toInt()),
                        ),
                    )
                android.app.Notification.Builder(context, CHANNEL_TIMER_LIVE)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("⏱ $label")
                    .setContentText(localized(context, "Timer Zing in corso"))
                    .setSubText(localized(context, "{minutes}m rimanenti", "minutes" to remainingMin.toString()))
                    .setWhen(endTimeMillis)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setCategory(android.app.Notification.CATEGORY_PROGRESS)
                    .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                    .setStyle(progressStyle)
                    .addExtras(extras)
                    .setContentIntent(contentPendingIntent(context))
                    .build()
            } else {
                NotificationCompat.Builder(context, CHANNEL_TIMER_LIVE)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("⏱ $label")
                    .setContentText(localized(context, "Timer Zing in corso"))
                    .setSubText(localized(context, "{minutes}m rimanenti", "minutes" to remainingMin.toString()))
                    .setWhen(endTimeMillis)
                    .setShowWhen(true)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(android.app.Notification.CATEGORY_PROGRESS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setProgress(100, progressPercent, false)
                    .addExtras(extras)
                    .setContentIntent(contentPendingIntent(context))
                    .build()
            }

            buildHyperIslandExtras(context, label, endTimeMillis)?.let { islandExtras ->
                runCatching { notification.extras.putAll(islandExtras) }
            }

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TIMER_LIVE, notification)
        }

        fun cancelLiveCookingTimerNotification(context: Context) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_TIMER_LIVE)
        }

        fun showTimerFinishedNotification(
            context: Context,
            label: String,
            notificationId: Int = NOTIFICATION_ID_TIMER_DONE,
        ) {
            ensureChannels(context)
            cancelLiveCookingTimerNotification(context)

            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return

            AlarmSoundController.startAlarm(context)
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val stopIntent = Intent(context, TimerActionReceiver::class.java).apply {
                action = ACTION_STOP_TIMER_SOUND
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_TIMER_DONE)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(localized(context, "⏰ Tempo scaduto: {label}", "label" to label))
                .setContentText(localized(context, "Tocca per aprire Zing. Usa \"Ferma suoneria\" per interrompere il suono."))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(alarmUri)
                .setVibrate(longArrayOf(0, 500, 250, 500, 250, 800))
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent(context))
                .addAction(android.R.drawable.ic_media_pause, localized(context, "Ferma suoneria"), stopPendingIntent)
                .build()

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }

        fun stopRingingAndCancelNotification(context: Context, notificationId: Int) {
            AlarmSoundController.stopAlarm()
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}