package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.localDay
import java.util.TimeZone

/** Which half of the pair the learner is shown; the other half is what they have to recall. */
enum class Shown(val code: Int) {
    WORD(0),
    MEANING(1);

    companion object {
        fun fromCode(code: Int): Shown = entries.firstOrNull { it.code == code } ?: WORD
    }
}

enum class SideState(val code: Int) {
    New(0),
    Review(1);

    companion object {
        fun fromCode(code: Int): SideState = entries.firstOrNull { it.code == code } ?: New
    }
}

/**
 * One direction of recall of a word pair, with its own schedule: knowing the meaning of a word
 * and being able to produce the word are different skills, so they are tracked apart.
 */
data class Side(
    val sideId: Long = 0,
    val wordPairId: Long,
    val shown: Shown,
    val state: SideState = SideState.New,
    /** Days the memory holds; null until the side has been rated once. */
    val stability: Double? = null,
    /** How hard this side is for this learner, 1..10; null until rated. */
    val difficulty: Double? = null,
    val due: Long? = null,
    val lastReview: Long? = null,
    val reps: Int = 0,
    val lapses: Int = 0,
) {
    val isNew: Boolean get() = state == SideState.New

    /** New sides are due at once; a scheduled side is due on its due date's calendar day. */
    fun isDue(now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Boolean =
        due == null || localDay(due, zone) <= localDay(now, zone)

    val level: WordStatus get() = WordStatus.fromStability(stability)
}
