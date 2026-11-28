package com.example.android.memoization.domain.session

import com.example.android.memoization.domain.session.MemorizationSession.Results
import kotlin.math.max

/**
 * What a finished session shows the learner: how many sides were practised, how each was first
 * answered, and how the mistakes compare with what the scheduler expected.
 */
data class SessionSummary(
    /** Distinct sides answered at least once. */
    val practised: Int,
    /** Each practised side once, by the button of its first answer; adds up to [practised]. */
    val firstAnswers: Results,
    /** Null when every side practised was new: there is nothing to have forgotten. */
    val mistakes: Mistakes?,
)

/**
 * Mistakes among the sides practised before - a side whose first answer in the session was
 * Forgot; Hard, checked wrong or not, is a recall to FSRS and not a mistake. [expected] is what
 * FSRS predicted for them: the sum of each side's chance of being forgotten when it was shown.
 */
data class Mistakes(val count: Int, val reviewed: Int, val expected: Double) {

    /**
     * Within [WINDOW] of the expected count, but never narrower than one mistake either way,
     * or a small session could land on no whole number inside it.
     */
    val verdict: Verdict
        get() {
            val margin = max(expected * WINDOW, MIN_MARGIN)
            return when {
                count < expected - margin -> Verdict.FEW
                count > expected + margin -> Verdict.MANY
                else -> Verdict.AS_EXPECTED
            }
        }

    enum class Verdict { FEW, AS_EXPECTED, MANY }

    companion object {
        const val WINDOW = 0.15
        const val MIN_MARGIN = 1.0
    }
}
