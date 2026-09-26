package islamalorabi.shafeezekr.pbuh.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupManagerTest {

    private val rule = PeriodRule(
        id = "rule-1",
        scheduleType = RuleScheduleType.WEEKLY_DAYS,
        startHour = 22,
        endHour = 6,
        daysOfWeek = setOf(5, 6)
    )

    @Test
    fun `export then parse returns the same counts and rules`() {
        val data = BackupData(mapOf("2026-09-08" to 4, "2026-09-09" to 12), listOf(rule))
        assertEquals(data, BackupManager.parseExport(BackupManager.buildExport(data)))
    }

    @Test
    fun `version 1 files without rules still import`() {
        val json = """{"format": "shafeezekr-stats", "version": 1, "dailyCounts": {"2026-09-09": 3}}"""
        assertEquals(BackupData(mapOf("2026-09-09" to 3), emptyList()), BackupManager.parseExport(json))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse rejects files that are not an export`() {
        BackupManager.parseExport("""{"dailyCounts": {"2026-09-09": 3}}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse rejects non-JSON text`() {
        BackupManager.parseExport("hello")
    }

    @Test
    fun `parse skips bad dates and non-positive counts`() {
        val json = """{"format": "shafeezekr-stats", "version": 2,
            "dailyCounts": {"2026-09-09": 3, "not-a-date": 5, "2026-09-08": 0}}"""
        assertEquals(mapOf("2026-09-09" to 3), BackupManager.parseExport(json).dailyCounts)
    }

    @Test
    fun `merge counts keeps the higher count per day`() {
        val existing = mapOf("2026-09-08" to 10, "2026-09-09" to 2)
        val imported = mapOf("2026-09-09" to 7, "2026-09-07" to 1)
        assertEquals(
            mapOf("2026-09-07" to 1, "2026-09-08" to 10, "2026-09-09" to 7),
            BackupManager.mergeCounts(existing, imported)
        )
    }

    @Test
    fun `merge rules skips rules that already exist`() {
        val other = rule.copy(id = "rule-2", startHour = 13)
        assertEquals(listOf(rule, other), BackupManager.mergeRules(listOf(rule), listOf(rule, other)))
    }
}
