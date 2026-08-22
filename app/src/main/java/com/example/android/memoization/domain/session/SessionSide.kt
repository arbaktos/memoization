package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.Side
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
 * What this stack asks for now: the most overdue practised sides first, then sides never
 * practised, up to [limit] for this session. Sides answered earlier today have been scheduled
 * forward and are simply not due, so a second sitting picks up the next batch.
 *
 * The order here is only the selection; MemorizationSession shuffles what it is given.
 */
fun MemoStack.dueSessionSides(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
    limit: Int = SessionDefaults.SESSION_LIMIT,
): List<SessionSide> =
    words.flatMap { pair ->
        pair.dueSides(practice, now, zone).map { side ->
            SessionSide(side = side, front = pair.front(side.shown), back = pair.back(side.shown))
        }
    }
        // Earliest due date first; sides never practised have no due date and fill what is left.
        .sortedWith(compareBy(nullsLast()) { it.side.due })
        .take(limit)
