package it.studio44z.zing.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_STOP_TIMER_RINGTONE) {
            stopRingtone()
            val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, ExpiryReminderWorker.NOTIFICATION_ID_TIMER_DONE)
            NotificationManagerCompat.from(context).cancel(notifId)
        }
    }

    companion object {
        const val ACTION_STOP_TIMER_RINGTONE = "it.studio44z.zing.ACTION_STOP_TIMER_RINGTONE"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        @Volatile
        private var activeRingtone: Ringtone? = null

        @Synchronized
        fun startRingtone(context: Context) {
            stopRingtone()
            runCatching {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
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
        fun stopRingtone() {
            runCatching {
                activeRingtone?.let {
                    if (it.isPlaying) it.stop()
                }
            }
            activeRingtone = null
        }
    }
}