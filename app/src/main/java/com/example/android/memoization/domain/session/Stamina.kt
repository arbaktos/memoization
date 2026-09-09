package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.BaseStack
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * How far into a session the learner tends to stop with this stack: the running mean of the
 * answers given in every session ended with "Enough for today". Two counters instead of the
 * history, so the mean costs nothing to keep and nothing to read.
 *
 * The button is offered at three quarters of that mean, not at the mean itself. Offered at the
 * mean, every stop would land at or past it and the mean could only ever climb; a little
 * earlier, a stop can fall below it and pull it back down, so the point drifts both ways.
 */
data class Stamina(
    val tiredSessions: Int = 0,
    val tiredAnswersSum: Int = 0,
) {
    /** Mean answers per "enough" session; null until the learner has stopped once. */
    val average: Double? get() = if (tiredSessions == 0) null else tiredAnswersSum.toDouble() / tiredSessions

    /** Answers after which the session offers to stop. */
    val offerFrom: Int
        get() = average?.let { max(MIN_OFFER, (it * OFFER_SHARE).roundToInt()) } ?: DEFAULT_OFFER

    /** The record after one more session stopped at [answers] answers. */
    fun stoppedAt(answers: Int): Stamina =
        Stamina(tiredSessions + 1, tiredAnswersSum + answers)

    companion object {
        /** Offered from here until the learner has stopped once; a setting one day. */
        const val DEFAULT_OFFER = 30
        /** Never earlier than this, however short the stops become. */
        const val MIN_OFFER = 10
        const val OFFER_SHARE = 0.75
    }
}

val BaseStack.stamina: Stamina get() = Stamina(tiredSessions, tiredAnswersSum)
