package com.example.android.memoization.domain.scheduler

/**
 * The learner's answer to a side. The grades are FSRS's own 1..4; the app only ever sends
 * Again, Hard and Good - Easy exists because the formulas and the reference vectors use it.
 */
enum class Rating(val grade: Int) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4),
}
