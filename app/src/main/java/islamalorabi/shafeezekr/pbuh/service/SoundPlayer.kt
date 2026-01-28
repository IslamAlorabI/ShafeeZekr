package islamalorabi.shafeezekr.pbuh.service

import android.content.Context
import android.media.MediaPlayer
import islamalorabi.shafeezekr.pbuh.R

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context, soundIndex: Int, volume: Float) {
        release()
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            // 1. Respect Privacy (Silent/Vibrate/DND)
            val ringerMode = audioManager.ringerMode
            if (ringerMode == android.media.AudioManager.RINGER_MODE_SILENT || 
                ringerMode == android.media.AudioManager.RINGER_MODE_VIBRATE) {
                return
            }

            // Check DND (Total Silence)
            val dndFilter = notificationManager.currentInterruptionFilter
            if (dndFilter == android.app.NotificationManager.INTERRUPTION_FILTER_NONE) {
                return
            }

            // 2. Manage Volume (Independent of current system level)
            val streamType = android.media.AudioManager.STREAM_MUSIC
            val maxVol = audioManager.getStreamMaxVolume(streamType)
            val originalVol = audioManager.getStreamVolume(streamType)
            
            // Calculate target volume step based on percentage
            // Ensure at least 1 if slider is not 0, otherwise 0
            val targetVol = if (volume <= 0f) 0 else (maxVol * volume).toInt().coerceAtLeast(1)
            
            if (targetVol > 0) {
                try {
                    audioManager.setStreamVolume(streamType, targetVol, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val resId = getSoundResourceId(soundIndex)
            val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/$resId")
            
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(context.applicationContext, soundUri)
                setOnPreparedListener { mp ->
                    // Set player volume to max (100% of the stream volume we just set)
                    mp.setVolume(1.0f, 1.0f)
                    mp.start()
                }
                setOnCompletionListener { 
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                    // Restore original system volume
                    try {
                        audioManager.setStreamVolume(streamType, originalVol, 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (mediaPlayer == mp) mediaPlayer = null
                    // Restore volume on error too
                    try {
                        audioManager.setStreamVolume(streamType, originalVol, 0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
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
