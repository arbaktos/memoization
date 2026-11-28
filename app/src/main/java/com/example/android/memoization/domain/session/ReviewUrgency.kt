package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.domain.scheduler.Fsrs
import java.util.TimeZone

/** Words expected forgotten at which reviewing counts as fully urgent. */
const val FULL_URGENCY_FORGOTTEN = 10.0

/**
 * How urgent reviewing this stack is, from 0 to 1: the words expected forgotten among the sides
 * due - the sum of their chances of being forgotten, 1 - retrievability - against
 * [FULL_URGENCY_FORGOTTEN]. It grows with both how many are due and how far past due they are.
 * A side never practised has no memory to lose and adds nothing. Study is as bright as this.
 */
fun MemoStack.reviewUrgency(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
    scheduler: Fsrs = Fsrs(zone = zone),
): Float {
    val forgotten = words.sumOf { pair ->
        pair.dueSides(practice, now, zone).sumOf { side -> scheduler.retrievability(side, now)?.let { 1 - it } ?: 0.0 }
    }
    return (forgotten / FULL_URGENCY_FORGOTTEN).coerceIn(0.0, 1.0).toFloat()
}
