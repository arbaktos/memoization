package com.example.android.memoization.data.model

import java.util.TimeZone

/**
 * A stack's pairs by how well they are known, in the four steps the stack list shows: [new]
 * (never answered), [learning] (Level1-2), [known] (Level3-4) and [learned]. A pair is placed
 * by its weakest practised side, as [WordPair.level] does, so a pair whose weakest side was
 * never answered is still new.
 */
data class StackLevels(val new: Int, val learning: Int, val known: Int, val learned: Int) {
    val counts: List<Int> get() = listOf(new, learning, known, learned)
}

/**
 * Where a side stands on the four-step scale: 0 new (never answered), 1 learning (Level1-2),
 * 2 known (Level3-4), 3 learned.
 */
val Side.step: Int
    get() = when {
        stability == null -> 0
        else -> when (level) {
            WordStatus.Level1, WordStatus.Level2 -> 1
            WordStatus.Level3, WordStatus.Level4 -> 2
            WordStatus.Learned -> 3
        }
    }

/** True when this setting ever asks for sides shown that way, today or after a hand-over. */
fun PracticeSides.covers(shown: Shown): Boolean = when (this) {
    PracticeSides.WORD_TO_MEANING -> shown == Shown.WORD
    PracticeSides.MEANING_TO_WORD -> shown == Shown.MEANING
    PracticeSides.SMART_SWITCH, PracticeSides.BOTH -> true
}

fun MemoStack.levels(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
): StackLevels {
    val counts = IntArray(4)
    words.forEach { pair ->
        // The weakest practised side places the pair; one never answered makes it new.
        counts[pair.activeSides(practice, now, zone).minOfOrNull { it.step } ?: 0]++
    }
    return StackLevels(counts[0], counts[1], counts[2], counts[3])
}
