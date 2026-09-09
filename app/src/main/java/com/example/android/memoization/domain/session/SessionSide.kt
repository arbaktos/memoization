package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.domain.scheduler.Fsrs
import java.util.TimeZone

/** One thing to answer in a session: a side, with the texts to show and to recall. */
data class SessionSide(
    val side: Side,
    val front: String,
    val back: String,
) {
    val id: Long get() = side.sideId
}

object SessionDefaults {
    /**
     * How many sides of a stack one session asks for. Someone who stops for a few months would
     * otherwise come back to the whole stack at once; with a cap the backlog is worked off a
     * sitting at a time, and the schedule is none the worse for it - a side left for later
     * simply stays overdue, and being recalled late earns it more. A learner who wants more the
     * same day just starts another session.
     *
     * A setting one day; for now a number to live with and change.
     */
    const val SESSION_LIMIT = 37
}

/**
 * What this stack asks for now: the practised sides that are due, the most forgotten first,
 * then sides never practised, up to [limit] for this session. "Most forgotten" is FSRS
 * retrievability, the chance of recalling the side today: a side a day late on a one-day
 * interval is far more forgotten than one three days late on a month, and it goes first. Sides
 * answered earlier today have been scheduled forward and are simply not due, so a second
 * sitting picks up the next batch.
 *
 * The order here is only the selection; MemorizationSession shuffles what it is given.
 */
fun MemoStack.dueSessionSides(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
    limit: Int = SessionDefaults.SESSION_LIMIT,
    scheduler: Fsrs = Fsrs(zone = zone),
): List<SessionSide> =
    words.flatMap { pair ->
        pair.dueSides(practice, now, zone).map { side ->
            SessionSide(side = side, front = pair.front(side.shown), back = pair.back(side.shown))
        }
    }
        .sortedWith(
            // Lowest retrievability first. Sides due on the day FSRS asked for all sit at the
            // desired retention, so among them the weaker memory goes first; sides never
            // practised have no retrievability and fill what is left.
            compareBy<SessionSide, Double?>(nullsLast()) { scheduler.retrievability(it.side, now) }
                .thenBy(nullsLast()) { it.side.stability }
        )
        .take(limit)

/** Every practised side of this stack that is due now, whether or not it fits one session. */
fun MemoStack.dueSideCount(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
): Int = words.sumOf { it.dueSides(practice, now, zone).size }
