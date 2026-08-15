package com.example.android.memoization.data.model

/**
 * Which sides of every pair the learner practises. A pair always has both sides in the
 * database; this only decides which of them are scheduled, counted and shown.
 */
enum class PracticeSides(val shown: Set<Shown>) {
    WORD_TO_MEANING(setOf(Shown.WORD)),
    MEANING_TO_WORD(setOf(Shown.MEANING)),
    BOTH(setOf(Shown.WORD, Shown.MEANING));

    fun includes(side: Shown): Boolean = side in shown

    companion object {
        val DEFAULT = WORD_TO_MEANING

        /** Stored as the enum name; anything unknown falls back to the default. */
        fun fromName(name: String?): PracticeSides =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
