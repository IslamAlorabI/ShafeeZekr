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
    ONE(1),
    FIVE(5),
    TEN(10),
    THIRTY(30),
    SIXTY(60),
    TWO_HOURS(120),
    CUSTOM(-1)
}

enum class RuleScheduleType { WEEKLY_DAYS, SPECIFIC_DATE }

data class PeriodRule(
    val id: String = UUID.randomUUID().toString(),
    val scheduleType: RuleScheduleType,
    val isEnabled: Boolean = true,
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 23,
    val endMinute: Int = 59,
    val daysOfWeek: Set<Int> = emptySet(),
    val year: Int = 0,
    val month: Int = 0,
    val dayOfMonth: Int = 0,
    val isAllDay: Boolean = false
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("scheduleType", scheduleType.name)
            put("isEnabled", isEnabled)
            put("startHour", startHour)
            put("startMinute", startMinute)
            put("endHour", endHour)
            put("endMinute", endMinute)
            put("daysOfWeek", JSONArray(daysOfWeek.toList()))
            put("year", year)
            put("month", month)
            put("dayOfMonth", dayOfMonth)
            put("isAllDay", isAllDay)
        }
    }

    fun isCurrentTimeInRange(): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        return when (scheduleType) {
            RuleScheduleType.WEEKLY_DAYS -> {
                if (currentDayOfWeek !in daysOfWeek) return false
                if (isAllDay) return true
                isTimeInRange(currentHour, currentMinute)
            }
            RuleScheduleType.SPECIFIC_DATE -> {
                if (currentYear != year || currentMonth != month || currentDay != dayOfMonth) return false
                if (isAllDay) return true
                isTimeInRange(currentHour, currentMinute)
            }
        }
    }

    private fun isTimeInRange(currentHour: Int, currentMinute: Int): Boolean {
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
        if (scheduleType != other.scheduleType) return false
        
        when (scheduleType) {
            RuleScheduleType.WEEKLY_DAYS -> {
                val commonDays = daysOfWeek.intersect(other.daysOfWeek)
                if (commonDays.isEmpty()) return false
                if (isAllDay || other.isAllDay) return true
                return timeRangesOverlap(other)
            }
            RuleScheduleType.SPECIFIC_DATE -> {
                if (year != other.year || month != other.month || dayOfMonth != other.dayOfMonth) return false
                if (isAllDay || other.isAllDay) return true
                return timeRangesOverlap(other)
            }
        }
    }

    private fun timeRangesOverlap(other: PeriodRule): Boolean {
        val startA = startHour * 60 + startMinute
        val endA = endHour * 60 + endMinute
        val startB = other.startHour * 60 + other.startMinute
        val endB = other.endHour * 60 + other.endMinute

        return if (startA <= endA && startB <= endB) {
            !(endA < startB || endB < startA)
        } else {
            true
        }
    }

    fun getDisplayText(): String {
        val timeRange = if (isAllDay) {
            "00:00 - 23:59"
        } else {
            String.format(java.util.Locale.getDefault(), "%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute)
        }
        return when (scheduleType) {
            RuleScheduleType.WEEKLY_DAYS -> {
                val days = if (daysOfWeek.size == 7) {
                    "All week"
                } else {
                    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    daysOfWeek.sorted().map { dayNames[it] }.joinToString(", ")
                }
                "$days | $timeRange"
            }
            RuleScheduleType.SPECIFIC_DATE -> {
                String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d | %s", year, month + 1, dayOfMonth, timeRange)
            }
        }
    }

    companion object {
        fun fromJson(json: JSONObject): PeriodRule {
            val daysArray = json.optJSONArray("daysOfWeek")
            val days = mutableSetOf<Int>()
            if (daysArray != null) {
                for (i in 0 until daysArray.length()) {
                    days.add(daysArray.getInt(i))
                }
            }
            
            val scheduleTypeStr = json.optString("scheduleType", "WEEKLY_DAYS")
            val scheduleType = try {
                RuleScheduleType.valueOf(scheduleTypeStr)
            } catch (e: Exception) {
                RuleScheduleType.WEEKLY_DAYS
            }
            
            return PeriodRule(
                id = json.getString("id"),
                scheduleType = scheduleType,
                isEnabled = json.optBoolean("isEnabled", true),
                startHour = json.optInt("startHour", 0),
                startMinute = json.optInt("startMinute", 0),
                endHour = json.optInt("endHour", 23),
                endMinute = json.optInt("endMinute", 59),
                daysOfWeek = days,
                year = json.optInt("year", 0),
                month = json.optInt("month", 0),
                dayOfMonth = json.optInt("dayOfMonth", 0),
                isAllDay = json.optBoolean("isAllDay", false)
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
    val selectedSoundIndex: Int = 1,
    val periodRules: List<PeriodRule> = emptyList()
) {
    fun isReminderAllowedByPeriodRules(): Boolean {
        // All rules are now "Block" rules.
        // If ANY enabled rule covers the current time, reminder is BLOCKED (return false).
        val enabledRules = periodRules.filter { it.isEnabled }
        
        for (rule in enabledRules) {
            if (rule.isCurrentTimeInRange()) {
                return false
            }
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
        val SELECTED_SOUND_INDEX = intPreferencesKey("selected_sound_index")
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
            selectedSoundIndex = preferences[PreferencesKeys.SELECTED_SOUND_INDEX] ?: 1,
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

    suspend fun setSelectedSoundIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_SOUND_INDEX] = index
        }
    }
}
