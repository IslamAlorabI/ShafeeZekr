package islamalorabi.shafeezekr.pbuh.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DhikrStatsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    companion object {
        private const val PREFS_NAME = "dhikr_stats"
        private const val KEY_DAILY_COUNTS = "daily_counts"
    }

    fun recordDhikr() {
        val today = LocalDate.now().format(dateFormatter)
        val counts = loadCounts()
        val current = counts.optInt(today, 0)
        counts.put(today, current + 1)
        saveCounts(counts)
    }

    fun getTodayCount(): Int {
        val today = LocalDate.now().format(dateFormatter)
        return loadCounts().optInt(today, 0)
    }

    fun getWeeklyData(): List<Pair<LocalDate, Int>> {
        val counts = loadCounts()
        val today = LocalDate.now()
        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val key = date.format(dateFormatter)
            date to counts.optInt(key, 0)
        }
    }

    fun getMonthlyTotal(): Int {
        val counts = loadCounts()
        val today = LocalDate.now()
        val yearMonth = "${today.year}-${String.format(java.util.Locale.US, "%02d", today.monthValue)}"
        var total = 0
        val keys = counts.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.startsWith(yearMonth)) {
                total += counts.optInt(key, 0)
            }
        }
        return total
    }

    fun getAllTimeTotal(): Int {
        val counts = loadCounts()
        var total = 0
        val keys = counts.keys()
        while (keys.hasNext()) {
            total += counts.optInt(keys.next(), 0)
        }
        return total
    }

    fun getCurrentStreak(): Int {
        val counts = loadCounts()
        var streak = 0
        var date = LocalDate.now()

        while (true) {
            val key = date.format(dateFormatter)
            if (counts.optInt(key, 0) > 0) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

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

    private fun saveCounts(counts: JSONObject) {
        prefs.edit().putString(KEY_DAILY_COUNTS, counts.toString()).apply()
    }
}
