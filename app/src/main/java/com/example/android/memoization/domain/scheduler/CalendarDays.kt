package com.example.android.memoization.domain.scheduler

import java.util.TimeZone

const val DAY_MILLIS = 86_400_000L

/** Calendar day number in [zone]; consecutive local midnights differ by exactly one. */
fun localDay(epochMillis: Long, zone: TimeZone): Long =
    Math.floorDiv(epochMillis + zone.getOffset(epochMillis), DAY_MILLIS)

/**
 * Calendar days between two instants in [zone], as the forgetting curve counts them: a review
 * at 16:00 answered again at 13:00 the next day is one day elapsed, not zero. Never negative.
 *
 * py-fsrs counts whole 24-hour spans instead, and so did this app until 2026-09-09. That
 * disagreed with due-ness, which has always been a calendar day: a side due "tomorrow" was
 * offered tomorrow, but if the sitting began a few minutes earlier than yesterday's the
 * scheduler took the answer for a same-day review - no growth on Hard, a few percent on Good -
 * and the side came back the day after, again. Anki counts scheduler days, as this does now.
 */
fun elapsedDays(from: Long, to: Long, zone: TimeZone): Long =
    maxOf(0L, localDay(to, zone) - localDay(from, zone))
