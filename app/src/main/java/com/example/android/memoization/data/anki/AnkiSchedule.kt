package com.example.android.memoization.data.anki

import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState

/**
 * How an Anki card's progress becomes a side's. With FSRS on in Anki the card carries the very
 * stability and difficulty this app schedules by, and they are taken as they are. Without it the
 * card only has SM-2's interval and ease: the interval is about as long as the memory holds at
 * nine in ten, which is what stability means, and the ease reads as difficulty - 250 %, Anki's
 * starting ease, the middle of the scale; its floor of 130 % the hardest. A card still on its
 * learning steps with no FSRS memory has nothing to carry over yet and starts New.
 */
object AnkiSchedule {

    private const val DAY_MILLIS = 86_400_000L
    private const val DEFAULT_EASE = 2500
    private const val EASE_PER_DIFFICULTY_STEP = 240.0
    private const val MID_DIFFICULTY = 5.0

    /** Due values above this are moments in seconds (learning today), below it day numbers. */
    private const val SECONDS_NOT_DAYS = 1_000_000_000L

    /** Template 0 asks for the back from the front, template 1 the reverse. */
    fun shownOf(ord: Int): Shown? = when (ord) {
        0 -> Shown.WORD
        1 -> Shown.MEANING
        else -> null
    }

    fun side(
        card: AnkiCard,
        wordPairId: Long,
        shown: Shown,
        collectionCreatedSeconds: Long,
        lastReviewMillis: Long?,
        now: Long,
    ): Side {
        val memory = AnkiFormat.fsrsMemory(card.data)
        val stability = memory?.first ?: when (card.type) {
            TYPE_REVIEW, TYPE_RELEARNING -> card.ivl.coerceAtLeast(1).toDouble()
            else -> null
        }
        if (card.type == TYPE_NEW || stability == null) return Side(wordPairId = wordPairId, shown = shown)

        val difficulty = memory?.second ?: difficultyOf(card.factor)
        val due = dueMillis(card.homeDue, collectionCreatedSeconds) ?: now
        val lastReview = lastReviewMillis
            ?: (due - card.ivl.coerceAtLeast(1) * DAY_MILLIS).coerceAtMost(now)
        return Side(
            wordPairId = wordPairId,
            shown = shown,
            state = SideState.Review,
            stability = stability,
            difficulty = difficulty.coerceIn(1.0, 10.0),
            due = due,
            lastReview = lastReview,
            reps = card.reps,
            lapses = card.lapses,
        )
    }

    fun difficultyOf(factor: Int): Double {
        val ease = if (factor <= 0) DEFAULT_EASE else factor
        return (MID_DIFFICULTY + (DEFAULT_EASE - ease) / EASE_PER_DIFFICULTY_STEP).coerceIn(1.0, 10.0)
    }

    fun dueMillis(due: Long, collectionCreatedSeconds: Long): Long? = when {
        due <= 0 && collectionCreatedSeconds <= 0 -> null
        due > SECONDS_NOT_DAYS -> due * 1000
        else -> (collectionCreatedSeconds + due * 86_400L) * 1000
    }

    const val TYPE_NEW = 0
    const val TYPE_LEARNING = 1
    const val TYPE_REVIEW = 2
    const val TYPE_RELEARNING = 3
}
