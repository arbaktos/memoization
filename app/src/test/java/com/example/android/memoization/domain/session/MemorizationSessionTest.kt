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
    fun `every answer fills a slot by its button, and every forgot adds a slot for the repeat`() {
        val s = session(1, 2, 3, 4)
        assertEquals(MemorizationSession.Results(), s.state().results)
        assertEquals(4, s.state().slots)

        s.rate(Rating.Easy, now)
        s.rate(Rating.Hard, now)
        val afterAgain = s.rate(Rating.Again, now).state
        assertEquals("a forgot shows at once", MemorizationSession.Results(easy = 1, hard = 1, forgot = 1), afterAgain.results)
        assertEquals("and the repeat gets a slot of its own", 5, afterAgain.slots)

        // Good comes from Hard checked right: it counts with Hard, by the button pressed.
        s.rate(Rating.Good, now)
        // The forgotten side comes round and is forgotten again: one more slot.
        assertEquals(6, s.rate(Rating.Again, now).state.slots)
        val recalled = s.rate(Rating.Easy, now).state
        assertTrue(recalled.isFinished)
        assertEquals(MemorizationSession.Results(easy = 2, hard = 2, forgot = 2), recalled.results)
        assertEquals("a full bar is a finished session", recalled.slots, recalled.results.total)
    }

    private fun newItem(id: Long) = SessionSide(
        Side(sideId = id, wordPairId = id, shown = Shown.WORD), front = "front$id", back = "back$id"
    )

    @Test
    fun `the summary counts first answers only, mistakes among sides practised before`() {
        val s = MemorizationSession(listOf(item(1), item(2), newItem(3)), Random(1), scheduler)

        s.rate(Rating.Again, now)   // side 1: a mistake
        s.rate(Rating.Hard, now)    // side 2: Hard is a recall, not a mistake
        s.rate(Rating.Again, now)   // side 3: new, so not a mistake
        s.rate(Rating.Again, now)   // side 1 again: a repeat, not a second mistake
        s.rate(Rating.Easy, now)    // side 3 recalled
        val summary = s.summary()
        s.rate(Rating.Good, now)    // side 1 recalled

        assertEquals(3, summary.practised)
        assertEquals(
            "each side once, by its first answer",
            MemorizationSession.Results(easy = 0, hard = 1, forgot = 2),
            summary.firstAnswers,
        )
        val mistakes = summary.mistakes!!
        assertEquals(1, mistakes.count)
        assertEquals(2, mistakes.reviewed)
        val forgetting = 1 - scheduler.retrievability(side(1), now)!!
        assertEquals("what FSRS expected for the two sides practised before", 2 * forgetting, mistakes.expected, 1e-9)
    }

    @Test
    fun `the summary's first answers add up to the sides practised, repeats left out`() {
        val s = MemorizationSession(listOf(item(1), item(2), newItem(3)), Random(1), scheduler)
        s.rate(Rating.Easy, now)
        s.rate(Rating.Again, now)
        s.rate(Rating.Good, now)    // side 3
        s.rate(Rating.Good, now)    // side 2 recalled: a repeat
        s.stop()

        val summary = s.summary()
        assertEquals(3, summary.practised)
        assertEquals(summary.practised, summary.firstAnswers.total)
        assertEquals(MemorizationSession.Results(easy = 1, hard = 1, forgot = 1), summary.firstAnswers)
    }

    @Test
    fun `a session of only new sides has no mistakes to judge`() {
        val s = MemorizationSession(listOf(newItem(1)), Random(1), scheduler)
        s.rate(Rating.Again, now)

        assertNull(s.summary().mistakes)
    }

    @Test
    fun `easy schedules a side further out than good, and hard closes it without a lapse`() {
        val easy = session(1).rate(Rating.Easy, now)
        val good = session(1).rate(Rating.Good, now)
        val hard = session(1).rate(Rating.Hard, now)

        assertTrue(easy.toPersist!!.due!! > good.toPersist!!.due!!)
        assertTrue(easy.state.isFinished)
        assertTrue("hard is not sent round again", hard.state.isFinished)
        assertEquals(0, hard.toPersist!!.lapses)
    }

    @Test
    fun `every answer is reported for the log, with whether it changed the schedule`() {
        val s = session(1, 2)
        val first = s.state().current!!

        val again = s.rate(Rating.Again, now)
        assertEquals(first.id, again.answer.sideId)
        assertEquals(Rating.Again, again.answer.rating)
        assertFalse(again.answer.requeued)
        assertTrue(again.answer.rescheduled)
        assertEquals(again.toPersist, again.answer.after)

        // The other side, then the lapsed one comes round: its answer is requeued, and the
        // side it reports is the lapsed one with its due date now set.
        s.rate(Rating.Good, now)
        val back = s.rate(Rating.Good, now)
        assertEquals(first.id, back.answer.sideId)
        assertTrue(back.answer.requeued)
        assertTrue(back.answer.rescheduled)
        assertEquals(back.toPersist, back.answer.after)
    }

    @Test
    fun `a second again is reported as changing nothing`() {
        val s = session(1)
        s.rate(Rating.Again, now)

        val second = s.rate(Rating.Again, now)

        assertTrue(second.answer.requeued)
        assertFalse(second.answer.rescheduled)
        assertNull(second.toPersist)
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
        assertEquals("a lapsed side stays due until it is recalled", now, persisted.due)
        assertEquals(3, outcome.state.remaining)
        assertNotEquals(lapsed.id, outcome.state.current?.id)

        // Work through the other two; the lapsed side comes round last, with the lapse on it.
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
    fun `good after again lets the lapse's schedule start, without re-rating the side`() {
        val s = session(1)

        val lapse = s.rate(Rating.Again, now).toPersist!!
        val outcome = s.rate(Rating.Good, now)
        val recalled = outcome.toPersist!!

        assertTrue(outcome.state.isFinished)
        assertEquals(lapse.stability, recalled.stability)
        assertEquals(lapse.difficulty, recalled.difficulty)
        assertEquals(lapse.lapses, recalled.lapses)
        assertEquals(lapse.reps, recalled.reps)
        assertTrue("due should move out of today", recalled.due!! > now)
        assertEquals(now + DAY_MILLIS, recalled.due)
    }

    @Test
    fun `hard after again does the same as good`() {
        val s = session(1)

        val lapse = s.rate(Rating.Again, now).toPersist!!
        val outcome = s.rate(Rating.Hard, now)
        val recalled = outcome.toPersist!!

        assertTrue(outcome.state.isFinished)
        assertEquals(lapse.stability, recalled.stability)
        assertEquals(now + DAY_MILLIS, recalled.due)
    }

    @Test
    fun `a lapse not recalled before the session ends is still due`() {
        val s = MemorizationSession(listOf(item(1), item(2)), Random(1), scheduler, offerStopFrom = 1)

        val lapse = s.rate(Rating.Again, now).toPersist!!
        s.stop()

        assertEquals(now, lapse.due)
        assertTrue(lapse.isDue(now, TimeZone.getTimeZone("UTC")))
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
