package com.example.android.memoization.data.model

/**
 * Which sides of every pair the learner practises. A pair always has both sides in the
 * database; this only decides which of them are scheduled, counted and shown.
 */
enum class PracticeSides {
    /**
     * Word to meaning first; the meaning side joins once the word side is known well enough -
     * Level3, a memory holding a week - and stays in from then on, so a lapse of the word side
     * never hides progress already made on the meaning side.
     */
    SMART_SWITCH,
    WORD_TO_MEANING,
    MEANING_TO_WORD,
    BOTH;

    /** The sides of one pair this setting schedules, in the order given. */
    fun practised(sides: List<Side>): List<Side> = when (this) {
        WORD_TO_MEANING -> sides.filter { it.shown == Shown.WORD }
        MEANING_TO_WORD -> sides.filter { it.shown == Shown.MEANING }
        BOTH -> sides
        SMART_SWITCH -> {
            val wordKnown = sides.firstOrNull { it.shown == Shown.WORD }?.let { isKnown(it) } == true
            sides.filter { it.shown == Shown.WORD || wordKnown || !it.isNew }
        }
    }

    companion object {
        val DEFAULT = SMART_SWITCH

        /** The level the word side must reach before Smart switch adds the meaning side. */
        val UNLOCK_LEVEL: WordStatus = WordStatus.Level3

        private fun isKnown(side: Side): Boolean = side.level.frequency >= UNLOCK_LEVEL.frequency

        /** Stored as the enum name; anything unknown falls back to the default. */
        fun fromName(name: String?): PracticeSides =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
