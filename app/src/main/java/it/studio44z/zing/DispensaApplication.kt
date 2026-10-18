package it.studio44z.zing

import android.app.Application
import it.studio44z.zing.notifications.ExpiryReminderWorker

class DispensaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ExpiryReminderWorker.ensureChannels(this)
    }
}