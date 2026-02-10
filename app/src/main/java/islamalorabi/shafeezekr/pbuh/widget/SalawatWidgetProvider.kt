package islamalorabi.shafeezekr.pbuh.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.util.AudioHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SalawatWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_SOUND = "islamalorabi.shafeezekr.pbuh.action.WIDGET_PLAY_SOUND"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_PLAY_SOUND) {
            playSound(context)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.salawat_widget)

        val playIntent = Intent(context, SalawatWidgetProvider::class.java).apply {
            action = ACTION_PLAY_SOUND
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun playSound(context: Context) {
        try {
            val preferencesManager = PreferencesManager(context)
            val settings = runBlocking {
                preferencesManager.settingsFlow.first()
            }
            AudioHelper.playWithMasterVolume(
                context = context,
                soundIndex = settings.selectedSoundIndex,
                appVolume = settings.appVolume
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
