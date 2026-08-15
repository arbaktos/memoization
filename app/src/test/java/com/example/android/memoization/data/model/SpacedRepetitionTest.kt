package com.example.android.memoization.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class SpacedRepetitionTest {

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

    private val allLevels = listOf(
        WordStatus.Level1, WordStatus.Level2, WordStatus.Level3, WordStatus.Level4, WordStatus.Learned
    )

    // --- isDue -----------------------------------------------------------------------------

    @Test
    fun `a pair that was never repeated is due at once`() {
        val now = at(belgrade, 2026, Calendar.AUGUST, 15, 10, 0)

        for (level in allLevels) {
            assertTrue("$level", SpacedRepetition.isDue(level, lastRep = null, now = now, zone = belgrade))
        }
    }

    @Test
    fun `level one repeated a minute before midnight is due a minute after`() {
        val lastRep = at(belgrade, 2026, Calendar.AUGUST, 15, 23, 59)
        val now = at(belgrade, 2026, Calendar.AUGUST, 16, 0, 1)

        assertTrue(SpacedRepetition.isDue(WordStatus.Level1, lastRep, now, belgrade))
    }

    @Test
    fun `level one repeated in the morning is not due that evening`() {
        val lastRep = at(belgrade, 2026, Calendar.AUGUST, 15, 0, 1)
        val now = at(belgrade, 2026, Calendar.AUGUST, 15, 23, 59)

        assertFalse(SpacedRepetition.isDue(WordStatus.Level1, lastRep, now, belgrade))
    }

    @Test
    fun `each level is due on its frequency-th day and not the day before`() {
        val lastRep = at(belgrade, 2026, Calendar.AUGUST, 1, 12, 0)

        for (level in allLevels) {
            val dayBefore = at(belgrade, 2026, Calendar.AUGUST, 1 + level.frequency - 1, 23, 59)
            val onTheDay = at(belgrade, 2026, Calendar.AUGUST, 1 + level.frequency, 0, 0)

            assertFalse("$level, day before", SpacedRepetition.isDue(level, lastRep, dayBefore, belgrade))
            assertTrue("$level, on the day", SpacedRepetition.isDue(level, lastRep, onTheDay, belgrade))
        }
    }

    @Test
    fun `calendar days are counted in the given zone`() {
        // 23:30 in Belgrade is 22:30 in London: still the same day there.
        val lastRep = at(belgrade, 2026, Calendar.AUGUST, 15, 12, 0)
        val now = at(belgrade, 2026, Calendar.AUGUST, 16, 0, 30)

        assertTrue(SpacedRepetition.isDue(WordStatus.Level1, lastRep, now, belgrade))
        assertFalse(SpacedRepetition.isDue(WordStatus.Level1, lastRep, now, london))
    }

    @Test
    fun `the night the clocks go back still counts as one day`() {
        // Europe/Belgrade leaves DST on 25 October 2026 - that day has 25 hours.
        val lastRep = at(belgrade, 2026, Calendar.OCTOBER, 24, 12, 0)
        val now = at(belgrade, 2026, Calendar.OCTOBER, 25, 12, 0)

        assertTrue(SpacedRepetition.isDue(WordStatus.Level1, lastRep, now, belgrade))
        assertFalse(SpacedRepetition.isDue(WordStatus.Level2, lastRep, now, belgrade))
    }

    // --- rate ------------------------------------------------------------------------------

    @Test
    fun `again resets any level to level one`() {
        for (level in allLevels) {
            assertEquals(WordStatus.Level1, SpacedRepetition.rate(level, Rating.Again))
        }
    }

    @Test
    fun `hard keeps the level`() {
        for (level in allLevels) {
            assertEquals(level, SpacedRepetition.rate(level, Rating.Hard))
        }
    }

    @Test
    fun `good moves one step up and learned stays learned`() {
        assertEquals(WordStatus.Level2, SpacedRepetition.rate(WordStatus.Level1, Rating.Good))
        assertEquals(WordStatus.Level3, SpacedRepetition.rate(WordStatus.Level2, Rating.Good))
        assertEquals(WordStatus.Level4, SpacedRepetition.rate(WordStatus.Level3, Rating.Good))
        assertEquals(WordStatus.Learned, SpacedRepetition.rate(WordStatus.Level4, Rating.Good))
        assertEquals(WordStatus.Learned, SpacedRepetition.rate(WordStatus.Learned, Rating.Good))
    }

    // --- WordPair ---------------------------------------------------------------------------

    @Test
    fun `rated pair carries the new level and the rating time, nothing else changes`() {
        val now = at(belgrade, 2026, Calendar.AUGUST, 15, 10, 0)
        val pair = WordPair(
            parentStackId = 7, word1 = "Haus", word2 = "house",
            lastRep = null, wordPairId = 42, isVisible = true, level = WordStatus.Level2
        )

        val rated = pair.rated(Rating.Good, now)

        assertEquals(WordStatus.Level3, rated.level)
        assertEquals(Date(now), rated.lastRep)
        assertEquals(pair.copy(level = WordStatus.Level3, lastRep = Date(now)), rated)
        assertTrue(pair.isNew)
        assertFalse(rated.isNew)
    }
}
