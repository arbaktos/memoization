package com.example.android.memoization.domain.scheduler

import java.util.TimeZone

const val DAY_MILLIS = 86_400_000L

/** Calendar day number in [zone]; consecutive local midnights differ by exactly one. */
fun localDay(epochMillis: Long, zone: TimeZone): Long =
    Math.floorDiv(epochMillis + zone.getOffset(epochMillis), DAY_MILLIS)

/**
 * Whole days between two instants, as FSRS counts them for the forgetting curve - a review
 * nine hours after the last one is zero days elapsed, not one. Never negative.
 */
fun elapsedDays(from: Long, to: Long): Long = maxOf(0L, Math.floorDiv(to - from, DAY_MILLIS))
