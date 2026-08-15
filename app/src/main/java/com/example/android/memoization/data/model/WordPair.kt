package com.example.android.memoization.data.model

import java.util.TimeZone

/**
 * A word and its meaning. The pair itself carries no schedule - each of its [sides] is
 * scheduled on its own, because recognising a word and producing it are separate skills.
 */
data class WordPair(
    override val parentStackId: Long,
    override var word1: String,
    override var word2: String?,
    override var wordPairId: Long = 0,
    override var isVisible: Boolean = true,
    val sides: List<Side> = emptyList(),
) : BaseWordPair, DismissableItem {

    fun activeSides(practice: PracticeSides): List<Side> =
        sides.filter { practice.includes(it.shown) }

    fun dueSides(
        practice: PracticeSides,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault(),
    ): List<Side> = activeSides(practice).filter { it.isDue(now, zone) }

    fun isDue(
        practice: PracticeSides,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault(),
    ): Boolean = dueSides(practice, now, zone).isNotEmpty()

    /** A pair is only as known as its weakest practised side. */
    fun level(practice: PracticeSides): WordStatus =
        activeSides(practice).minByOrNull { it.level.frequency }?.level ?: WordStatus.Level1

    fun front(shown: Shown): String = if (shown == Shown.WORD) word1 else word2.orEmpty()

    fun back(shown: Shown): String = if (shown == Shown.WORD) word2.orEmpty() else word1
}
