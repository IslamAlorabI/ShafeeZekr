package islamalorabi.shafeezekr.pbuh.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ServiceCompat
import islamalorabi.shafeezekr.pbuh.data.DhikrStatsManager
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.receiver.ReminderReceiver
import islamalorabi.shafeezekr.pbuh.util.AudioHelper
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Plays a reminder sound that is too long for a broadcast receiver to keep the process alive for.
 * Runs in the foreground only while the sound plays, reusing the reminder notification.
 */
class DhikrPlaybackService : Service() {

    companion object {
        private const val EXTRA_SOUND_INDEX = "sound_index"
        // Stop even if the player never reports completion
        private const val SAFETY_MARGIN_MS = 5_000L
        private const val MAX_PLAYBACK_MS = 5 * 60_000L

        fun start(context: Context, soundIndex: Int): Boolean {
            return try {
                val intent = Intent(context, DhikrPlaybackService::class.java)
                    .putExtra(EXTRA_SOUND_INDEX, soundIndex)
                context.startForegroundService(intent)
                true
            } catch (e: Exception) {
                // Background start not allowed (e.g. alarm wasn't exact)
                e.printStackTrace()
                false
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var activePlaybacks = 0
    private var autoDismiss = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val preferencesManager = PreferencesManager(applicationContext)
        val localizedContext = LocaleUtils.localizedContext(applicationContext)

        try {
            ReminderReceiver.createNotificationChannel(localizedContext)
            // ServiceCompat drops the service type below Android 10, where it doesn't exist
            ServiceCompat.startForeground(
                this,
                ReminderReceiver.NOTIFICATION_ID,
                ReminderReceiver.buildNotification(localizedContext),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (activePlaybacks == 0) stopSelf()
            return START_NOT_STICKY
        }

        val settings = runBlocking { preferencesManager.settingsFlow.first() }
        val soundIndex = intent?.getIntExtra(EXTRA_SOUND_INDEX, settings.selectedSoundIndex)
            ?: settings.selectedSoundIndex
        autoDismiss = settings.autoDismissNotification

        activePlaybacks++
        var finished = false
        val onFinished = {
            if (!finished) {
                finished = true
                onPlaybackFinished()
            }
        }

        val durationMs = AudioHelper.getSoundDurationMs(
            applicationContext, soundIndex, settings.customSoundPath, settings.isCustomSoundEnabled
        )
        val timeoutMs = if (durationMs > 0) durationMs + SAFETY_MARGIN_MS else MAX_PLAYBACK_MS
        handler.postDelayed(onFinished, timeoutMs.coerceAtMost(MAX_PLAYBACK_MS))

        AudioHelper.playWithMasterVolume(
            context = applicationContext,
            soundIndex = soundIndex,
            appVolume = settings.appVolume,
            muteOnSilent = settings.muteOnSilent,
            muteOnDND = settings.muteOnDND,
            customSoundPath = settings.customSoundPath,
            isCustomSoundEnabled = settings.isCustomSoundEnabled,
            audioStreamType = settings.audioStreamType,
            useSystemVolume = settings.useSystemVolume,
            onStart = { DhikrStatsManager(applicationContext).recordDhikr() },
            onComplete = { handler.post(onFinished) }
        )

        return START_NOT_STICKY
    }

    private fun onPlaybackFinished() {
        activePlaybacks--
        if (activePlaybacks > 0) return

        // Keep the reminder notification unless the user chose to auto-dismiss it
        stopForeground(if (autoDismiss) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
