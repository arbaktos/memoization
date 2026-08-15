package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import com.example.android.memoization.domain.session.dueSessionSides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoStackTest {

    private val now = 1_800_000_000_000L

    private fun pair(id: Long, wordDueInDays: Long?, meaningDueInDays: Long?) = WordPair(
        parentStackId = 1,
        word1 = "word$id",
        word2 = "meaning$id",
        wordPairId = id,
        sides = listOf(
            Side(
                sideId = id * 10, wordPairId = id, shown = Shown.WORD,
                state = SideState.Review, stability = 5.0,
                due = now + (wordDueInDays ?: 0) * DAY_MILLIS,
            ),
            Side(
                sideId = id * 10 + 1, wordPairId = id, shown = Shown.MEANING,
                state = SideState.Review, stability = 5.0,
                due = now + (meaningDueInDays ?: 0) * DAY_MILLIS,
            ),
        )
    )

    private fun stack(vararg pairs: WordPair) = MemoStack(name = "Serbian").apply {
        words = pairs.toMutableList()
    }

    @Test
    fun `due pairs are counted as pairs, not as sides`() {
        // Both sides of the first pair are due; that is still one pair to work through.
        val stack = stack(
            pair(1, wordDueInDays = 0, meaningDueInDays = 0),
            pair(2, wordDueInDays = 5, meaningDueInDays = 5),
        )

        assertEquals(listOf(1L), stack.duePairs(PracticeSides.BOTH, now).map { it.wordPairId })
        assertTrue(stack.hasDue(PracticeSides.BOTH, now))
    }

    @Test
    fun `nothing is due when every practised side is scheduled ahead`() {
        val stack = stack(pair(1, wordDueInDays = 3, meaningDueInDays = 0))

        assertFalse(stack.hasDue(PracticeSides.WORD_TO_MEANING, now))
        assertTrue(stack.hasDue(PracticeSides.MEANING_TO_WORD, now))
    }

    @Test
    fun `the session gets one item per due practised side, with its own texts`() {
        val stack = stack(
            pair(1, wordDueInDays = 0, meaningDueInDays = 0),
            pair(2, wordDueInDays = 5, meaningDueInDays = 0),
        )

        val oneWay = stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now)
        assertEquals(1, oneWay.size)
        assertEquals("word1", oneWay.single().front)
        assertEquals("meaning1", oneWay.single().back)

        val bothWays = stack.dueSessionSides(PracticeSides.BOTH, now)
        assertEquals(3, bothWays.size)
        assertEquals(
            listOf("word1", "meaning1", "meaning2"),
            bothWays.map { it.front }
        )
    }
}
