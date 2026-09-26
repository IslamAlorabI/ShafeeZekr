package islamalorabi.shafeezekr.pbuh.receiver

import android.app.AlarmManager
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
        // Exact-alarm permission granted: swap the pending inexact alarm for an exact one
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            ReminderScheduler.rescheduleCurrent(context)
            return
        }

        // Alarms are cleared on reboot and when the app is updated, so reschedule in both cases
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Keep the receiver alive until the coroutine finishes, otherwise the process
            // may be killed before the alarm is rescheduled
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
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
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
