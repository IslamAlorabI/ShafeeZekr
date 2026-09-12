package islamalorabi.shafeezekr.pbuh.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PeriodRuleTest {

    @Before
    fun setUp() {
        // getDisplayText() formats with the default locale; pin it for stable assertions
        Locale.setDefault(Locale.US)
    }

    private fun utcNow(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }

    // 2026-09-09 is a Wednesday
    private fun wednesday(hour: Int, minute: Int) = utcNow(2026, Calendar.SEPTEMBER, 9, hour, minute)
    private val wednesdayIndex get() = wednesday(12, 0).get(Calendar.DAY_OF_WEEK) - 1
    private val thursdayIndex get() = (wednesdayIndex + 1) % 7

    private fun weeklyRule(
        days: Set<Int>,
        startHour: Int = 0, startMinute: Int = 0,
        endHour: Int = 23, endMinute: Int = 59,
        isAllDay: Boolean = false,
        isEnabled: Boolean = true
    ) = PeriodRule(
        scheduleType = RuleScheduleType.WEEKLY_DAYS,
        isEnabled = isEnabled,
        startHour = startHour, startMinute = startMinute,
        endHour = endHour, endMinute = endMinute,
        daysOfWeek = days,
        isAllDay = isAllDay
    )

    private fun dateRule(
        year: Int = 2026, month: Int = Calendar.SEPTEMBER, day: Int = 9,
        startHour: Int = 9, endHour: Int = 17,
        isAllDay: Boolean = false
    ) = PeriodRule(
        scheduleType = RuleScheduleType.SPECIFIC_DATE,
        startHour = startHour, startMinute = 0,
        endHour = endHour, endMinute = 0,
        year = year, month = month, dayOfMonth = day,
        isAllDay = isAllDay
    )

    // --- isCurrentTimeInRange: weekly, same-day range ---

    @Test
    fun `weekly same-day range includes start boundary`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 17)
        assertTrue(rule.isCurrentTimeInRange(wednesday(9, 0)))
    }

    @Test
    fun `weekly same-day range includes end boundary`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 17)
        assertTrue(rule.isCurrentTimeInRange(wednesday(17, 0)))
    }

    @Test
    fun `weekly same-day range includes middle`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 17)
        assertTrue(rule.isCurrentTimeInRange(wednesday(12, 30)))
    }

    @Test
    fun `weekly same-day range excludes before start`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 17)
        assertFalse(rule.isCurrentTimeInRange(wednesday(8, 59)))
    }

    @Test
    fun `weekly same-day range excludes after end`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 17)
        assertFalse(rule.isCurrentTimeInRange(wednesday(17, 1)))
    }

    @Test
    fun `weekly rule inactive on unselected day`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 17)
        val thursday = (wednesday(12, 0).clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        assertEquals(thursdayIndex, thursday.get(Calendar.DAY_OF_WEEK) - 1)
        assertFalse(rule.isCurrentTimeInRange(thursday))
    }

    // --- isCurrentTimeInRange: weekly, overnight (wrap-midnight) range ---

    @Test
    fun `overnight range active late evening`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 22, endHour = 6)
        assertTrue(rule.isCurrentTimeInRange(wednesday(23, 0)))
    }

    @Test
    fun `overnight range active at start boundary`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 22, endHour = 6)
        assertTrue(rule.isCurrentTimeInRange(wednesday(22, 0)))
    }

    @Test
    fun `overnight range active in early morning`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 22, endHour = 6)
        assertTrue(rule.isCurrentTimeInRange(wednesday(5, 59)))
    }

    @Test
    fun `overnight range inactive just after end`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 22, endHour = 6)
        assertFalse(rule.isCurrentTimeInRange(wednesday(6, 1)))
    }

    @Test
    fun `overnight range inactive just before start`() {
        val rule = weeklyRule(setOf(wednesdayIndex), startHour = 22, endHour = 6)
        assertFalse(rule.isCurrentTimeInRange(wednesday(21, 59)))
    }

    // --- isCurrentTimeInRange: all-day ---

    @Test
    fun `all-day rule active all day on selected day`() {
        val rule = weeklyRule(setOf(wednesdayIndex), isAllDay = true)
        assertTrue(rule.isCurrentTimeInRange(wednesday(0, 0)))
        assertTrue(rule.isCurrentTimeInRange(wednesday(23, 59)))
    }

    @Test
    fun `all-day rule inactive on other day`() {
        val rule = weeklyRule(setOf(wednesdayIndex), isAllDay = true)
        val thursday = (wednesday(12, 0).clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        assertFalse(rule.isCurrentTimeInRange(thursday))
    }

    // --- isCurrentTimeInRange: specific date ---

    @Test
    fun `date rule active on its date within range`() {
        val rule = dateRule(startHour = 9, endHour = 17)
        assertTrue(rule.isCurrentTimeInRange(wednesday(12, 0)))
    }

    @Test
    fun `date rule inactive on wrong date`() {
        val rule = dateRule(day = 10)
        assertFalse(rule.isCurrentTimeInRange(wednesday(12, 0)))
    }

    @Test
    fun `date rule inactive on wrong month`() {
        val rule = dateRule(month = Calendar.OCTOBER)
        assertFalse(rule.isCurrentTimeInRange(wednesday(12, 0)))
    }

    @Test
    fun `date rule inactive outside hours`() {
        val rule = dateRule(startHour = 9, endHour = 17)
        assertFalse(rule.isCurrentTimeInRange(wednesday(20, 0)))
    }

    // --- conflictsWith ---

    @Test
    fun `overlapping ranges on common day conflict`() {
        val a = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 12)
        val b = weeklyRule(setOf(wednesdayIndex), startHour = 11, endHour = 14)
        assertTrue(a.conflictsWith(b))
    }

    @Test
    fun `ranges touching at a boundary conflict because ends are inclusive`() {
        val a = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 12)
        val b = weeklyRule(setOf(wednesdayIndex), startHour = 12, endHour = 14)
        assertTrue(a.conflictsWith(b))
    }

    @Test
    fun `disjoint ranges do not conflict`() {
        val a = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 12)
        val b = weeklyRule(setOf(wednesdayIndex), startHour = 13, endHour = 14)
        assertFalse(a.conflictsWith(b))
    }

    @Test
    fun `rules on different days never conflict`() {
        val a = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 12)
        val b = weeklyRule(setOf(thursdayIndex), startHour = 9, endHour = 12)
        assertFalse(a.conflictsWith(b))
    }

    @Test
    fun `all-day rule conflicts with any range on common day`() {
        val a = weeklyRule(setOf(wednesdayIndex), isAllDay = true)
        val b = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 10)
        assertTrue(a.conflictsWith(b))
    }

    @Test
    fun `overnight range conflicts with any range on common day`() {
        val a = weeklyRule(setOf(wednesdayIndex), startHour = 22, endHour = 6)
        val b = weeklyRule(setOf(wednesdayIndex), startHour = 9, endHour = 10)
        assertTrue(a.conflictsWith(b))
    }

    @Test
    fun `different schedule types never conflict`() {
        val a = weeklyRule(setOf(wednesdayIndex), isAllDay = true)
        val b = dateRule()
        assertFalse(a.conflictsWith(b))
    }

    // --- JSON round-trip ---

    @Test
    fun `weekly rule survives json round-trip`() {
        val rule = weeklyRule(
            days = setOf(0, 2, 4),
            startHour = 22, startMinute = 30,
            endHour = 6, endMinute = 15,
            isEnabled = false
        )
        assertEquals(rule, PeriodRule.fromJson(rule.toJson()))
    }

    @Test
    fun `date rule survives json round-trip`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.SPECIFIC_DATE,
            startHour = 1, startMinute = 2,
            endHour = 3, endMinute = 4,
            year = 2027, month = Calendar.DECEMBER, dayOfMonth = 31,
            isAllDay = true
        )
        assertEquals(rule, PeriodRule.fromJson(rule.toJson()))
    }

    // --- getDisplayText ---

    @Test
    fun `all-day rule displays full-day range`() {
        val rule = weeklyRule(setOf(wednesdayIndex), isAllDay = true)
        assertTrue(rule.getDisplayText().endsWith("00:00 - 23:59"))
    }

    @Test
    fun `every-day rule displays all week`() {
        val rule = weeklyRule((0..6).toSet(), startHour = 9, endHour = 10)
        assertTrue(rule.getDisplayText().startsWith("All week"))
    }

    @Test
    fun `date rule displays iso date`() {
        val rule = dateRule(startHour = 9, endHour = 10)
        assertTrue(rule.getDisplayText().startsWith("2026-09-09"))
    }
}
