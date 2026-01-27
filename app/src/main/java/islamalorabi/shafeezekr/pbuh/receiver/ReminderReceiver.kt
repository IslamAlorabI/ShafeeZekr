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

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "zikr_alert_channel_v2"
        private const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        showNotification(context)
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
            val preferences = kotlinx.coroutines.runBlocking {
                context.dataStore.data.first()
            }
            val volume = preferences[floatPreferencesKey("app_volume")] ?: 1.0f
            val soundIndex = preferences[intPreferencesKey("selected_sound_index")] ?: 1

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(audioAttributes)
            
            val resId = context.resources.getIdentifier("zikr_sound_$soundIndex", "raw", context.packageName)
            val soundUri = Uri.parse("android.resource://${context.packageName}/$resId")
            
            mediaPlayer.setDataSource(context, soundUri)
            mediaPlayer.setOnPreparedListener { mp ->
                mp.setVolume(volume, volume)
                mp.start()
            }
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
            mediaPlayer.setOnErrorListener { mp, _, _ ->
                mp.release()
                true
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

