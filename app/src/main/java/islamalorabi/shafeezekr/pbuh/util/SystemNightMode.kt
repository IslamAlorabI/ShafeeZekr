package islamalorabi.shafeezekr.pbuh.util

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import islamalorabi.shafeezekr.pbuh.data.ThemeMode

/**
 * The system draws the splash screen before the app runs, so it only knows the in-app theme
 * if we hand it over. Android 12+ persists this per app and uses it for the next launch.
 */
object SystemNightMode {

    fun apply(context: Context, mode: ThemeMode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val uiModeManager = context.getSystemService(UiModeManager::class.java) ?: return
        uiModeManager.setApplicationNightMode(
            when (mode) {
                ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            }
        )
    }
}
