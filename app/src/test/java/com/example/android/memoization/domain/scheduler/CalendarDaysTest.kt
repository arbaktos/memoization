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
    fun `elapsed days floors and never goes negative`() {
        val now = 1_800_000_000_000L

        assertEquals(0, elapsedDays(now, now))
        assertEquals(0, elapsedDays(now, now + DAY_MILLIS - 1))
        assertEquals(1, elapsedDays(now, now + DAY_MILLIS))
        assertEquals(3, elapsedDays(now, now + 3 * DAY_MILLIS + 5_000))
        assertEquals(0, elapsedDays(now, now - 10 * DAY_MILLIS))
    }
}
