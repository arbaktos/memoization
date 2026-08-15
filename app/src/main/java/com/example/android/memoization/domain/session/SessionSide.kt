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

/** Everything of this stack that is waiting to be answered, one entry per due practised side. */
fun MemoStack.dueSessionSides(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
): List<SessionSide> = words.flatMap { pair ->
    pair.dueSides(practice, now, zone).map { side ->
        SessionSide(side = side, front = pair.front(side.shown), back = pair.back(side.shown))
    }
}
