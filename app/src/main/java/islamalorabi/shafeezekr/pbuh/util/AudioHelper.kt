package islamalorabi.shafeezekr.pbuh.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AudioStreamType
import kotlin.math.roundToInt

object AudioHelper {

    const val BUILT_IN_SOUND_COUNT = 9

    fun getRandomSoundIndex(): Int = (1..BUILT_IN_SOUND_COUNT).random()

    private class ActivePlayer(val overriddenStream: Int?)
    private class VolumeHold(val originalVolume: Int, var holders: Int)

    private val lock = Any()

    // Strong references to players that are still playing. Without this, a MediaPlayer
    // created as a local variable can be garbage-collected (and finalized/released)
    // mid-playback, which cuts the sound off before it finishes.
    private val activePlayers = mutableMapOf<MediaPlayer, ActivePlayer>()
    private val volumeHolds = mutableMapOf<Int, VolumeHold>()

    private fun getStreamType(audioStreamType: AudioStreamType): Int {
        return when (audioStreamType) {
            AudioStreamType.MEDIA -> AudioManager.STREAM_MUSIC
            AudioStreamType.ALARM -> AudioManager.STREAM_ALARM
            AudioStreamType.NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
            AudioStreamType.RING -> AudioManager.STREAM_RING
        }
    }

    private fun getUsageType(audioStreamType: AudioStreamType): Int {
        return when (audioStreamType) {
            AudioStreamType.MEDIA -> AudioAttributes.USAGE_MEDIA
            AudioStreamType.ALARM -> AudioAttributes.USAGE_ALARM
            AudioStreamType.NOTIFICATION -> AudioAttributes.USAGE_NOTIFICATION
            AudioStreamType.RING -> AudioAttributes.USAGE_NOTIFICATION_RINGTONE
        }
    }

    fun getMaxVolume(context: Context, audioStreamType: AudioStreamType): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamMaxVolume(getStreamType(audioStreamType))
    }

    fun isInCall(context: Context): Boolean {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
            if (telecomManager.isInCall) return true
        } catch (_: SecurityException) { }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mode = audioManager.mode
        return mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION
    }

    fun isMediaPlaying(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.isMusicActive
    }

    fun shouldPlaySound(
        context: Context,
        muteOnSilent: Boolean = true,
        muteOnDND: Boolean = true
    ): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (muteOnSilent) {
            val ringerMode = audioManager.ringerMode
            if (ringerMode == AudioManager.RINGER_MODE_SILENT ||
                ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                return false
            }
        }

        if (muteOnDND) {
            val interruptionFilter = notificationManager.currentInterruptionFilter
            if (interruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                return false
            }
        }

        return true
    }

    fun playWithMasterVolume(
        context: Context,
        soundIndex: Int,
        appVolume: Float,
        muteOnSilent: Boolean = true,
        muteOnDND: Boolean = true,
        customSoundPath: String? = null,
        isCustomSoundEnabled: Boolean = false,
        audioStreamType: AudioStreamType = AudioStreamType.ALARM,
        useSystemVolume: Boolean = false,
        onStart: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        if (!shouldPlaySound(context, muteOnSilent, muteOnDND)) {
            onComplete?.invoke()
            return
        }

        var mediaPlayer: MediaPlayer? = null
        try {
            mediaPlayer = createPlayer(context, soundIndex, customSoundPath, isCustomSoundEnabled, audioStreamType)
            val player = mediaPlayer
            registerPlayer(context, player, audioStreamType, appVolume, useSystemVolume)

            player.setOnPreparedListener { mp ->
                mp.start()
                onStart?.invoke()
            }
            player.setOnCompletionListener { mp ->
                stopPlayer(context, mp)
                onComplete?.invoke()
            }
            player.setOnErrorListener { mp, _, _ ->
                stopPlayer(context, mp)
                onComplete?.invoke()
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer?.let { stopPlayer(context, it) }
            onComplete?.invoke()
        }
    }

    fun playWithMasterVolumeSync(
        context: Context,
        soundIndex: Int,
        appVolume: Float,
        muteOnSilent: Boolean = true,
        muteOnDND: Boolean = true,
        customSoundPath: String? = null,
        isCustomSoundEnabled: Boolean = false,
        audioStreamType: AudioStreamType = AudioStreamType.ALARM,
        useSystemVolume: Boolean = false
    ): MediaPlayer? {
        if (!shouldPlaySound(context, muteOnSilent, muteOnDND)) {
            return null
        }

        var mediaPlayer: MediaPlayer? = null
        return try {
            mediaPlayer = createPlayer(context, soundIndex, customSoundPath, isCustomSoundEnabled, audioStreamType)
            val player = mediaPlayer
            player.prepare()
            registerPlayer(context, player, audioStreamType, appVolume, useSystemVolume)
            player.setOnCompletionListener { mp -> stopPlayer(context, mp) }
            player.start()
            player
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer?.let { stopPlayer(context, it) }
            null
        }
    }

    /**
     * Stops and releases a player started by this helper and restores the stream volume
     * once no other sound of ours is still using it. Safe to call more than once.
     */
    fun stopPlayer(context: Context, mediaPlayer: MediaPlayer) {
        val entry = synchronized(lock) { activePlayers.remove(mediaPlayer) }
        try {
            mediaPlayer.release()
        } catch (_: Exception) { }
        if (entry?.overriddenStream != null) {
            restoreVolume(context, entry.overriddenStream)
        }
    }

    /** Duration of the sound that would be played, in ms, or 0 if unknown. */
    fun getSoundDurationMs(
        context: Context,
        soundIndex: Int,
        customSoundPath: String?,
        isCustomSoundEnabled: Boolean
    ): Long {
        val retriever = android.media.MediaMetadataRetriever()
        return try {
            val customFile = customSoundPath?.let { java.io.File(it) }
            if (isCustomSoundEnabled && customFile != null && customFile.exists()) {
                retriever.setDataSource(customFile.absolutePath)
            } else {
                retriever.setDataSource(context, getSoundUri(context, soundIndex))
            }
            retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }

    private fun createPlayer(
        context: Context,
        soundIndex: Int,
        customSoundPath: String?,
        isCustomSoundEnabled: Boolean,
        audioStreamType: AudioStreamType
    ): MediaPlayer {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(getUsageType(audioStreamType))
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            val customFile = customSoundPath?.let { java.io.File(it) }
            if (isCustomSoundEnabled && customFile != null && customFile.exists()) {
                mediaPlayer.setDataSource(customFile.absolutePath)
            } else {
                mediaPlayer.setDataSource(context, getSoundUri(context, soundIndex))
            }
        } catch (e: Exception) {
            mediaPlayer.release()
            throw e
        }
        return mediaPlayer
    }

    private fun getSoundUri(context: Context, soundIndex: Int): Uri =
        Uri.parse("android.resource://${context.packageName}/${getSoundResourceId(soundIndex)}")

    private fun registerPlayer(
        context: Context,
        mediaPlayer: MediaPlayer,
        audioStreamType: AudioStreamType,
        appVolume: Float,
        useSystemVolume: Boolean
    ) {
        val streamType = getStreamType(audioStreamType)
        synchronized(lock) {
            if (!useSystemVolume) overrideVolume(context, streamType, appVolume)
            activePlayers[mediaPlayer] = ActivePlayer(if (useSystemVolume) null else streamType)
        }
    }

    private fun overrideVolume(context: Context, streamType: Int, appVolume: Float) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        synchronized(lock) {
            // Only the first overlapping sound remembers the user's volume; later ones would
            // otherwise capture our own override and "restore" the stream to it.
            val hold = volumeHolds.getOrPut(streamType) {
                VolumeHold(originalVolume = audioManager.getStreamVolume(streamType), holders = 0)
            }
            hold.holders++
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val targetVolume = (appVolume * maxVolume).roundToInt().coerceIn(0, maxVolume)
            audioManager.setStreamVolume(streamType, targetVolume, 0)
        }
    }

    private fun restoreVolume(context: Context, streamType: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        synchronized(lock) {
            val hold = volumeHolds[streamType] ?: return
            hold.holders--
            if (hold.holders <= 0) {
                volumeHolds.remove(streamType)
                audioManager.setStreamVolume(streamType, hold.originalVolume, 0)
            }
        }
    }

    private fun getSoundResourceId(index: Int): Int {
        return when (index) {
            1 -> R.raw.zikr_sound_1
            2 -> R.raw.zikr_sound_2
            3 -> R.raw.zikr_sound_3
            4 -> R.raw.zikr_sound_4
            5 -> R.raw.zikr_sound_5
            6 -> R.raw.zikr_sound_6
            7 -> R.raw.zikr_sound_7
            8 -> R.raw.zikr_sound_8
            9 -> R.raw.zikr_sound_9
            else -> R.raw.zikr_sound_1
        }
    }
}
