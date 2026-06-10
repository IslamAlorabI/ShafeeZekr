package islamalorabi.shafeezekr.pbuh.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.graphics.ImageDecoder

import androidx.core.app.NotificationCompat

import islamalorabi.shafeezekr.pbuh.MainActivity
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.DhikrStatsManager
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils

import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import kotlinx.coroutines.flow.first

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "zikr_alert_channel_v2"
        private const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isQuietResume = intent.getBooleanExtra("quiet_hours_resume", false)

        if (isQuietResume) {
            ReminderScheduler.resumeFromQuietHours(context)
            return
        }

        val preferencesManager = PreferencesManager(context)
        val languageCode = preferencesManager.getLanguageCodeSync()
        val localizedContext = LocaleUtils.updateResources(context, languageCode)

        val settings = kotlinx.coroutines.runBlocking {
            preferencesManager.settingsFlow.first()
        }

        if (!settings.isReminderAllowedByPeriodRules()) {
            val quietEndMillis = settings.getQuietHoursEndMillis()
            ReminderScheduler.pauseForQuietHours(context, quietEndMillis)
            return
        }

        ReminderScheduler.clearQuietHoursPause(context)

        val blocked = isBlockedByMuteOptions(context, settings)

        if (!blocked) {
            createNotificationChannel(localizedContext)
            showNotification(localizedContext)
            val didPlay = playSound(context, settings)
            if (didPlay) {
                DhikrStatsManager(context).recordDhikr()
            }
        }

        ReminderScheduler.scheduleNextAlarm(context)
    }

    private fun isBlockedByMuteOptions(
        context: Context,
        settings: islamalorabi.shafeezekr.pbuh.data.PreferencesManager.AppSettings
    ): Boolean {
        if (settings.muteOnCall && islamalorabi.shafeezekr.pbuh.util.AudioHelper.isInCall(context)) {
            return true
        }
        if (!islamalorabi.shafeezekr.pbuh.util.AudioHelper.shouldPlaySound(context, settings.muteOnSilent, settings.muteOnDND)) {
            return true
        }
        if (settings.muteOnMedia && islamalorabi.shafeezekr.pbuh.util.AudioHelper.isMediaPlaying(context)) {
            return true
        }
        if (settings.appVolume < 0.2f) {
            return true
        }
        return false
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            setSound(null, null)
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(context: Context) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(context.resources, R.drawable.ic_pbuh)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_pbuh_white)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun playSound(context: Context, settings: islamalorabi.shafeezekr.pbuh.data.PreferencesManager.AppSettings): Boolean {
        try {
            islamalorabi.shafeezekr.pbuh.util.AudioHelper.playWithMasterVolume(
                context = context,
                soundIndex = settings.selectedSoundIndex,
                appVolume = settings.appVolume,
                muteOnSilent = settings.muteOnSilent,
                muteOnDND = settings.muteOnDND,
                customSoundPath = settings.customSoundPath,
                isCustomSoundEnabled = settings.isCustomSoundEnabled,
                audioStreamType = settings.audioStreamType
            )
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
