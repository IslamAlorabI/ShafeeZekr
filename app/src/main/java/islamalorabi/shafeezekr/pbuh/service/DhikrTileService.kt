package islamalorabi.shafeezekr.pbuh.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import islamalorabi.shafeezekr.pbuh.data.dataStore
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DhikrTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val preferencesManager = PreferencesManager(applicationContext)

        // Reminders don't run without "Alarms & reminders": send the user to grant it instead
        val isEnabled = runBlocking { preferencesManager.settingsFlow.first().isReminderEnabled }
        if (!isEnabled && !canScheduleExactAlarms()) {
            openExactAlarmSettings()
            return
        }

        runBlocking {
            val settings = preferencesManager.settingsFlow.first()
            val newEnabled = !settings.isReminderEnabled
            preferencesManager.setReminderEnabled(newEnabled)

            if (newEnabled) {
                val intervalMinutes = if (settings.reminderInterval == ReminderInterval.CUSTOM) {
                    settings.customIntervalMinutes
                } else {
                    settings.reminderInterval.minutes
                }
                ReminderScheduler.startReminder(applicationContext, intervalMinutes)
            } else {
                ReminderScheduler.stopReminder(applicationContext)
            }
        }

        updateTileState()
    }

    private fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun openExactAlarmSettings() {
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.fromParts("package", packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // Android 14 requires a PendingIntent here; older versions only accept an Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val preferencesManager = PreferencesManager(applicationContext)

        val isEnabled = runBlocking {
            preferencesManager.settingsFlow.first().isReminderEnabled
        }

        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        // Tile subtitles exist from Android 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val localizedContext = LocaleUtils.localizedContext(this)
            tile.subtitle = if (isEnabled) {
                localizedContext.getString(R.string.tile_pause_dhikr_active)
            } else {
                localizedContext.getString(R.string.tile_pause_dhikr_inactive)
            }
        }
        tile.updateTile()
    }
}
