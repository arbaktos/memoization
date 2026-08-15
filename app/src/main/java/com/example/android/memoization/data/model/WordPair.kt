package com.example.android.memoization.data.model

import java.util.Date
import java.util.TimeZone

data class WordPair(
    override val parentStackId: Long,
    override var word1: String,
    override var word2: String?,
    override var lastRep: Date? = null,
    override var wordPairId: Long = 0,
    override var isVisible: Boolean = true,
    override var level: WordStatus = WordStatus.Level1
) : BaseWordPair, DismissableItem {

    /** Never rated yet - shown in the next session regardless of level. */
    val isNew: Boolean
        get() = lastRep == null

    fun isDue(
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault()
    ): Boolean = SpacedRepetition.isDue(level, lastRep?.time, now, zone)

    /** The pair as it should be persisted after one rating given at [now]. */
    fun rated(rating: Rating, now: Long): WordPair =
        copy(level = SpacedRepetition.rate(level, rating), lastRep = Date(now))
}
