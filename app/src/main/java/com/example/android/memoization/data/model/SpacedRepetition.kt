package com.example.android.memoization.data.model

import java.util.TimeZone

/** The three answers the learner can give to a card. */
enum class Rating { Again, Hard, Good }

/**
 * The scheduling rules, kept free of Android and of the database so they can be tested with a
 * clock and a time zone of the test's choosing.
 */
object SpacedRepetition {
    private const val DAY_MILLIS = 86_400_000L

    /** Calendar day number in [zone]; consecutive local midnights differ by exactly one. */
    fun localDay(epochMillis: Long, zone: TimeZone): Long =
        Math.floorDiv(epochMillis + zone.getOffset(epochMillis), DAY_MILLIS)

    /**
     * A pair that has never been repeated ([lastRep] == null) is New and due at once. Otherwise
     * it is due once at least the level's frequency in whole local calendar days has passed - so
     * a Level1 pair repeated at 23:59 is due again a minute later, and one repeated at 00:01 is
     * not due until tomorrow.
     */
    fun isDue(level: WordStatus, lastRep: Long?, now: Long, zone: TimeZone): Boolean =
        lastRep == null || localDay(now, zone) - localDay(lastRep, zone) >= level.frequency

    /** Again resets to Level1, Hard keeps the level (same gap again), Good moves one step up. */
    fun rate(level: WordStatus, rating: Rating): WordStatus = when (rating) {
        Rating.Again -> WordStatus.Level1
        Rating.Hard -> level
        Rating.Good -> level.next()
    }
}
