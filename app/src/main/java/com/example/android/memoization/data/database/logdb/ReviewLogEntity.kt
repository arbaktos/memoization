package com.example.android.memoization.data.database.logdb

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames

/**
 * One answer given in a session - every tap, including the answer to a side brought back
 * after Forgot, which the scheduler ignores but the learner did give.
 *
 * The side's schedule *after* the answer is kept here because the side row only holds the
 * latest: the growth curve, time-to-Learned and the streak's obligation days all need the
 * value as it was at the time. Nothing here is ever deleted.
 */
@Entity(
    tableName = TableNames.REVIEW_LOG_TABLE,
    indices = [Index("sessionId"), Index("sideId"), Index("ratedAt")],
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Long = 0,
    val sessionId: Long,
    val sideId: Long,
    val ratedAt: Long,
    /** Card shown to tap, raw milliseconds; any cap is applied when summing, not here. */
    val shownMs: Long,
    /** FSRS grade: 1 Again (Forgot), 2 Hard, 3 Good. */
    val rating: Int,
    /** True for the answer to a side that came round again after Forgot in this session. */
    val requeued: Boolean,
    val stabilityAfter: Double?,
    val difficultyAfter: Double?,
    /** Next due after this answer; null when the answer changed no schedule. */
    val dueAfter: Long?,
)
