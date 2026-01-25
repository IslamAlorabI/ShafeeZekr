package islamalorabi.shafeezekr.pbuh.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import islamalorabi.shafeezekr.pbuh.receiver.ReminderReceiver

object ReminderScheduler {
    private const val PREFS_NAME = "reminder_prefs"
    private const val KEY_NEXT_TRIGGER = "next_trigger_time"
    private const val KEY_INTERVAL = "interval_minutes"
    private const val KEY_ENABLED = "reminder_enabled"
    
    fun startReminder(context: Context, intervalMinutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_INTERVAL, intervalMinutes)
            .putBoolean(KEY_ENABLED, true)
            .apply()
        
        scheduleNextAlarm(context)
    }
    
    fun stopReminder(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, false)
            .remove(KEY_NEXT_TRIGGER)
            .apply()
        
        cancelAlarm(context)
    }
    
    fun scheduleNextAlarm(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        if (!prefs.getBoolean(KEY_ENABLED, false)) {
            return
        }
        
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
        
        try {
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
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTrigger,
                pendingIntent
            )
        }
    }
    
    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, false)
    }
}
