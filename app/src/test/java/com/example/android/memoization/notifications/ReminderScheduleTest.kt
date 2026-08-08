package com.example.android.memoization.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import java.util.Calendar
import java.util.TimeZone
import org.junit.Test

class ReminderScheduleTest {

    private val everyDay = (Calendar.SUNDAY..Calendar.SATURDAY).toSet()
    private val noon = listOf(12 to 0)

    private fun at(
        zone: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Calendar = Calendar.getInstance(TimeZone.getTimeZone(zone)).apply {
        clear()
        timeZone = TimeZone.getTimeZone(zone)
        set(year, month, day, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }

    @Test
    fun `picks todays time when it is still ahead`() {
        val from = at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 9, 30)

        val next = ReminderSchedule.nextTriggerAt(noon, everyDay, from)

        assertEquals(at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 12, 0).timeInMillis, next)
    }

    @Test
    fun `rolls to tomorrow once todays time has passed`() {
        val from = at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 14, 11)

        val next = ReminderSchedule.nextTriggerAt(noon, everyDay, from)

        assertEquals(at("Europe/Belgrade", 2026, Calendar.AUGUST, 9, 12, 0).timeInMillis, next)
    }

    @Test
    fun `noon means local noon, so the same wall clock is a different instant per zone`() {
        val belgrade = ReminderSchedule.nextTriggerAt(
            noon, everyDay, at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 9, 0)
        )!!
        val london = ReminderSchedule.nextTriggerAt(
            noon, everyDay, at("Europe/London", 2026, Calendar.AUGUST, 8, 9, 0)
        )!!

        // Belgrade is an hour ahead of London, so its noon happens an hour earlier.
        assertEquals(ONE_HOUR, london - belgrade)
    }

    @Test
    fun `stays at noon across the end of daylight saving`() {
        // Belgrade puts the clocks back on Sunday 25 October 2026.
        val from = at("Europe/Belgrade", 2026, Calendar.OCTOBER, 24, 14, 0)

        val next = ReminderSchedule.nextTriggerAt(noon, everyDay, from)!!

        // 22 hours of wall clock, but the day is 25 hours long, so 23 hours elapse.
        assertEquals(23 * ONE_HOUR, next - from.timeInMillis)
        val fired = Calendar.getInstance(TimeZone.getTimeZone("Europe/Belgrade"))
            .apply { timeInMillis = next }
        assertEquals(12, fired.get(Calendar.HOUR_OF_DAY))
        assertEquals(Calendar.SUNDAY, fired.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `skips days the user turned off`() {
        val from = at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 14, 11) // a Saturday
        val withoutSunday = everyDay - Calendar.SUNDAY

        val next = ReminderSchedule.nextTriggerAt(noon, withoutSunday, from)

        assertEquals(at("Europe/Belgrade", 2026, Calendar.AUGUST, 10, 12, 0).timeInMillis, next)
    }

    @Test
    fun `takes the earliest of several times a day`() {
        val from = at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 13, 0)

        val next = ReminderSchedule.nextTriggerAt(listOf(20 to 30, 9 to 0, 18 to 0), everyDay, from)

        assertEquals(at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 18, 0).timeInMillis, next)
    }

    @Test
    fun `no enabled day means no reminder`() {
        val from = at("Europe/Belgrade", 2026, Calendar.AUGUST, 8, 9, 0)

        assertNull(ReminderSchedule.nextTriggerAt(noon, emptySet(), from))
        assertNull(ReminderSchedule.nextTriggerAt(emptyList(), everyDay, from))
    }

    private companion object {
        const val ONE_HOUR = 60 * 60 * 1000L
    }
}
