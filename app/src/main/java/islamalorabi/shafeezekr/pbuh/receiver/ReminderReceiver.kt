package islamalorabi.shafeezekr.pbuh.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.PorterDuff

import androidx.core.app.NotificationCompat

import islamalorabi.shafeezekr.pbuh.MainActivity
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.DhikrStatsManager
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils

import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import kotlinx.coroutines.flow.first

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "zikr_alert_channel_v2"
        const val NOTIFICATION_ID = 1
        // Broadcasts must finish within ~10s, so stop holding the receiver open before that
        private const val MAX_PLAYBACK_HOLD_MS = 9_000L

        fun createNotificationChannel(context: Context) {
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

        fun buildNotification(context: Context): Notification {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            val largeIcon = tintedLargeIcon(context)

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                // Padded, bolder copy of the artwork sized per density for the status bar
                .setSmallIcon(R.drawable.ic_stat_pbuh)
                // Without a color, Android 9 draws the header icon in pale gray
                .setColor(0xFF006E10.toInt())
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }

        // The artwork is near-white, so it vanishes on a light shade; tint it to the green
        // of the matching app theme (the shade follows the system dark mode, not the app's).
        private fun tintedLargeIcon(context: Context): Bitmap {
            val source = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.resources, R.drawable.ic_pbuh)
            ) { decoder, _, _ -> decoder.isMutableRequired = true }
            val isNight = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val tint = if (isNight) 0xFF8BD98B.toInt() else 0xFF006E10.toInt()
            Canvas(source).drawColor(tint, PorterDuff.Mode.SRC_IN)
            return source
        }

        fun pickSoundIndex(settings: AppSettings): Int =
            if (settings.shuffleSounds && !settings.isCustomSoundEnabled) {
                islamalorabi.shafeezekr.pbuh.util.AudioHelper.getRandomSoundIndex()
            } else {
                settings.selectedSoundIndex
            }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isQuietResume = intent.getBooleanExtra("quiet_hours_resume", false)

        if (isQuietResume) {
            ReminderScheduler.resumeFromQuietHours(context)
            return
        }

        val preferencesManager = PreferencesManager(context)
        val localizedContext = LocaleUtils.localizedContext(context)

        val settings = kotlinx.coroutines.runBlocking {
            preferencesManager.settingsFlow.first()
        }

        if (!settings.isReminderAllowedByPeriodRules()) {
            val quietEndMillis = settings.getQuietHoursEndMillis()
            ReminderScheduler.pauseForQuietHours(context, quietEndMillis)
            return
        }

        ReminderScheduler.clearQuietHoursPause(context)

        // Schedule the next reminder before notifying/playing, so a failure below can't break
        // the alarm chain and leave the countdown stuck at 00:00
        ReminderScheduler.scheduleNextAlarm(context)

        val blocked = isBlockedByMuteOptions(context, settings)

        if (!blocked) {
            createNotificationChannel(localizedContext)
            showNotification(localizedContext)

            val soundIndex = pickSoundIndex(settings)
            val durationMs = islamalorabi.shafeezekr.pbuh.util.AudioHelper.getSoundDurationMs(
                context, soundIndex, settings.customSoundPath, settings.isCustomSoundEnabled
            )

            // Sounds longer than a broadcast may stay alive are played from a short-lived
            // foreground service so they aren't cut off. If the system refuses to start it
            // (e.g. exact alarms not allowed), fall back to playing from here.
            val playedByService = durationMs > MAX_PLAYBACK_HOLD_MS &&
                islamalorabi.shafeezekr.pbuh.service.DhikrPlaybackService.start(context, soundIndex)

            if (!playedByService) {
                // Keep the process alive while the sound plays; otherwise, when the app is in the
                // background, the system may freeze/kill it right after onReceive returns and the
                // dhikr gets cut off mid-sentence.
                val pendingResult = goAsync()
                val finished = java.util.concurrent.atomic.AtomicBoolean(false)
                val finish = {
                    if (finished.compareAndSet(false, true)) pendingResult.finish()
                }
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(finish, MAX_PLAYBACK_HOLD_MS)

                playSound(
                    context,
                    settings,
                    soundIndex,
                    onStart = { DhikrStatsManager(context).recordDhikr() },
                    onComplete = {
                        if (settings.autoDismissNotification) {
                            val notificationManager = localizedContext.getSystemService(NotificationManager::class.java)
                            notificationManager.cancel(NOTIFICATION_ID)
                        }
                        finish()
                    }
                )
            }
        }
    }

    private fun isBlockedByMuteOptions(
        context: Context,
        settings: AppSettings
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
        if (!settings.useSystemVolume && settings.appVolume < 0.2f) {
            return true
        }
        return false
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(context))
    }

    private fun playSound(
        context: Context,
        settings: AppSettings,
        soundIndex: Int,
        onStart: () -> Unit,
        onComplete: () -> Unit
    ) {
        islamalorabi.shafeezekr.pbuh.util.AudioHelper.playWithMasterVolume(
            context = context,
            soundIndex = soundIndex,
            appVolume = settings.appVolume,
            muteOnSilent = settings.muteOnSilent,
            muteOnDND = settings.muteOnDND,
            customSoundPath = settings.customSoundPath,
            isCustomSoundEnabled = settings.isCustomSoundEnabled,
            audioStreamType = settings.audioStreamType,
            useSystemVolume = settings.useSystemVolume,
            onStart = onStart,
            onComplete = onComplete
        )
    }
}
