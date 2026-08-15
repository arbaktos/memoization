package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SideTest {

    private val belgrade: TimeZone = TimeZone.getTimeZone("Europe/Belgrade")

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(belgrade).apply {
            clear()
            timeZone = belgrade
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun side(due: Long?, stability: Double? = 5.0) = Side(
        sideId = 1,
        wordPairId = 1,
        shown = Shown.WORD,
        state = if (due == null) SideState.New else SideState.Review,
        stability = stability,
        due = due,
    )

    @Test
    fun `a side that has never been practised is due at once`() {
        val newSide = Side(wordPairId = 1, shown = Shown.WORD)

        assertTrue(newSide.isNew)
        assertTrue(newSide.isDue(at(2026, Calendar.AUGUST, 15, 10, 0), belgrade))
    }

    @Test
    fun `due later today counts as due`() {
        val due = at(2026, Calendar.AUGUST, 15, 23, 0)
        val now = at(2026, Calendar.AUGUST, 15, 8, 0)

        assertTrue(side(due).isDue(now, belgrade))
    }

    @Test
    fun `due tomorrow is not due tonight`() {
        val due = at(2026, Calendar.AUGUST, 16, 0, 30)
        val now = at(2026, Calendar.AUGUST, 15, 23, 59)

        assertFalse(side(due).isDue(now, belgrade))
    }

    @Test
    fun `an overdue side stays due`() {
        val due = at(2026, Calendar.AUGUST, 1, 12, 0)
        val now = due + 40 * DAY_MILLIS

        assertTrue(side(due).isDue(now, belgrade))
    }

    @Test
    fun `the level reads the stability`() {
        assertEquals(WordStatus.Level1, side(null, stability = null).level)
        assertEquals(WordStatus.Level2, side(0, stability = 5.0).level)
        assertEquals(WordStatus.Learned, side(0, stability = 90.0).level)
    }
}
