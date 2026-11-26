package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import com.example.android.memoization.domain.scheduler.Fsrs
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class ReviewUrgencyTest {

    private val now = 1_800_000_000_000L
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val fsrs = Fsrs(zone = utc)

    /** A word side last rated [elapsedDays] ago, due [overdueDays] ago. */
    private fun pair(id: Long, stability: Double, elapsedDays: Long, overdueDays: Long = 0) = WordPair(
        parentStackId = 1,
        word1 = "word$id",
        word2 = "meaning$id",
        wordPairId = id,
        sides = listOf(
            Side(
                sideId = id, wordPairId = id, shown = Shown.WORD,
                state = SideState.Review, stability = stability,
                due = now - overdueDays * DAY_MILLIS,
                lastReview = now - elapsedDays * DAY_MILLIS,
            )
        )
    )

    private fun newPair(id: Long) = WordPair(
        parentStackId = 1, word1 = "word$id", word2 = "meaning$id", wordPairId = id,
        sides = listOf(Side(sideId = id, wordPairId = id, shown = Shown.WORD)),
    )

    private fun stack(vararg pairs: WordPair) = MemoStack(name = "Serbian").apply { words = pairs.toMutableList() }

    private fun urgency(stack: MemoStack) =
        stack.reviewUrgency(PracticeSides.WORD_TO_MEANING, now, utc, fsrs)

    @Test
    fun `nothing due is no urgency`() {
        val notDue = WordPair(
            parentStackId = 1, word1 = "w", word2 = "m", wordPairId = 1,
            sides = listOf(
                Side(
                    sideId = 1, wordPairId = 1, shown = Shown.WORD, state = SideState.Review,
                    stability = 5.0, due = now + 3 * DAY_MILLIS, lastReview = now - DAY_MILLIS,
                )
            ),
        )
        assertEquals(0f, urgency(stack(notDue)), 0f)
    }

    @Test
    fun `new words have nothing to forget yet`() {
        assertEquals(0f, urgency(stack(newPair(1), newPair(2))), 0f)
    }

    @Test
    fun `urgency is the words expected forgotten, against ten`() {
        val due = pair(1, stability = 5.0, elapsedDays = 20, overdueDays = 10)
        val forgetting = 1 - fsrs.retrievability(5.0, 20)
        assertEquals((forgetting / 10).toFloat(), urgency(stack(due)), 1e-6f)
    }

    @Test
    fun `more and older words are more urgent, up to full`() {
        val few = stack(pair(1, stability = 5.0, elapsedDays = 6, overdueDays = 1))
        val many = stack(*(1L..5L).map { pair(it, stability = 5.0, elapsedDays = 6, overdueDays = 1) }.toTypedArray())
        assert(urgency(many) > urgency(few))
        val lost = stack(*(1L..40L).map { pair(it, stability = 1.0, elapsedDays = 100, overdueDays = 90) }.toTypedArray())
        assertEquals(1f, urgency(lost), 0f)
    }
}
