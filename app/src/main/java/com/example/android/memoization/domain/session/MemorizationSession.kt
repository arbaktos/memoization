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
        /** How the sides answered so far went, for the session's progress bar. */
        val results: Results = Results(),
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

    /**
     * The sides answered so far, each counted once by its worst answer in this session. A
     * Forgot counts at once, though the side is still owed - the learner sees the tap land -
     * and recalling it later adds nothing; Hard and Good count when they close the side. The
     * total can so run ahead of the sides done while forgotten ones wait to be recalled.
     */
    data class Results(val good: Int = 0, val hard: Int = 0, val forgot: Int = 0) {
        val total: Int get() = good + hard + forgot
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
     * applied, [rescheduled] whether the answer changed it - a second Again on a side does
     * not, nor does anything the scheduler ignores.
     */
    data class Answer(
        val sideId: Long,
        val rating: Rating,
        val requeued: Boolean,
        val rescheduled: Boolean,
        val after: Side,
    )

    private val queue = ArrayDeque(sides)
    private val total = sides.size
    private val lapsedIds = mutableSetOf<Long>()
    private var serial = 0
    private var waiting = 0
    private var results = Results()

    fun state(): State = State(
        current = queue.firstOrNull(),
        remaining = queue.size,
        total = total,
        serial = serial,
        waiting = waiting,
        stopOffered = queue.isNotEmpty() && serial >= offerStopFrom,
        results = results,
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
     * Again: the lapse is written once, on the first Again - stability drops, a lapse is
     * counted - but the side is kept due today, and it goes to the back of the queue. It is
     * not done until it is recalled, and the session may end before that: a side still owed
     * is still due, in the stack's count and in the next sitting, whenever that is. A second
     * Again changes nothing more.
     *
     * Hard/Good on a side answered right first time: written as scheduled. Hard/Good after an
     * Again in this session: the recall is not rated again - the lapse set the stability -
     * it only lets the lapse's schedule start, so the due date moves out and nothing else.
     */
    fun rate(rating: Rating, now: Long): Outcome {
        val item = queue.removeFirst()
        serial++
        val lapsed = item.id in lapsedIds
        val toPersist = when (rating) {
            Rating.Again -> if (lapsed) {
                queue.addLast(item)
                null
            } else {
                val reviewed = scheduler.review(item.side, Rating.Again, now).copy(due = now)
                lapsedIds += item.id
                queue.addLast(item.copy(side = reviewed))
                reviewed
            }
            else -> if (lapsed) scheduler.dueFrom(item.side, now) else scheduler.review(item.side, rating, now)
        }
        if (!lapsed) results = when (rating) {
            Rating.Again -> results.copy(forgot = results.forgot + 1)
            Rating.Hard -> results.copy(hard = results.hard + 1)
            else -> results.copy(good = results.good + 1)
        }
        val answer = Answer(
            sideId = item.id,
            rating = rating,
            requeued = lapsed,
            rescheduled = toPersist != null,
            after = toPersist ?: item.side,
        )
        return Outcome(toPersist, state(), answer)
    }
}
