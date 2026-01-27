package islamalorabi.shafeezekr.pbuh.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class ColorScheme { MONET, GREEN, BLUE, PURPLE, TEAL, ORANGE, PINK, RED }
enum class ReminderInterval(val minutes: Int) {
    FIVE(5),
    TEN(10),
    THIRTY(30),
    SIXTY(60),
    CUSTOM(-1)
}

data class AppSettings(
    val isReminderEnabled: Boolean = false,
    val reminderInterval: ReminderInterval = ReminderInterval.THIRTY,
    val customIntervalMinutes: Int = 15,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorScheme = ColorScheme.MONET,
    val languageCode: String = "",
    val appVolume: Float = 1.0f
)

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_INTERVAL = stringPreferencesKey("reminder_interval")
        val CUSTOM_INTERVAL_MINUTES = intPreferencesKey("custom_interval_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val APP_VOLUME = floatPreferencesKey("app_volume")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            isReminderEnabled = preferences[PreferencesKeys.REMINDER_ENABLED] ?: false,
            reminderInterval = try {
                ReminderInterval.valueOf(preferences[PreferencesKeys.REMINDER_INTERVAL] ?: "THIRTY")
            } catch (e: Exception) {
                ReminderInterval.THIRTY
            },
            customIntervalMinutes = preferences[PreferencesKeys.CUSTOM_INTERVAL_MINUTES] ?: 15,
            themeMode = try {
                ThemeMode.valueOf(preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM")
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            },
            colorScheme = try {
                ColorScheme.valueOf(preferences[PreferencesKeys.COLOR_SCHEME] ?: "MONET")
            } catch (e: Exception) {
                ColorScheme.MONET
            },
            languageCode = preferences[PreferencesKeys.LANGUAGE_CODE] ?: "",
            appVolume = preferences[PreferencesKeys.APP_VOLUME] ?: 1.0f
        )
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setReminderInterval(interval: ReminderInterval) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_INTERVAL] = interval.name
        }
    }

    suspend fun setCustomIntervalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_INTERVAL_MINUTES] = minutes
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setColorScheme(scheme: ColorScheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_SCHEME] = scheme.name
        }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = code
        }
    }

    suspend fun setAppVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }
}
