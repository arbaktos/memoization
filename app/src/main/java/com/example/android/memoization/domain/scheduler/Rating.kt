package com.example.android.memoization.domain.scheduler

/**
 * The learner's answer to a side, as one of FSRS's grades 1..4.
 *
 * The app has three buttons and all four grades: Forgot is Again; Easy is Easy; Hard is checked
 * against the answer - recalled right it is Good, recalled wrong it is Hard. So Good and Hard
 * name grades, not buttons, and only Hard is a button and a grade of the same name.
 */
enum class Rating(val grade: Int) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4),
}
