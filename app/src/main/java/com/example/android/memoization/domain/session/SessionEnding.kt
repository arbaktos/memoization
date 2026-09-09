package com.example.android.memoization.domain.session

/** The three ways a session ends; stored on the session row by [code]. */
enum class SessionEnding(val code: Int) {
    /** Every side in the queue was answered. */
    DRAINED(1),
    /** The learner tapped "Enough for today"; see [Stamina]. */
    ENOUGH(2),
    /** The learner left the screen. */
    LEFT(3);

    companion object {
        fun fromCode(code: Int): SessionEnding = entries.first { it.code == code }
    }
}
