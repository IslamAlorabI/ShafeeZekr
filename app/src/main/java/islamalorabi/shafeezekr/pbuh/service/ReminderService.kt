package islamalorabi.shafeezekr.pbuh.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import islamalorabi.shafeezekr.pbuh.MainActivity
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.receiver.ReminderReceiver

class ReminderService : Service() {

    companion object {
        const val CHANNEL_ID = "zikr_reminder_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "islamalorabi.shafeezekr.pbuh.ACTION_START"
        const val ACTION_STOP = "islamalorabi.shafeezekr.pbuh.ACTION_STOP"
        const val EXTRA_INTERVAL_MINUTES = "interval_minutes"

        fun startService(context: Context, intervalMinutes: Int) {
            val intent = Intent(context, ReminderService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_INTERVAL_MINUTES, intervalMinutes)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ReminderService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MINUTES, 30)
                startForegroundNotification()
                scheduleAlarm(intervalMinutes)
            }
            ACTION_STOP -> {
                cancelAlarm()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        cancelAlarm()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.enable_reminder_desc))
            .setSmallIcon(R.drawable.ic_volume_up)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun scheduleAlarm(intervalMinutes: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalMinutes * 60 * 1000L
        val triggerAt = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            intervalMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
