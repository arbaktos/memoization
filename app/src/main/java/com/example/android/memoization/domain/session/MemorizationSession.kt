package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.Side
import com.example.android.memoization.domain.scheduler.Fsrs
import com.example.android.memoization.domain.scheduler.Rating
import kotlin.random.Random

/**
 * One sitting with a stack: everything due, in the order given (see dueSessionSides: the most
 * forgotten first), worked through one at a time. A side rated Again goes to the back of the
 * queue and keeps coming round until it is rated Hard or Good, so nothing leaves the session
 * unrecalled. There is no size: the session ends when the queue is empty, when the learner
 * says they have had enough ([stop]), or when they leave the screen.
 *
 * Pure Kotlin - the ViewModel feeds it ratings and persists whatever [Outcome.toPersist] says.
 */
class MemorizationSession(
    sides: List<SessionSide>,
    random: Random = Random.Default,
    private val scheduler: Fsrs = Fsrs(random = random),
    /** Answers after which the learner is offered to stop; see [Stamina]. */
    private val offerStopFrom: Int = Stamina.DEFAULT_OFFER,
) {

    data class State(
        /** The side on screen; null once the queue is empty. */
        val current: SessionSide?,
        /** Sides still in the queue, the current one included. */
        val remaining: Int,
        /** Sides the session started with; one re-queued after Again is not counted twice. */
        val total: Int,
        /** Bumped on every rating so the UI can tell "same side, next attempt" apart. */
        val serial: Int,
        /** Sides still in the queue when the learner stopped; another sitting would take them. */
        val waiting: Int,
        /** True once enough has been answered that stopping here is offered; see [stop]. */
        val stopOffered: Boolean,
    ) {
        val isFinished: Boolean get() = current == null

        /** Sides answered to a close, whether the session went on or was stopped after. */
        val done: Int get() = total - remaining - waiting

        /** Sides not got to: still queued, or left behind by [stop]. */
        val left: Int get() = remaining + waiting

        /** True once the queue is empty because everything in it was answered. */
        val isDoneForToday: Boolean get() = isFinished && waiting == 0

        /** Share of the session done, 0f..1f; an empty session counts as done. */
        val progress: Float
            get() = if (total == 0) 1f else (total - remaining).toFloat() / total
    }

    data class Outcome(
        /** The side as it should be written to the database, or null if nothing changed. */
        val toPersist: Side?,
        val state: State,
        /** What was answered, for the review log - every tap, scheduled or not. */
        val answer: Answer,
    )

    /**
     * One answer as the log wants it: [after] is the side's schedule once the answer is
     * applied - unchanged for a [requeued] answer, which the scheduler ignores.
     */
    data class Answer(
        val sideId: Long,
        val rating: Rating,
        val requeued: Boolean,
        val after: Side,
    )

    private val queue = ArrayDeque(sides)
    private val total = sides.size
    private val lapsedIds = mutableSetOf<Long>()
    private var serial = 0
    private var waiting = 0

    fun state(): State = State(
        current = queue.firstOrNull(),
        remaining = queue.size,
        total = total,
        serial = serial,
        waiting = waiting,
        stopOffered = queue.isNotEmpty() && serial >= offerStopFrom,
    )

    /** Answers given so far, every tap counted - what "Enough for today" is measured in. */
    val answered: Int get() = serial

    /**
     * Enough for today: the queue is dropped and its sides are left waiting for another
     * sitting. Nothing needs writing for them - a side rated Again already had its lapse
     * saved, and one never answered is untouched and still due.
     */
    fun stop(): State {
        waiting = queue.size
        queue.clear()
        return state()
    }

    /**
     * Again: the schedule is rewritten once, on the first lapse, and the side goes to the back
     * of the queue. Hard/Good on a side answered right first time: written as scheduled.
     * Hard/Good after an Again in this session: the side is done for today but earns no new
     * schedule, so nothing is written.
     */
    fun rate(rating: Rating, now: Long): Outcome {
        val item = queue.removeFirst()
        serial++
        val lapsed = item.id in lapsedIds
        val toPersist = when (rating) {
            Rating.Again -> {
                val reviewed = scheduler.review(item.side, Rating.Again, now)
                queue.addLast(item.copy(side = reviewed))
                if (lapsed) null else {
                    lapsedIds += item.id
                    reviewed
                }
            }
            else -> if (lapsed) null else scheduler.review(item.side, rating, now)
        }
        val answer = Answer(item.id, rating, requeued = lapsed, after = toPersist ?: item.side)
        return Outcome(toPersist, state(), answer)
    }
}
