package islamalorabi.shafeezekr.pbuh.util

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import islamalorabi.shafeezekr.pbuh.R

object AudioHelper {

    fun shouldPlaySound(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val ringerMode = audioManager.ringerMode
        if (ringerMode == AudioManager.RINGER_MODE_SILENT ||
            ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            return false
        }

        val interruptionFilter = notificationManager.currentInterruptionFilter
        if (interruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
            return false
        }

        return true
    }

    fun playWithMasterVolume(
        context: Context,
        soundIndex: Int,
        appVolume: Float,
        onComplete: (() -> Unit)? = null
    ) {
        if (!shouldPlaySound(context)) {
            onComplete?.invoke()
            return
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (appVolume * maxVolume).toInt().coerceIn(0, maxVolume)
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val resId = getSoundResourceId(soundIndex)
            val soundUri = Uri.parse("android.resource://${context.packageName}/$resId")

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(audioAttributes)
            mediaPlayer.setDataSource(context, soundUri)
            
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
            }
            
            mediaPlayer.setOnCompletionListener { mp ->
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                mp.release()
                onComplete?.invoke()
            }
            
            mediaPlayer.setOnErrorListener { mp, _, _ ->
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
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
        appVolume: Float
    ): MediaPlayer? {
        if (!shouldPlaySound(context)) {
            return null
        }

        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (appVolume * maxVolume).toInt().coerceIn(0, maxVolume)
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)

            val resId = getSoundResourceId(soundIndex)
            val mediaPlayer = MediaPlayer.create(context, resId)
            
            mediaPlayer?.setOnCompletionListener { mp ->
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
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
            else -> R.raw.zikr_sound_1
        }
    }
}
