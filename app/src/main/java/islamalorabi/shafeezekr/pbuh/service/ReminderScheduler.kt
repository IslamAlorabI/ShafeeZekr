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
    private const val KEY_REMAINING_MS = "remaining_ms"
    private const val KEY_PAUSED_BY_QUIET = "paused_by_quiet_hours"
    private const val QUIET_RESUME_REQUEST_CODE = 9999
    // How late an alarm may be before we assume it was deferred or lost
    private const val OVERDUE_GRACE_MS = 10_000L
    
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
        val nextTrigger = prefs.getLong(KEY_NEXT_TRIGGER, 0L)
        val now = System.currentTimeMillis()
        val remaining = if (nextTrigger > now) nextTrigger - now else 0L
        prefs.edit()
            .putBoolean(KEY_ENABLED, false)
            .putLong(KEY_REMAINING_MS, remaining)
            .remove(KEY_NEXT_TRIGGER)
            .apply()
        
        cancelAlarm(context)
    }
    
    fun resumeReminder(context: Context, intervalMinutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedRemaining = prefs.getLong(KEY_REMAINING_MS, 0L)
        prefs.edit()
            .putInt(KEY_INTERVAL, intervalMinutes)
            .putBoolean(KEY_ENABLED, true)
            .remove(KEY_REMAINING_MS)
            .apply()
        
        if (savedRemaining > 0L) {
            scheduleAlarmAt(context, System.currentTimeMillis() + savedRemaining)
        } else {
            scheduleNextAlarm(context)
        }
    }
    
    fun scheduleNextAlarm(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        if (!prefs.getBoolean(KEY_ENABLED, false)) {
            return
        }
        
        val intervalMinutes = prefs.getInt(KEY_INTERVAL, 30)
        val intervalMillis = intervalMinutes * 60 * 1000L
        val nextTrigger = System.currentTimeMillis() + intervalMillis
        scheduleAlarmAt(context, nextTrigger)
        
    }
    
    /**
     * Re-registers the pending reminder at its current trigger time, e.g. after the exact-alarm
     * permission is granted, so it switches from an inexact to an exact alarm without
     * restarting the countdown.
     */
    fun rescheduleCurrent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, false)) return
        if (prefs.getBoolean(KEY_PAUSED_BY_QUIET, false)) return

        val nextTrigger = prefs.getLong(KEY_NEXT_TRIGGER, 0L)
        if (nextTrigger > System.currentTimeMillis()) {
            scheduleAlarmAt(context, nextTrigger)
        } else {
            scheduleNextAlarm(context)
        }
    }

    /**
     * Fires the reminder now if its alarm is overdue. Inexact alarms (exact-alarm permission
     * not granted) can be deferred by many minutes, leaving the countdown at 00:00. A lost
     * alarm is rescheduled from now without firing. Returns true if a reminder was triggered.
     */
    fun recoverIfOverdue(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, false)) return false
        if (prefs.getBoolean(KEY_PAUSED_BY_QUIET, false)) return false

        val nextTrigger = prefs.getLong(KEY_NEXT_TRIGGER, 0L)
        val now = System.currentTimeMillis()
        // Force-stop (including every install from Android Studio) and app updates cancel the
        // alarm's PendingIntent but leave next_trigger_time behind. That's a lost alarm, not a
        // due reminder: start a fresh countdown instead of firing immediately.
        if (nextTrigger == 0L || !isAlarmPending(context)) {
            scheduleNextAlarm(context)
            return false
        }
        if (now - nextTrigger < OVERDUE_GRACE_MS) return false

        // Push next_trigger forward right away so the next UI tick doesn't fire again
        // before the receiver has rescheduled
        prefs.edit().putLong(KEY_NEXT_TRIGGER, now + prefs.getInt(KEY_INTERVAL, 30) * 60 * 1000L).apply()
        context.sendBroadcast(Intent(context, ReminderReceiver::class.java))
        return true
    }

    private fun isAlarmPending(context: Context): Boolean =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) != null

    private fun scheduleAlarmAt(context: Context, triggerAtMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_NEXT_TRIGGER, triggerAtMillis).apply()
        
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
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
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

    fun isPausedForQuietHours(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PAUSED_BY_QUIET, false)
    }

    fun pauseForQuietHours(context: Context, quietEndMillis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_PAUSED_BY_QUIET, true)
            .remove(KEY_NEXT_TRIGGER)
            .apply()

        cancelAlarm(context)

        if (quietEndMillis > System.currentTimeMillis()) {
            scheduleQuietHoursResume(context, quietEndMillis)
        }
    }

    fun resumeFromQuietHours(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_PAUSED_BY_QUIET, false)) return
        if (!prefs.getBoolean(KEY_ENABLED, false)) return

        prefs.edit()
            .putBoolean(KEY_PAUSED_BY_QUIET, false)
            .apply()

        scheduleNextAlarm(context)
    }

    private fun scheduleQuietHoursResume(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("quiet_hours_resume", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            QUIET_RESUME_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelQuietHoursResume(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            QUIET_RESUME_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun clearQuietHoursPause(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_PAUSED_BY_QUIET, false)
            .apply()
        cancelQuietHoursResume(context)
    }
}
