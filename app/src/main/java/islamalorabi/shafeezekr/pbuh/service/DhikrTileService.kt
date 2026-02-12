package islamalorabi.shafeezekr.pbuh.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import islamalorabi.shafeezekr.pbuh.data.dataStore
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

    private fun updateTileState() {
        val tile = qsTile ?: return
        val preferencesManager = PreferencesManager(applicationContext)

        val isEnabled = runBlocking {
            preferencesManager.settingsFlow.first().isReminderEnabled
        }

        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = if (isEnabled) {
            getString(R.string.tile_pause_dhikr_active)
        } else {
            getString(R.string.tile_pause_dhikr_inactive)
        }
        tile.updateTile()
    }
}
