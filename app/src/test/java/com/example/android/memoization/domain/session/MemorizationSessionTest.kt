package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState
import com.example.android.memoization.domain.scheduler.Fsrs
import com.example.android.memoization.domain.scheduler.Rating
import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone
import kotlin.random.Random

class MemorizationSessionTest {

    private val now = 1_800_000_000_000L
    private val scheduler = Fsrs(fuzz = false, zone = TimeZone.getTimeZone("UTC"))

    private fun side(id: Long, shown: Shown = Shown.WORD, pairId: Long = id) = Side(
        sideId = id,
        wordPairId = pairId,
        shown = shown,
        state = SideState.Review,
        stability = 5.0,
        difficulty = 5.0,
        due = now - DAY_MILLIS,
        lastReview = now - 6 * DAY_MILLIS,
        reps = 1,
    )

    private fun item(id: Long, shown: Shown = Shown.WORD, pairId: Long = id) =
        SessionSide(side(id, shown, pairId), front = "front$id", back = "back$id")

    private fun session(vararg ids: Long, seed: Int = 1) = MemorizationSession(
        ids.map { item(it) }, Random(seed), scheduler
    )

    private fun MemorizationSession.drain(): List<Long> {
        val seen = mutableListOf<Long>()
        while (!state().isFinished) {
            seen += state().current!!.id
            rate(Rating.Good, now)
        }
        return seen
    }

    @Test
    fun `asks the sides in the order it was given`() {
        val ids = LongArray(20) { it + 1L }
        val s = session(*ids, seed = 7)

        val first = s.state().current
        assertEquals(first, s.state().current)

        assertEquals(ids.toList(), s.drain())
    }

    @Test
    fun `an empty session is finished immediately`() {
        val s = MemorizationSession(emptyList())

        assertTrue(s.state().isFinished)
        assertTrue(s.state().isDoneForToday)
        assertNull(s.state().current)
        assertEquals(0, s.state().remaining)
        assertEquals(1f, s.state().progress, 0f)
    }

    @Test
    fun `a session answered to the end is done for today`() {
        val s = MemorizationSession(listOf(item(1)), Random(1), scheduler)

        assertEquals(0, s.state().waiting)
        assertFalse(s.state().isFinished)
        s.rate(Rating.Good, now)
        assertTrue(s.state().isFinished)
        assertTrue(s.state().isDoneForToday)
        assertEquals(1, s.state().done)
        assertEquals(0, s.state().left)
    }

    @Test
    fun `done and left add up to the total whether the session goes on or is stopped`() {
        val s = MemorizationSession(listOf(item(1), item(2), item(3)), Random(1), scheduler, offerStopFrom = 1)
        assertEquals(0, s.state().done)
        assertEquals(3, s.state().left)

        s.rate(Rating.Good, now)
        s.rate(Rating.Again, now)
        // One closed; the Again side is back in the queue, so it is not done and still left.
        assertEquals(1, s.state().done)
        assertEquals(2, s.state().left)

        val stopped = s.stop()
        assertEquals(1, stopped.done)
        assertEquals(2, stopped.left)
        assertEquals(2, stopped.waiting)
    }

    @Test
    fun `stopping is offered after enough answers and withdrawn once the queue is empty`() {
        val s = MemorizationSession(listOf(item(1), item(2), item(3)), Random(1), scheduler, offerStopFrom = 2)

        assertFalse(s.state().stopOffered)
        s.rate(Rating.Good, now)
        assertFalse(s.state().stopOffered)
        s.rate(Rating.Good, now)
        assertTrue(s.state().stopOffered)
        s.rate(Rating.Good, now)
        assertTrue(s.state().isFinished)
        assertFalse("nothing to stop once the queue has drained", s.state().stopOffered)
    }

    @Test
    fun `an answer after Again counts towards the offer like any other tap`() {
        val s = MemorizationSession(listOf(item(1)), Random(1), scheduler, offerStopFrom = 2)

        s.rate(Rating.Again, now)
        assertEquals(1, s.answered)
        assertFalse(s.state().stopOffered)
        assertFalse(s.state().isFinished)
        // The same side came back; this tap is the second answer.
        s.rate(Rating.Again, now)
        assertEquals(2, s.answered)
        assertTrue(s.state().stopOffered)
    }

    @Test
    fun `stopping ends the session and leaves the rest of the queue waiting`() {
        val s = MemorizationSession(
            listOf(item(1), item(2), item(3), item(4)), Random(1), scheduler, offerStopFrom = 1
        )
        s.rate(Rating.Good, now)
        s.rate(Rating.Again, now)

        val state = s.stop()

        assertTrue(state.isFinished)
        assertFalse(state.isDoneForToday)
        assertEquals(0, state.remaining)
        // Two never answered plus the one re-queued after Again.
        assertEquals(3, state.waiting)
        assertEquals(2, s.answered)
        assertFalse(state.stopOffered)
    }

    @Test
    fun `both sides of one pair are separate items`() {
        val s = MemorizationSession(
            listOf(item(1, Shown.WORD, pairId = 7), item(2, Shown.MEANING, pairId = 7)),
            Random(1),
            scheduler
        )

        assertEquals(2, s.state().total)
        val order = s.drain()
        assertEquals(setOf(1L, 2L), order.toSet())
    }

    @Test
    fun `progress is closed sides over the starting total and holds still on again`() {
        val s = session(1, 2, 3, 4)
        assertEquals(4, s.state().total)
        assertEquals(0f, s.state().progress, 0f)

        assertEquals(0.25f, s.rate(Rating.Good, now).state.progress, 0.001f)
        assertEquals(0.25f, s.rate(Rating.Again, now).state.progress, 0.001f)
        assertEquals(0.5f, s.rate(Rating.Hard, now).state.progress, 0.001f)
        assertEquals(4, s.state().total)
    }

    @Test
    fun `every answer is reported for the log, requeued ones with no schedule change`() {
        val s = session(1, 2)
        val first = s.state().current!!

        val again = s.rate(Rating.Again, now)
        assertEquals(first.id, again.answer.sideId)
        assertEquals(Rating.Again, again.answer.rating)
        assertFalse(again.answer.requeued)
        assertEquals(again.toPersist, again.answer.after)

        // The other side, then the lapsed one comes round: its answer is requeued and the
        // side it reports is the one already rescheduled by the Again.
        s.rate(Rating.Good, now)
        val back = s.rate(Rating.Good, now)
        assertEquals(first.id, back.answer.sideId)
        assertTrue(back.answer.requeued)
        assertNull(back.toPersist)
        assertEquals(again.toPersist, back.answer.after)
    }

    @Test
    fun `good schedules the side further out and counts down`() {
        val s = session(1, 2, 3)
        val first = s.state().current!!

        val outcome = s.rate(Rating.Good, now)
        val persisted = outcome.toPersist!!

        assertEquals(first.id, persisted.sideId)
        assertEquals(SideState.Review, persisted.state)
        assertTrue("stability should grow", persisted.stability!! > 5.0)
        assertEquals(now, persisted.lastReview)
        assertTrue("due should be in the future", persisted.due!! > now)
        assertEquals(2, persisted.reps)
        assertEquals(0, persisted.lapses)
        assertEquals(2, outcome.state.remaining)
    }

    @Test
    fun `again sends the side to the back, drops its stability and counts a lapse`() {
        val s = session(1, 2, 3)
        val lapsed = s.state().current!!

        val outcome = s.rate(Rating.Again, now)
        val persisted = outcome.toPersist!!

        assertEquals(lapsed.id, persisted.sideId)
        assertTrue("stability should fall", persisted.stability!! < 5.0)
        assertEquals(1, persisted.lapses)
        assertEquals(3, outcome.state.remaining)
        assertNotEquals(lapsed.id, outcome.state.current?.id)

        // Work through the other two; the lapsed side comes round last, already rescheduled.
        s.rate(Rating.Good, now)
        s.rate(Rating.Good, now)
        assertEquals(lapsed.id, s.state().current?.id)
        assertEquals(persisted.stability, s.state().current?.side?.stability)
    }

    @Test
    fun `a second again on the same side persists nothing`() {
        val s = session(1)

        assertNotNull(s.rate(Rating.Again, now).toPersist)
        assertNull(s.rate(Rating.Again, now).toPersist)
        assertEquals(1, s.state().remaining)
    }

    @Test
    fun `good after again closes the side without a new schedule`() {
        val s = session(1)

        s.rate(Rating.Again, now)
        val outcome = s.rate(Rating.Good, now)

        assertNull(outcome.toPersist)
        assertTrue(outcome.state.isFinished)
    }

    @Test
    fun `hard after again also closes the side without a write`() {
        val s = session(1)

        s.rate(Rating.Again, now)
        val outcome = s.rate(Rating.Hard, now)

        assertNull(outcome.toPersist)
        assertTrue(outcome.state.isFinished)
    }

    @Test
    fun `the last good finishes the session`() {
        val s = session(1, 2)

        s.rate(Rating.Good, now)
        assertFalse(s.state().isFinished)
        val outcome = s.rate(Rating.Good, now)

        assertTrue(outcome.state.isFinished)
        assertEquals(0, outcome.state.remaining)
    }

    @Test
    fun `serial grows on every rating so a returning side reads as a new attempt`() {
        val s = session(1)

        assertEquals(0, s.state().serial)
        val afterAgain = s.rate(Rating.Again, now).state
        assertEquals(1, afterAgain.serial)
        assertEquals(1L, afterAgain.current?.id)
        assertEquals(2, s.rate(Rating.Again, now).state.serial)
    }
}
