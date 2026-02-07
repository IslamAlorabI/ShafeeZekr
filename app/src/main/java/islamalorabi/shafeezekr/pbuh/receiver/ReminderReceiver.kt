package islamalorabi.shafeezekr.pbuh.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory

import androidx.core.app.NotificationCompat

import islamalorabi.shafeezekr.pbuh.MainActivity
import islamalorabi.shafeezekr.pbuh.R
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
        val preferencesManager = PreferencesManager(context)
        val languageCode = preferencesManager.getLanguageCodeSync()
        val localizedContext = LocaleUtils.updateResources(context, languageCode)
        
        createNotificationChannel(localizedContext)
        showNotification(localizedContext)
        playSound(context)
        ReminderScheduler.scheduleNextAlarm(context)
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

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_pbuh)

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

    private fun playSound(context: Context) {
        try {
            val preferencesManager = islamalorabi.shafeezekr.pbuh.data.PreferencesManager(context)
            val settings = kotlinx.coroutines.runBlocking {
                preferencesManager.settingsFlow.first()
            }
            
            if (!settings.isReminderAllowedByPeriodRules()) {
                return
            }

            islamalorabi.shafeezekr.pbuh.util.AudioHelper.playWithMasterVolume(
                context = context,
                soundIndex = settings.selectedSoundIndex,
                appVolume = settings.appVolume
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

