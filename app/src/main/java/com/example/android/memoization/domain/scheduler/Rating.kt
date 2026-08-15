package com.example.android.memoization.domain.scheduler

/**
 * The learner's answer to a side, as one of FSRS's grades 1..4.
 *
 * The app has three buttons and sends only Again, Hard and Good. Grade 4 is never sent and has
 * no button; it is declared because two formulas need it as a constant - the difficulty of a
 * grade-4 answer is what difficulty reverts towards - and because the reference vectors that
 * pin this implementation cover all four grades.
 */
enum class Rating(val grade: Int) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4),
}
