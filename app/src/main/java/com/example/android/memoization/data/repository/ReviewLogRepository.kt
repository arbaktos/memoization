package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.logdb.ReviewLogEntity
import com.example.android.memoization.data.database.logdb.SessionEntity
import com.example.android.memoization.domain.session.MemorizationSession
import javax.inject.Inject

/** Writes the review log: one row per session, one per answer. Read side comes with the statistics. */
class ReviewLogRepository @Inject constructor(private val memoDao: MemoDao) {

    suspend fun startSession(stackId: Long, sidesOffered: Int, sidesWaiting: Int, now: Long): Long =
        memoDao.insertSession(
            SessionEntity(
                stackId = stackId,
                startedAt = now,
                sidesOffered = sidesOffered,
                sidesWaiting = sidesWaiting,
            )
        )

    suspend fun finishSession(sessionId: Long, now: Long) {
        memoDao.finishSession(sessionId, now)
    }

    suspend fun logAnswer(sessionId: Long, answer: MemorizationSession.Answer, shownMs: Long, now: Long) {
        memoDao.insertReview(
            ReviewLogEntity(
                sessionId = sessionId,
                sideId = answer.sideId,
                ratedAt = now,
                shownMs = shownMs,
                rating = answer.rating.grade,
                requeued = answer.requeued,
                stabilityAfter = answer.after.stability,
                difficultyAfter = answer.after.difficulty,
                // A requeued answer changes no schedule, so it has no "next due" of its own.
                dueAfter = if (answer.requeued) null else answer.after.due,
            )
        )
    }
}
