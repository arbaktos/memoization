package com.example.android.memoization.data.model

/**
 * Which sides of every pair the learner practises. A pair always has both sides in the
 * database; this only decides which of them are scheduled, counted and shown.
 */
enum class PracticeSides {
    /**
     * One side at a time, word to meaning first. Once the word side is known well enough -
     * Level3, a memory holding a week - the pair hands over to its meaning side: the word is
     * no longer asked for, the learner is shown the meaning and recalls the word. The hand-over
     * holds from then on, so a lapse of the word side never takes back a meaning side that has
     * already been rated.
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
            val word = sides.firstOrNull { it.shown == Shown.WORD }
            val meaning = sides.firstOrNull { it.shown == Shown.MEANING }
            // Handed over once the word side is known, or once the meaning side has been rated;
            // a pair that has no word side at all starts on its meaning side.
            val handedOver = meaning != null && (word == null || isKnown(word) || !meaning.isNew)
            listOfNotNull(if (handedOver) meaning else word ?: meaning)
        }
    }

    companion object {
        val DEFAULT = SMART_SWITCH

        /** The level the word side must reach before Smart switch hands over to the meaning side. */
        val HANDOVER_LEVEL: WordStatus = WordStatus.Level3

        private fun isKnown(side: Side): Boolean = side.level.frequency >= HANDOVER_LEVEL.frequency

        /** Stored as the enum name; anything unknown falls back to the default. */
        fun fromName(name: String?): PracticeSides =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
