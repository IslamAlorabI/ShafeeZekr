package islamalorabi.shafeezekr.pbuh.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.ColorFilter
import androidx.glance.appwidget.cornerRadius
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.util.AudioHelper
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import kotlinx.coroutines.flow.first

class SalawatWidget : GlanceAppWidget() {

    private fun getLocalizedContext(context: Context): Context {
        val preferencesManager = PreferencesManager(context)
        val languageCode = preferencesManager.getLanguageCodeSync()
        return LocaleUtils.updateResources(context, languageCode)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val localizedContext = getLocalizedContext(context)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(24.dp)
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable(actionRunCallback<PlaySoundAction>()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizedContext.getString(R.string.notification_title),
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = localizedContext.getString(R.string.home_subtitle),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_volume_up),
                            contentDescription = null,
                            modifier = GlanceModifier.size(18.dp),
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                        )

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        Text(
                            text = localizedContext.getString(R.string.tap_to_listen),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class SalawatWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SalawatWidget()
}

class PlaySoundAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val preferencesManager = PreferencesManager(context)
            val settings = preferencesManager.settingsFlow.first()
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
