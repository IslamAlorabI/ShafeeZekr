package islamalorabi.shafeezekr.pbuh.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import islamalorabi.shafeezekr.pbuh.MainActivity

/** Launcher icons, each backed by an activity-alias in AndroidManifest.xml. */
enum class AppIcon(val aliasName: String, val enabledByDefault: Boolean) {
    CREAM("IconCream", true),
    GREEN("IconGreen", false),
    CLASSIC("IconClassic", false)
}

object AppIconManager {

    fun current(context: Context): AppIcon =
        AppIcon.entries.firstOrNull { isEnabled(context, it) } ?: AppIcon.CREAM

    fun set(context: Context, icon: AppIcon) {
        val pm = context.packageManager
        // Enable the new alias before disabling the others so the app never has zero launcher entries
        pm.setComponentEnabledSetting(
            component(context, icon),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        AppIcon.entries.filter { it != icon }.forEach {
            pm.setComponentEnabledSetting(
                component(context, it),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    private fun isEnabled(context: Context, icon: AppIcon): Boolean =
        when (context.packageManager.getComponentEnabledSetting(component(context, icon))) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> icon.enabledByDefault
            else -> false
        }

    // Alias names resolve against the manifest namespace, which is MainActivity's package
    private fun component(context: Context, icon: AppIcon) =
        ComponentName(context.packageName, "${MainActivity::class.java.name.substringBeforeLast('.')}.${icon.aliasName}")
}
