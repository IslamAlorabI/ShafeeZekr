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
enum class RuleScheduleType { DAILY_TIME, WEEKLY_DAYS, SPECIFIC_DATE }

data class PeriodRule(
    val id: String = UUID.randomUUID().toString(),
    val type: PeriodRuleType,
    val scheduleType: RuleScheduleType,
    val isEnabled: Boolean = true,
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 23,
    val endMinute: Int = 59,
    val daysOfWeek: Set<Int> = emptySet(),
    val year: Int = 0,
    val month: Int = 0,
    val dayOfMonth: Int = 0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("type", type.name)
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
            RuleScheduleType.DAILY_TIME -> {
                isTimeInRange(currentHour, currentMinute)
            }
            RuleScheduleType.WEEKLY_DAYS -> {
                if (currentDayOfWeek !in daysOfWeek) return false
                isTimeInRange(currentHour, currentMinute)
            }
            RuleScheduleType.SPECIFIC_DATE -> {
                if (currentYear != year || currentMonth != month || currentDay != dayOfMonth) return false
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
        if (type == other.type) return false
        
        when (scheduleType) {
            RuleScheduleType.DAILY_TIME -> {
                return timeRangesOverlap(other)
            }
            RuleScheduleType.WEEKLY_DAYS -> {
                val commonDays = daysOfWeek.intersect(other.daysOfWeek)
                if (commonDays.isEmpty()) return false
                return timeRangesOverlap(other)
            }
            RuleScheduleType.SPECIFIC_DATE -> {
                if (year != other.year || month != other.month || dayOfMonth != other.dayOfMonth) return false
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
        val timeRange = String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute)
        return when (scheduleType) {
            RuleScheduleType.DAILY_TIME -> timeRange
            RuleScheduleType.WEEKLY_DAYS -> {
                val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val days = daysOfWeek.sorted().map { dayNames[it] }.joinToString(", ")
                "$days | $timeRange"
            }
            RuleScheduleType.SPECIFIC_DATE -> {
                String.format("%04d-%02d-%02d | %s", year, month + 1, dayOfMonth, timeRange)
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
            
            val scheduleTypeStr = json.optString("scheduleType", "DAILY_TIME")
            
            return PeriodRule(
                id = json.getString("id"),
                type = PeriodRuleType.valueOf(json.getString("type")),
                scheduleType = RuleScheduleType.valueOf(scheduleTypeStr),
                isEnabled = json.optBoolean("isEnabled", true),
                startHour = json.optInt("startHour", 0),
                startMinute = json.optInt("startMinute", 0),
                endHour = json.optInt("endHour", 23),
                endMinute = json.optInt("endMinute", 59),
                daysOfWeek = days,
                year = json.optInt("year", 0),
                month = json.optInt("month", 0),
                dayOfMonth = json.optInt("dayOfMonth", 0)
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
