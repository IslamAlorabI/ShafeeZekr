package islamalorabi.shafeezekr.pbuh.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val preferencesManager = PreferencesManager(context)
                val settings = preferencesManager.settingsFlow.first()
                
                if (settings.isReminderEnabled) {
                    val intervalMinutes = if (settings.reminderInterval == ReminderInterval.CUSTOM) {
                        settings.customIntervalMinutes
                    } else {
                        settings.reminderInterval.minutes
                    }
                    ReminderScheduler.startReminder(context, intervalMinutes)
                }
            }
        }
    }
}
