package islamalorabi.shafeezekr.pbuh.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AudioStreamType

object AudioHelper {

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
        onComplete: (() -> Unit)? = null
    ) {
        if (!shouldPlaySound(context, muteOnSilent, muteOnDND)) {
            onComplete?.invoke()
            return
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            val streamType = getStreamType(audioStreamType)
            val originalVolume = audioManager.getStreamVolume(streamType)
            if (!useSystemVolume) {
                val maxVolume = audioManager.getStreamMaxVolume(streamType)
                val targetVolume = (appVolume * maxVolume).toInt().coerceIn(0, maxVolume)
                audioManager.setStreamVolume(streamType, targetVolume, 0)
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(getUsageType(audioStreamType))
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(audioAttributes)
            
            if (isCustomSoundEnabled && !customSoundPath.isNullOrEmpty()) {
                val file = java.io.File(customSoundPath)
                if (file.exists()) {
                    mediaPlayer.setDataSource(file.absolutePath)
                } else {
                    val resId = getSoundResourceId(soundIndex)
                    val soundUri = Uri.parse("android.resource://${context.packageName}/$resId")
                    mediaPlayer.setDataSource(context, soundUri)
                }
            } else {
                val resId = getSoundResourceId(soundIndex)
                val soundUri = Uri.parse("android.resource://${context.packageName}/$resId")
                mediaPlayer.setDataSource(context, soundUri)
            }
            
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
            }
            
            mediaPlayer.setOnCompletionListener { mp ->
                if (!useSystemVolume) audioManager.setStreamVolume(streamType, originalVolume, 0)
                mp.release()
                onComplete?.invoke()
            }
            
            mediaPlayer.setOnErrorListener { mp, _, _ ->
                if (!useSystemVolume) audioManager.setStreamVolume(streamType, originalVolume, 0)
                mp.release()
                onComplete?.invoke()
                true
            }
            
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
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

        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            val streamType = getStreamType(audioStreamType)
            val originalVolume = audioManager.getStreamVolume(streamType)
            if (!useSystemVolume) {
                val maxVolume = audioManager.getStreamMaxVolume(streamType)
                val targetVolume = (appVolume * maxVolume).toInt().coerceIn(0, maxVolume)
                audioManager.setStreamVolume(streamType, targetVolume, 0)
            }

            val mediaPlayer = if (isCustomSoundEnabled && !customSoundPath.isNullOrEmpty()) {
                val file = java.io.File(customSoundPath)
                if (file.exists()) {
                    MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(getUsageType(audioStreamType))
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        setDataSource(file.absolutePath)
                        prepare()
                    }
                } else {
                    val resId = getSoundResourceId(soundIndex)
                    MediaPlayer.create(context, resId)
                }
            } else {
                val resId = getSoundResourceId(soundIndex)
                MediaPlayer.create(context, resId)
            }
            
            mediaPlayer?.setOnCompletionListener { mp ->
                if (!useSystemVolume) audioManager.setStreamVolume(streamType, originalVolume, 0)
                mp.release()
            }
            
            mediaPlayer?.start()
            mediaPlayer
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
