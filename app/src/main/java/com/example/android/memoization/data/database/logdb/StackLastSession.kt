package com.example.android.memoization.data.database.logdb

/** When a stack was last sat down with; the read side of the review log needs no more than this. */
data class StackLastSession(
    val stackId: Long,
    val lastStartedAt: Long,
)
