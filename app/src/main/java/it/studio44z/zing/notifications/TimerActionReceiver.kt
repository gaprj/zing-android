package it.studio44z.zing.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ExpiryReminderWorker.ACTION_STOP_TIMER_SOUND) {
            val notificationId = intent.getIntExtra(
                ExpiryReminderWorker.EXTRA_NOTIFICATION_ID,
                ExpiryReminderWorker.NOTIFICATION_ID_TIMER_DONE,
            )
            ExpiryReminderWorker.stopRingingAndCancelNotification(context, notificationId)
        }
    }
}