package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.domain.scheduler.Fsrs
import java.util.TimeZone
import kotlin.random.Random

/** One thing to answer in a session: a side, with the texts to show and to recall. */
data class SessionSide(
    val side: Side,
    val front: String,
    val back: String,
) {
    val id: Long get() = side.sideId
}

/**
 * What this stack asks for now, in the order the session will ask it: every practised side
 * that is due, the most forgotten first, then every side never practised. A session has no
 * size - it takes all of it and ends when the queue is empty, when the learner has had enough
 * (see [Stamina]) or when they leave - so the order is what decides which sides a learner who
 * stops early has answered and which are still waiting.
 *
 * "Most forgotten" is FSRS retrievability, the chance of recalling the side today: a side a day
 * late on a one-day interval is far more forgotten than one three days late on a month, and it
 * goes first. Sides due on the day FSRS asked for all sit at the desired retention, so among
 * them the weaker memory goes first. Sides never practised have no retrievability and come
 * last. Where nothing tells two sides apart - equal retrievability, or none - the order is
 * random: the list is shuffled first and the sort is stable.
 *
 * Sides answered earlier today have been scheduled forward and are simply not due, so a second
 * sitting picks up whatever the first left.
 */
fun MemoStack.dueSessionSides(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
    random: Random = Random.Default,
    scheduler: Fsrs = Fsrs(zone = zone),
): List<SessionSide> =
    words.flatMap { pair ->
        pair.dueSides(practice, now, zone).map { side ->
            SessionSide(side = side, front = pair.front(side.shown), back = pair.back(side.shown))
        }
    }
        .shuffled(random)
        .sortedWith(
            compareBy<SessionSide, Double?>(nullsLast()) { scheduler.retrievability(it.side, now) }
                .thenBy(nullsLast()) { it.side.stability }
        )
