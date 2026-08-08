package com.example.android.memoization.notifications

import java.util.Calendar

/**
 * Works out when the next reminder is due. Times are wall-clock times in whatever
 * zone the incoming Calendar carries: the day is advanced first and the hour set
 * afterwards, so 12:00 stays 12:00 across a daylight saving change instead of
 * sliding by an hour.
 */
object ReminderSchedule {

    /**
     * @param times hour-to-minute pairs the user asked for
     * @param days [Calendar.DAY_OF_WEEK] values the reminder is allowed to land on
     * @param from the moment to search forward from, in the current timezone
     * @return epoch millis of the next reminder, or null if nothing is enabled
     */
    fun nextTriggerAt(
        times: List<Pair<Int, Int>>,
        days: Set<Int>,
        from: Calendar = Calendar.getInstance(),
    ): Long? {
        if (times.isEmpty() || days.isEmpty()) return null
        val sortedTimes = times.sortedWith(compareBy({ it.first }, { it.second }))

        // A full week ahead plus one, so a match is always found if any day is on.
        for (dayOffset in 0..7) {
            val day = (from.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            if (day.get(Calendar.DAY_OF_WEEK) !in days) continue
            for ((hour, minute) in sortedTimes) {
                val candidate = (day.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (candidate.timeInMillis > from.timeInMillis) return candidate.timeInMillis
            }
        }
        return null
    }
}
