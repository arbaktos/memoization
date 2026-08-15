package com.example.android.memoization.data.model

/**
 * How well a side is known, shown as a colour. It is no longer stored: the schedule lives in
 * FSRS stability, and the level is a reading of it - roughly "how many days this memory holds".
 */
sealed class WordStatus(val frequency: Int) {
    data object Level1 : WordStatus(1)
    data object Level2 : WordStatus(2)
    data object Level3 : WordStatus(7)
    data object Level4 : WordStatus(14)
    data object Learned : WordStatus(30)

    companion object {
        /** A side that has never been rated (null stability) reads as Level1. */
        fun fromStability(stability: Double?): WordStatus = when {
            stability == null || stability < 2 -> Level1
            stability < 7 -> Level2
            stability < 14 -> Level3
            stability < 30 -> Level4
            else -> Learned
        }
    }
}
