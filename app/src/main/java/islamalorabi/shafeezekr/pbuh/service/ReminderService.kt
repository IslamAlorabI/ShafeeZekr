package islamalorabi.shafeezekr.pbuh.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
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
        private const val PREFS_NAME = "reminder_prefs"
        private const val KEY_NEXT_TRIGGER = "next_trigger_time"
        private const val KEY_INTERVAL = "interval_minutes"

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
        
        fun scheduleNextAlarm(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val intervalMinutes = prefs.getInt(KEY_INTERVAL, 30)
            val intervalMillis = intervalMinutes * 60 * 1000L
            val nextTrigger = System.currentTimeMillis() + intervalMillis
            
            prefs.edit().putLong(KEY_NEXT_TRIGGER, nextTrigger).apply()
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger,
                    pendingIntent
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        
        when (intent?.action) {
            ACTION_START -> {
                val intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MINUTES, 30)
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putInt(KEY_INTERVAL, intervalMinutes).apply()
                
                scheduleNextAlarm(this)
            }
            ACTION_STOP -> {
                cancelAlarm()
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_NEXT_TRIGGER).apply()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
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
