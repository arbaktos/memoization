package com.example.android.memoization.domain.scheduler

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class CalendarDaysTest {

    private val belgrade: TimeZone = TimeZone.getTimeZone("Europe/Belgrade")
    private val london: TimeZone = TimeZone.getTimeZone("Europe/London")

    private fun at(
        zone: TimeZone,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long = Calendar.getInstance(zone).apply {
        clear()
        timeZone = zone
        set(year, month, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun `a minute past midnight is already the next day`() {
        val before = at(belgrade, 2026, Calendar.AUGUST, 15, 23, 59)
        val after = at(belgrade, 2026, Calendar.AUGUST, 16, 0, 1)

        assertEquals(1, localDay(after, belgrade) - localDay(before, belgrade))
    }

    @Test
    fun `the same instant can fall on different days in different zones`() {
        val instant = at(belgrade, 2026, Calendar.AUGUST, 16, 0, 30)

        assertEquals(
            localDay(at(belgrade, 2026, Calendar.AUGUST, 16, 12, 0), belgrade),
            localDay(instant, belgrade)
        )
        assertEquals(
            localDay(at(london, 2026, Calendar.AUGUST, 15, 12, 0), london),
            localDay(instant, london)
        )
    }

    @Test
    fun `the night the clocks go back is still one day`() {
        // Europe/Belgrade leaves summer time on 25 October 2026 - that day has 25 hours.
        val before = at(belgrade, 2026, Calendar.OCTOBER, 24, 12, 0)
        val after = at(belgrade, 2026, Calendar.OCTOBER, 25, 12, 0)

        assertEquals(1, localDay(after, belgrade) - localDay(before, belgrade))
    }

    @Test
    fun `elapsed days are calendar days, so an earlier sitting the next day still counts as one`() {
        // Yesterday's session at 16:08, today's at 13:15: under 24 hours, but a day apart.
        val yesterday = at(belgrade, 2026, Calendar.AUGUST, 26, 16, 8)
        val today = at(belgrade, 2026, Calendar.AUGUST, 27, 13, 15)

        assertEquals(1, elapsedDays(yesterday, today, belgrade))
        assertEquals(
            1,
            elapsedDays(
                at(belgrade, 2026, Calendar.AUGUST, 26, 23, 59),
                at(belgrade, 2026, Calendar.AUGUST, 27, 0, 1),
                belgrade
            )
        )
    }

    @Test
    fun `elapsed days are zero within a day and never negative`() {
        val morning = at(belgrade, 2026, Calendar.AUGUST, 26, 9, 0)
        val evening = at(belgrade, 2026, Calendar.AUGUST, 26, 23, 30)

        assertEquals(0, elapsedDays(morning, morning, belgrade))
        assertEquals(0, elapsedDays(morning, evening, belgrade))
        assertEquals(3, elapsedDays(morning, at(belgrade, 2026, Calendar.AUGUST, 29, 8, 0), belgrade))
        assertEquals(0, elapsedDays(evening, morning, belgrade))
    }

    @Test
    fun `elapsed days follow the zone the learner is in`() {
        // 00:30 in Belgrade on the 16th is still 23:30 on the 15th in London.
        val before = at(belgrade, 2026, Calendar.AUGUST, 15, 12, 0)
        val after = at(belgrade, 2026, Calendar.AUGUST, 16, 0, 30)

        assertEquals(1, elapsedDays(before, after, belgrade))
        assertEquals(0, elapsedDays(before, after, london))
    }
}
