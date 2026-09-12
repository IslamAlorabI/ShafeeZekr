package islamalorabi.shafeezekr.pbuh.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class QuietHoursTest {

    private fun utcNow(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }

    // 2026-09-09 is a Wednesday
    private fun wednesday(hour: Int, minute: Int) = utcNow(2026, Calendar.SEPTEMBER, 9, hour, minute)
    private val wednesdayIndex get() = wednesday(12, 0).get(Calendar.DAY_OF_WEEK) - 1

    /** Mirrors the app's contract: rule end fires at HH:MM:59.999, resume alarm 1s later. */
    private fun expectedEnd(now: Calendar, hour: Int, minute: Int, addDays: Int = 0): Long =
        (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
            if (addDays != 0) add(Calendar.DAY_OF_MONTH, addDays)
        }.timeInMillis + 1000L

    private fun settingsWith(vararg rules: PeriodRule) = AppSettings(periodRules = rules.toList())

    // --- isReminderAllowedByPeriodRules ---

    @Test
    fun `no rules means allowed`() {
        assertTrue(settingsWith().isReminderAllowedByPeriodRules(wednesday(12, 0)))
    }

    @Test
    fun `active matching rule blocks reminders`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 9, endHour = 17,
            daysOfWeek = setOf(wednesdayIndex)
        )
        assertFalse(settingsWith(rule).isReminderAllowedByPeriodRules(wednesday(12, 0)))
    }

    @Test
    fun `disabled matching rule does not block`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            isEnabled = false,
            startHour = 9, endHour = 17,
            daysOfWeek = setOf(wednesdayIndex)
        )
        assertTrue(settingsWith(rule).isReminderAllowedByPeriodRules(wednesday(12, 0)))
    }

    @Test
    fun `rule on another day does not block`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 9, endHour = 17,
            daysOfWeek = setOf((wednesdayIndex + 1) % 7)
        )
        assertTrue(settingsWith(rule).isReminderAllowedByPeriodRules(wednesday(12, 0)))
    }

    // --- getQuietHoursEndMillis ---

    @Test
    fun `same-day rule ends at rule end today`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 9, endHour = 17,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val now = wednesday(12, 0)
        assertEquals(expectedEnd(now, 17, 0), settingsWith(rule).getQuietHoursEndMillis(now))
    }

    @Test
    fun `overnight rule entered in evening ends tomorrow morning`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 22, endHour = 6,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val now = wednesday(23, 0)
        assertEquals(expectedEnd(now, 6, 0, addDays = 1), settingsWith(rule).getQuietHoursEndMillis(now))
    }

    @Test
    fun `overnight rule in early morning ends same morning`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 22, endHour = 6,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val now = wednesday(5, 0)
        assertEquals(expectedEnd(now, 6, 0), settingsWith(rule).getQuietHoursEndMillis(now))
    }

    @Test
    fun `all-day rule ends at midnight`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            isAllDay = true,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val now = wednesday(12, 0)
        assertEquals(expectedEnd(now, 23, 59), settingsWith(rule).getQuietHoursEndMillis(now))
    }

    @Test
    fun `earliest ending active rule wins`() {
        val later = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 8, endHour = 20,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val earlier = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            startHour = 10, endHour = 14,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val now = wednesday(12, 0)
        assertEquals(expectedEnd(now, 14, 0), settingsWith(later, earlier).getQuietHoursEndMillis(now))
    }

    @Test
    fun `no active rule falls back to one minute from now`() {
        val now = wednesday(12, 0)
        assertEquals(now.timeInMillis + 60_000L, settingsWith().getQuietHoursEndMillis(now))
    }

    @Test
    fun `inactive rules are ignored for end calculation`() {
        val rule = PeriodRule(
            scheduleType = RuleScheduleType.WEEKLY_DAYS,
            isEnabled = false,
            startHour = 9, endHour = 17,
            daysOfWeek = setOf(wednesdayIndex)
        )
        val now = wednesday(12, 0)
        assertEquals(now.timeInMillis + 60_000L, settingsWith(rule).getQuietHoursEndMillis(now))
    }
}
