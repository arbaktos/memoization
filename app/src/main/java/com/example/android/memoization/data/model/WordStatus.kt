package com.example.android.memoization.data.model

/**
 * How well a word pair is known. Each level sets the gap, in whole calendar days, before the
 * pair is due again. Objects rather than classes so two references to the same level are equal.
 */
sealed class WordStatus(val frequency: Int) {
    data object Level1 : WordStatus(1)
    data object Level2 : WordStatus(2)
    data object Level3 : WordStatus(7)
    data object Level4 : WordStatus(14)
    data object Learned : WordStatus(30)

    /** One step up after a successful recall; Learned stays Learned. */
    fun next(): WordStatus = when (this) {
        Level1 -> Level2
        Level2 -> Level3
        Level3 -> Level4
        Level4, Learned -> Learned
    }
}
