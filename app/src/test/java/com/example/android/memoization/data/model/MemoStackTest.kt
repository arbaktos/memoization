package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import com.example.android.memoization.domain.session.dueSessionSides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone
import kotlin.random.Random

class MemoStackTest {

    private val now = 1_800_000_000_000L
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    /** A word side rated [intervalDays] before its due date, which was [overdueDays] ago. */
    private fun scheduledPair(id: Long, stability: Double, intervalDays: Long, overdueDays: Long) = WordPair(
        parentStackId = 1,
        word1 = "word$id",
        word2 = "meaning$id",
        wordPairId = id,
        sides = listOf(
            Side(
                sideId = id, wordPairId = id, shown = Shown.WORD,
                state = SideState.Review, stability = stability,
                due = now - overdueDays * DAY_MILLIS,
                lastReview = now - (overdueDays + intervalDays) * DAY_MILLIS,
            )
        )
    )

    private fun overduePair(id: Long, overdueDays: Long) = WordPair(
        parentStackId = 1,
        word1 = "word$id",
        word2 = "meaning$id",
        wordPairId = id,
        sides = listOf(
            Side(
                sideId = id, wordPairId = id, shown = Shown.WORD,
                state = SideState.Review, stability = 5.0,
                due = now - overdueDays * DAY_MILLIS,
                lastReview = now - (overdueDays + 5) * DAY_MILLIS,
            )
        )
    )

    /** Answered earlier today, so already scheduled forward and no longer due. */
    private fun answeredTodayPair(id: Long) = WordPair(
        parentStackId = 1,
        word1 = "word$id",
        word2 = "meaning$id",
        wordPairId = id,
        sides = listOf(
            Side(
                sideId = id, wordPairId = id, shown = Shown.WORD,
                state = SideState.Review, stability = 5.0,
                due = now + 5 * DAY_MILLIS,
                lastReview = now,
            )
        )
    )

    private fun newPair(id: Long) = WordPair(
        parentStackId = 1,
        word1 = "word$id",
        word2 = "meaning$id",
        wordPairId = id,
        sides = listOf(Side(sideId = id, wordPairId = id, shown = Shown.WORD))
    )

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
        // All three are equally due, so their order among themselves is random.
        assertEquals(
            setOf("word1", "meaning1", "meaning2"),
            bothWays.map { it.front }.toSet()
        )
    }

    // --- what a session takes, and in what order -----------------------------------------

    @Test
    fun `a session takes every due side, the most overdue first`() {
        val stack = stack(*(1L..50L).map { overduePair(it, overdueDays = it) }.toTypedArray())

        val session = stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now)

        assertEquals((50L downTo 1L).toList(), session.map { it.side.sideId })
    }

    @Test
    fun `how late a side is counts against its interval, not in days`() {
        val stack = stack(
            // Three days late on a month: still about nine in ten recalled.
            scheduledPair(1, stability = 30.0, intervalDays = 30, overdueDays = 3),
            // One day late on a one-day interval: the interval doubled, half of it forgotten.
            scheduledPair(2, stability = 1.0, intervalDays = 1, overdueDays = 1),
        )

        val session = stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now, utc)

        assertEquals(listOf(2L, 1L), session.map { it.side.sideId })
    }

    @Test
    fun `among sides due on the day asked for, the weaker memory goes first`() {
        val stack = stack(
            scheduledPair(1, stability = 20.0, intervalDays = 20, overdueDays = 0),
            scheduledPair(2, stability = 2.0, intervalDays = 2, overdueDays = 0),
        )

        val session = stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now, utc)

        assertEquals(listOf(2L, 1L), session.map { it.side.sideId })
    }

    @Test
    fun `sides never practised come after every overdue one, in random order`() {
        val stack = stack(
            *(1L..20L).map { newPair(it) }.toTypedArray(),
            overduePair(21, overdueDays = 3),
            overduePair(22, overdueDays = 5),
        )

        val session = stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now, random = Random(7))
        val ids = session.map { it.side.sideId }

        assertEquals(listOf(22L, 21L), ids.take(2))
        assertEquals((1L..20L).toSet(), ids.drop(2).toSet())
        assertNotEquals("20 new sides should not come out in insertion order", (1L..20L).toList(), ids.drop(2))
    }

    @Test
    fun `a second sitting on the same day gets what the first left, not what it answered`() {
        val stack = stack(
            answeredTodayPair(1),
            answeredTodayPair(2),
            overduePair(3, overdueDays = 3),
            overduePair(4, overdueDays = 2),
            overduePair(5, overdueDays = 1),
        )

        val session = stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now)

        // The two answered earlier are scheduled forward; everything else is offered.
        assertEquals(listOf(3L, 4L, 5L), session.map { it.side.sideId })
    }

    @Test
    fun `a sitting with nothing due is empty`() {
        val stack = stack(answeredTodayPair(1), answeredTodayPair(2))

        assertTrue(stack.dueSessionSides(PracticeSides.WORD_TO_MEANING, now).isEmpty())
    }
}
