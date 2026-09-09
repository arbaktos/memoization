package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class WordPairTest {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    private val now = 1_800_000_000_000L

    private fun side(shown: Shown, stability: Double?, dueInDays: Long?) = Side(
        sideId = if (shown == Shown.WORD) 1 else 2,
        wordPairId = 1,
        shown = shown,
        state = if (stability == null) SideState.New else SideState.Review,
        stability = stability,
        due = dueInDays?.let { now + it * DAY_MILLIS },
    )

    private fun pair(vararg sides: Side) = WordPair(
        parentStackId = 1, word1 = "Haus", word2 = "house", wordPairId = 1, sides = sides.toList()
    )

    @Test
    fun `only the practised sides count`() {
        val pair = pair(side(Shown.WORD, 5.0, 3), side(Shown.MEANING, null, null))

        assertEquals(listOf(Shown.WORD), pair.activeSides(PracticeSides.WORD_TO_MEANING).map { it.shown })
        assertEquals(listOf(Shown.MEANING), pair.activeSides(PracticeSides.MEANING_TO_WORD).map { it.shown })
        assertEquals(2, pair.activeSides(PracticeSides.BOTH).size)
    }

    @Test
    fun `smart switch hands the pair over to its meaning side once the word side is known`() {
        val fresh = pair(side(Shown.WORD, 5.0, 3), side(Shown.MEANING, null, null))
        val known = pair(side(Shown.WORD, 8.0, 8), side(Shown.MEANING, null, null))

        assertEquals(listOf(Shown.WORD), fresh.activeSides(PracticeSides.SMART_SWITCH).map { it.shown })
        assertFalse(fresh.isDue(PracticeSides.SMART_SWITCH, now))
        // The word side is no longer asked for; only the meaning side is left, and being New
        // it is due at once and the pair reads at its level.
        assertEquals(listOf(Shown.MEANING), known.activeSides(PracticeSides.SMART_SWITCH).map { it.shown })
        assertTrue(known.isDue(PracticeSides.SMART_SWITCH, now))
        assertEquals(WordStatus.Level1, known.level(PracticeSides.SMART_SWITCH))
    }

    @Test
    fun `on the day the word side settles the pair is done, and due again tomorrow from the meaning side`() {
        val settledToday = pair(
            side(Shown.WORD, 8.0, 8).copy(lastReview = now),
            side(Shown.MEANING, null, null),
        )

        assertFalse(settledToday.isDue(PracticeSides.SMART_SWITCH, now, utc))
        assertEquals(WordStatus.Level3, settledToday.level(PracticeSides.SMART_SWITCH, now, utc))
        val tomorrow = now + DAY_MILLIS
        assertTrue(settledToday.isDue(PracticeSides.SMART_SWITCH, tomorrow, utc))
        assertEquals(
            listOf(Shown.MEANING),
            settledToday.activeSides(PracticeSides.SMART_SWITCH, tomorrow, utc).map { it.shown }
        )
        assertEquals(WordStatus.Level1, settledToday.level(PracticeSides.SMART_SWITCH, tomorrow, utc))
    }

    @Test
    fun `a pair is due when any practised side is due`() {
        val pair = pair(side(Shown.WORD, 5.0, 3), side(Shown.MEANING, null, null))

        // The word side is not due for three days; the meaning side has never been practised.
        assertFalse(pair.isDue(PracticeSides.WORD_TO_MEANING, now))
        assertTrue(pair.isDue(PracticeSides.MEANING_TO_WORD, now))
        assertTrue(pair.isDue(PracticeSides.BOTH, now))
    }

    @Test
    fun `the level is that of the weakest practised side`() {
        val pair = pair(side(Shown.WORD, 90.0, 30), side(Shown.MEANING, null, null))

        assertEquals(WordStatus.Learned, pair.level(PracticeSides.WORD_TO_MEANING))
        assertEquals(WordStatus.Level1, pair.level(PracticeSides.MEANING_TO_WORD))
        assertEquals(WordStatus.Level1, pair.level(PracticeSides.BOTH))
    }

    @Test
    fun `a pair without sides reads as level one`() {
        assertEquals(WordStatus.Level1, pair().level(PracticeSides.BOTH))
        assertFalse(pair().isDue(PracticeSides.BOTH, now))
    }

    @Test
    fun `each side shows one half and asks for the other`() {
        val pair = pair()

        assertEquals("Haus", pair.front(Shown.WORD))
        assertEquals("house", pair.back(Shown.WORD))
        assertEquals("house", pair.front(Shown.MEANING))
        assertEquals("Haus", pair.back(Shown.MEANING))
    }

    @Test
    fun `a missing meaning shows as empty rather than crashing`() {
        val pair = WordPair(parentStackId = 1, word1 = "Haus", word2 = null)

        assertEquals("", pair.front(Shown.MEANING))
        assertEquals("", pair.back(Shown.WORD))
    }
}
