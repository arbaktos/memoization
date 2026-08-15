package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.domain.scheduler.localDay
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
     * How many sides of a stack a learner is asked for in one day. Someone who stops for a few
     * months would otherwise come back to the whole stack at once; with a cap the backlog is
     * worked off over a few days instead, and the schedule is none the worse for it - a side
     * left for tomorrow simply stays overdue, and being recalled late earns it more.
     *
     * A setting one day; for now a number to live with and change.
     */
    const val DAILY_LIMIT = 37
}

/**
 * What this stack asks for today: the most overdue practised sides first, then sides never
 * practised, up to [limit] for the day. Sides already answered today count against the day's
 * allowance, so a second sitting continues where the first stopped rather than starting over.
 *
 * The order here is only the selection; MemorizationSession shuffles what it is given.
 */
fun MemoStack.dueSessionSides(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
    limit: Int = SessionDefaults.DAILY_LIMIT,
): List<SessionSide> {
    val allowance = limit - answeredToday(practice, now, zone)
    if (allowance <= 0) return emptyList()

    return words.flatMap { pair ->
        pair.dueSides(practice, now, zone).map { side ->
            SessionSide(side = side, front = pair.front(side.shown), back = pair.back(side.shown))
        }
    }
        // Earliest due date first; sides never practised have no due date and fill what is left.
        .sortedWith(compareBy(nullsLast()) { it.side.due })
        .take(allowance)
}

private fun MemoStack.answeredToday(
    practice: PracticeSides,
    now: Long,
    zone: TimeZone,
): Int = words.sumOf { pair ->
    pair.activeSides(practice).count { side ->
        side.lastReview?.let { localDay(it, zone) == localDay(now, zone) } == true
    }
}
