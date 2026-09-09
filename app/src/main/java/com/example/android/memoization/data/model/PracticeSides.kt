package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.localDay
import java.util.TimeZone

/**
 * Which sides of every pair the learner practises. A pair always has both sides in the
 * database; this only decides which of them are scheduled, counted and shown.
 */
enum class PracticeSides {
    /**
     * One side at a time, word to meaning first. Once the word side is known well enough -
     * Level3, a memory holding a week - the pair hands over to its meaning side: the word is
     * no longer asked for, the learner is shown the meaning and recalls the word. The hand-over
     * takes effect the day after the word side got there, so a word settled in this sitting is
     * not asked for from the other side in the same sitting, and what a session leaves waiting
     * is what the stack list shows afterwards. It holds from then on, so a lapse of the word
     * side never takes back a meaning side that has already been rated.
     */
    SMART_SWITCH,
    WORD_TO_MEANING,
    MEANING_TO_WORD,
    BOTH;

    /** The sides of one pair this setting schedules on the day of [now], in the order given. */
    fun practised(
        sides: List<Side>,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault(),
    ): List<Side> = when (this) {
        WORD_TO_MEANING -> sides.filter { it.shown == Shown.WORD }
        MEANING_TO_WORD -> sides.filter { it.shown == Shown.MEANING }
        BOTH -> sides
        SMART_SWITCH -> {
            val word = sides.firstOrNull { it.shown == Shown.WORD }
            val meaning = sides.firstOrNull { it.shown == Shown.MEANING }
            // Handed over once the word side is known, or once the meaning side has been rated;
            // a pair that has no word side at all starts on its meaning side.
            val handedOver = meaning != null && (word == null || isKnown(word, now, zone) || !meaning.isNew)
            listOfNotNull(if (handedOver) meaning else word ?: meaning)
        }
    }

    companion object {
        val DEFAULT = SMART_SWITCH

        /** The level the word side must reach before Smart switch hands over to the meaning side. */
        val HANDOVER_LEVEL: WordStatus = WordStatus.Level3

        /** At the hand-over level, and rated there before today: the switch waits for the next day. */
        private fun isKnown(side: Side, now: Long, zone: TimeZone): Boolean =
            side.level.frequency >= HANDOVER_LEVEL.frequency &&
                (side.lastReview == null || localDay(side.lastReview, zone) < localDay(now, zone))

        /** Stored as the enum name; anything unknown falls back to the default. */
        fun fromName(name: String?): PracticeSides =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
