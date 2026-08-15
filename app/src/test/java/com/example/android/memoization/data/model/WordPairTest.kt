package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordPairTest {

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
