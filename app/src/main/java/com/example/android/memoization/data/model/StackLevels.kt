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

fun MemoStack.levels(
    practice: PracticeSides,
    now: Long = System.currentTimeMillis(),
    zone: TimeZone = TimeZone.getDefault(),
): StackLevels {
    var new = 0
    var learning = 0
    var known = 0
    var learned = 0
    words.forEach { pair ->
        val sides = pair.activeSides(practice, now, zone)
        when {
            sides.isEmpty() || sides.any { it.stability == null } -> new++
            else -> when (sides.minBy { it.level.frequency }.level) {
                WordStatus.Level1, WordStatus.Level2 -> learning++
                WordStatus.Level3, WordStatus.Level4 -> known++
                WordStatus.Learned -> learned++
            }
        }
    }
    return StackLevels(new, learning, known, learned)
}
