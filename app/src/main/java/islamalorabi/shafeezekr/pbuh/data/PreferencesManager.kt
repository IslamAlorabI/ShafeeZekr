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
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

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

enum class PeriodRuleType { ALLOW, BLOCK }

data class PeriodRule(
    val id: String = UUID.randomUUID().toString(),
    val type: PeriodRuleType,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("type", type.name)
            put("startHour", startHour)
            put("startMinute", startMinute)
            put("endHour", endHour)
            put("endMinute", endMinute)
            put("isEnabled", isEnabled)
        }
    }

    fun isCurrentTimeInRange(): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            currentTotalMinutes in startTotalMinutes..endTotalMinutes
        } else {
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
        }
    }

    fun conflictsWith(other: PeriodRule): Boolean {
        val startA = startHour * 60 + startMinute
        val endA = endHour * 60 + endMinute
        val startB = other.startHour * 60 + other.startMinute
        val endB = other.endHour * 60 + other.endMinute

        return if (startA <= endA && startB <= endB) {
            !(endA < startB || endB < startA)
        } else if (startA > endA && startB > endB) {
            true
        } else {
            true
        }
    }

    companion object {
        fun fromJson(json: JSONObject): PeriodRule {
            return PeriodRule(
                id = json.getString("id"),
                type = PeriodRuleType.valueOf(json.getString("type")),
                startHour = json.getInt("startHour"),
                startMinute = json.getInt("startMinute"),
                endHour = json.getInt("endHour"),
                endMinute = json.getInt("endMinute"),
                isEnabled = json.optBoolean("isEnabled", true)
            )
        }
    }
}

data class AppSettings(
    val isReminderEnabled: Boolean = false,
    val reminderInterval: ReminderInterval = ReminderInterval.THIRTY,
    val customIntervalMinutes: Int = 15,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorScheme: ColorScheme = ColorScheme.MONET,
    val languageCode: String = "",
    val appVolume: Float = 1.0f,
    val periodRules: List<PeriodRule> = emptyList()
) {
    fun isReminderAllowedByPeriodRules(): Boolean {
        val enabledRules = periodRules.filter { it.isEnabled }
        if (enabledRules.isEmpty()) return true

        val enabledAllowRules = enabledRules.filter { it.type == PeriodRuleType.ALLOW }
        val enabledBlockRules = enabledRules.filter { it.type == PeriodRuleType.BLOCK }

        for (blockRule in enabledBlockRules) {
            if (blockRule.isCurrentTimeInRange()) {
                return false
            }
        }

        if (enabledAllowRules.isNotEmpty()) {
            for (allowRule in enabledAllowRules) {
                if (allowRule.isCurrentTimeInRange()) {
                    return true
                }
            }
            return false
        }

        return true
    }
}

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_INTERVAL = stringPreferencesKey("reminder_interval")
        val CUSTOM_INTERVAL_MINUTES = intPreferencesKey("custom_interval_minutes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val APP_VOLUME = floatPreferencesKey("app_volume")
        val PERIOD_RULES = stringPreferencesKey("period_rules")
    }

    private fun parsePeriodRules(json: String): List<PeriodRule> {
        if (json.isEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { PeriodRule.fromJson(array.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun periodRulesToJson(rules: List<PeriodRule>): String {
        val array = JSONArray()
        rules.forEach { array.put(it.toJson()) }
        return array.toString()
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
            appVolume = preferences[PreferencesKeys.APP_VOLUME] ?: 1.0f,
            periodRules = parsePeriodRules(preferences[PreferencesKeys.PERIOD_RULES] ?: "")
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

    suspend fun setPeriodRules(rules: List<PeriodRule>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERIOD_RULES] = periodRulesToJson(rules)
        }
    }
}
