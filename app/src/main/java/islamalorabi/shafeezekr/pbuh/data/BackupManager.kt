package islamalorabi.shafeezekr.pbuh.data

import android.content.Context
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Contents of an export file: daily dhikr counts and quiet-hours rules. */
data class BackupData(
    val dailyCounts: Map<String, Int>,
    val quietHoursRules: List<PeriodRule>
)

/** Result of an import: how much the file contained and how many rules were new. */
data class ImportResult(val days: Int, val newRules: Int)

class BackupManager(context: Context) {

    private val statsManager = DhikrStatsManager(context)
    private val preferencesManager = PreferencesManager(context)

    suspend fun exportJson(): String {
        val rules = preferencesManager.settingsFlow.first().periodRules
        return buildExport(BackupData(statsManager.getAllCounts(), rules))
    }

    /** Merges an export file into the saved data. Throws [IllegalArgumentException] for other files. */
    suspend fun importJson(json: String): ImportResult {
        val backup = parseExport(json)
        statsManager.replaceAllCounts(mergeCounts(statsManager.getAllCounts(), backup.dailyCounts))

        val existingRules = preferencesManager.settingsFlow.first().periodRules
        val mergedRules = mergeRules(existingRules, backup.quietHoursRules)
        if (mergedRules.size != existingRules.size) {
            preferencesManager.setPeriodRules(mergedRules)
        }
        return ImportResult(
            days = backup.dailyCounts.size,
            newRules = mergedRules.size - existingRules.size
        )
    }

    companion object {
        private const val EXPORT_FORMAT = "shafeezekr-stats"
        private const val EXPORT_VERSION = 2
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

        fun buildExport(data: BackupData): String {
            val days = JSONObject()
            data.dailyCounts.toSortedMap().forEach { (date, count) -> days.put(date, count) }
            val rules = JSONArray()
            data.quietHoursRules.forEach { rules.put(it.toJson()) }
            return JSONObject()
                .put("format", EXPORT_FORMAT)
                .put("version", EXPORT_VERSION)
                .put("dailyCounts", days)
                .put("quietHoursRules", rules)
                .toString(2)
        }

        /** Parses an export file; throws [IllegalArgumentException] if it isn't one. */
        fun parseExport(json: String): BackupData {
            val root = try {
                JSONObject(json)
            } catch (e: Exception) {
                throw IllegalArgumentException("Not a JSON file", e)
            }
            require(root.optString("format") == EXPORT_FORMAT) { "Not a statistics export" }
            val days = root.optJSONObject("dailyCounts") ?: throw IllegalArgumentException("Missing dailyCounts")

            val counts = mutableMapOf<String, Int>()
            val keys = days.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val date = runCatching { LocalDate.parse(key, DATE_FORMATTER) }.getOrNull() ?: continue
                val count = days.optInt(key, -1)
                if (count > 0) counts[date.format(DATE_FORMATTER)] = count
            }

            // Version 1 files have no rules.
            val rulesArray = root.optJSONArray("quietHoursRules") ?: JSONArray()
            val rules = (0 until rulesArray.length()).mapNotNull { i ->
                rulesArray.optJSONObject(i)?.let { runCatching { PeriodRule.fromJson(it) }.getOrNull() }
            }
            return BackupData(counts, rules)
        }

        /** Keeps the higher count per day, so importing the same file twice changes nothing. */
        fun mergeCounts(existing: Map<String, Int>, imported: Map<String, Int>): Map<String, Int> {
            val merged = existing.toMutableMap()
            imported.forEach { (date, count) -> merged[date] = maxOf(merged[date] ?: 0, count) }
            return merged
        }

        /** Adds imported rules that aren't already saved, matching by id. */
        fun mergeRules(existing: List<PeriodRule>, imported: List<PeriodRule>): List<PeriodRule> {
            val existingIds = existing.map { it.id }.toSet()
            return existing + imported.filter { it.id !in existingIds }.distinctBy { it.id }
        }
    }
}
