package islamalorabi.shafeezekr.pbuh.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DhikrStatsTest {

    private val today: LocalDate = LocalDate.of(2026, 9, 9)

    // --- computeStreak ---

    @Test
    fun `empty counts give zero streak`() {
        assertEquals(0, DhikrStatsManager.computeStreak(emptyMap(), today))
    }

    @Test
    fun `count only today gives streak of one`() {
        assertEquals(1, DhikrStatsManager.computeStreak(mapOf("2026-09-09" to 5), today))
    }

    @Test
    fun `consecutive days count fully`() {
        val counts = mapOf(
            "2026-09-09" to 3,
            "2026-09-08" to 1,
            "2026-09-07" to 10
        )
        assertEquals(3, DhikrStatsManager.computeStreak(counts, today))
    }

    @Test
    fun `gap breaks the streak`() {
        val counts = mapOf(
            "2026-09-09" to 3,
            "2026-09-08" to 0,
            "2026-09-07" to 10
        )
        assertEquals(1, DhikrStatsManager.computeStreak(counts, today))
    }

    @Test
    fun `missing today gives zero even with yesterday chain`() {
        val counts = mapOf(
            "2026-09-08" to 4,
            "2026-09-07" to 2
        )
        assertEquals(0, DhikrStatsManager.computeStreak(counts, today))
    }

    // --- computeWeeklyData ---

    @Test
    fun `weekly data covers seven days ending today, oldest first`() {
        val data = DhikrStatsManager.computeWeeklyData(emptyMap(), today)
        assertEquals(7, data.size)
        assertEquals(LocalDate.of(2026, 9, 3), data.first().first)
        assertEquals(today, data.last().first)
    }

    @Test
    fun `weekly data maps dates to their counts`() {
        val counts = mapOf(
            "2026-09-09" to 12,
            "2026-09-07" to 4,
            "2026-08-30" to 99 // outside the window
        )
        val data = DhikrStatsManager.computeWeeklyData(counts, today)
        assertEquals(12, data.last { it.first == LocalDate.of(2026, 9, 9) }.second)
        assertEquals(4, data.last { it.first == LocalDate.of(2026, 9, 7) }.second)
        assertEquals(0, data.last { it.first == LocalDate.of(2026, 9, 4) }.second)
    }

    // --- computeMonthlyTotal ---

    @Test
    fun `monthly total sums only current month`() {
        val counts = mapOf(
            "2026-09-01" to 5,
            "2026-09-09" to 7,
            "2026-08-31" to 100,
            "2025-09-09" to 50
        )
        assertEquals(12, DhikrStatsManager.computeMonthlyTotal(counts, today))
    }

    @Test
    fun `monthly total is zero for empty counts`() {
        assertEquals(0, DhikrStatsManager.computeMonthlyTotal(emptyMap(), today))
    }

    // --- computeAllTimeTotal ---

    @Test
    fun `all-time total sums every day`() {
        val counts = mapOf(
            "2026-09-09" to 5,
            "2026-08-31" to 100,
            "2025-01-01" to 50
        )
        assertEquals(155, DhikrStatsManager.computeAllTimeTotal(counts))
    }
}
