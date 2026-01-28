package islamalorabi.shafeezekr.pbuh.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import islamalorabi.shafeezekr.pbuh.MainActivity
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.dataStore
import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "zikr_alert_channel_v2"
        private const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        val localizedContext = getLocalizedContext(context)
        createNotificationChannel(localizedContext)
        showNotification(localizedContext)
        playSound(context)
        ReminderScheduler.scheduleNextAlarm(context)
    }

    private fun getLocalizedContext(context: Context): Context {
        val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val locale = locales.get(0)
            if (locale != null) {
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(locale)
                return context.createConfigurationContext(config)
            }
        }
        return context
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
            .setColor(context.getColor(R.color.teal_700))
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun playSound(context: Context) {
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            val preferences = context.dataStore.data.first()
            val volume = preferences[floatPreferencesKey("app_volume")] ?: 1.0f
            val soundIndex = preferences[intPreferencesKey("selected_sound_index")] ?: 1
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                islamalorabi.shafeezekr.pbuh.service.SoundPlayer.play(context, soundIndex, volume)
            }
        }
    }
}

