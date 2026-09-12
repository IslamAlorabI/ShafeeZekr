package islamalorabi.shafeezekr.pbuh.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DhikrStatsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dhikr_stats"
        private const val KEY_DAILY_COUNTS = "daily_counts"
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

        fun computeStreak(counts: Map<String, Int>, today: LocalDate): Int {
            var streak = 0
            var date = today

            while (counts.getOrDefault(date.format(DATE_FORMATTER), 0) > 0) {
                streak++
                date = date.minusDays(1)
            }
            return streak
        }

        fun computeWeeklyData(counts: Map<String, Int>, today: LocalDate): List<Pair<LocalDate, Int>> {
            return (6 downTo 0).map { daysAgo ->
                val date = today.minusDays(daysAgo.toLong())
                date to counts.getOrDefault(date.format(DATE_FORMATTER), 0)
            }
        }

        fun computeMonthlyTotal(counts: Map<String, Int>, today: LocalDate): Int {
            val yearMonth = "${today.year}-${String.format(java.util.Locale.US, "%02d", today.monthValue)}"
            return counts.entries
                .filter { it.key.startsWith(yearMonth) }
                .sumOf { it.value }
        }

        fun computeAllTimeTotal(counts: Map<String, Int>): Int = counts.values.sum()
    }

    fun recordDhikr() {
        val today = LocalDate.now().format(DATE_FORMATTER)
        val counts = loadCounts()
        val current = counts.optInt(today, 0)
        counts.put(today, current + 1)
        saveCounts(counts)
    }

    fun getTodayCount(): Int {
        val today = LocalDate.now().format(DATE_FORMATTER)
        return loadCounts().optInt(today, 0)
    }

    fun getWeeklyData(): List<Pair<LocalDate, Int>> =
        computeWeeklyData(toMap(loadCounts()), LocalDate.now())

    fun getMonthlyTotal(): Int =
        computeMonthlyTotal(toMap(loadCounts()), LocalDate.now())

    fun getAllTimeTotal(): Int =
        computeAllTimeTotal(toMap(loadCounts()))

    fun getCurrentStreak(): Int =
        computeStreak(toMap(loadCounts()), LocalDate.now())

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun loadCounts(): JSONObject {
        val json = prefs.getString(KEY_DAILY_COUNTS, null) ?: return JSONObject()
        return try {
            JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun toMap(counts: JSONObject): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val keys = counts.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = counts.optInt(key, 0)
        }
        return map
    }

    private fun saveCounts(counts: JSONObject) {
        prefs.edit().putString(KEY_DAILY_COUNTS, counts.toString()).apply()
    }
}
