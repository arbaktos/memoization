package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.Side
import com.example.android.memoization.domain.scheduler.Fsrs
import com.example.android.memoization.domain.scheduler.Rating
import kotlin.random.Random

/**
 * One sitting with a stack: the sides waiting to be answered, shuffled once on entry and worked
 * through one at a time. A side rated Again goes to the back of the queue and keeps coming round
 * until it is rated Hard or Good, so nothing leaves the session unrecalled.
 *
 * Pure Kotlin - the ViewModel feeds it ratings and persists whatever [Outcome.toPersist] says.
 */
class MemorizationSession(
    sides: List<SessionSide>,
    random: Random = Random.Default,
    private val scheduler: Fsrs = Fsrs(random = random),
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
    ) {
        val isFinished: Boolean get() = current == null

        /** Share of the session done, 0f..1f; an empty session counts as done. */
        val progress: Float
            get() = if (total == 0) 1f else (total - remaining).toFloat() / total
    }

    data class Outcome(
        /** The side as it should be written to the database, or null if nothing changed. */
        val toPersist: Side?,
        val state: State,
    )

    private val queue = ArrayDeque(sides.shuffled(random))
    private val total = sides.size
    private val lapsedIds = mutableSetOf<Long>()
    private var serial = 0

    fun state(): State = State(queue.firstOrNull(), queue.size, total, serial)

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
        return Outcome(toPersist, state())
    }
}
