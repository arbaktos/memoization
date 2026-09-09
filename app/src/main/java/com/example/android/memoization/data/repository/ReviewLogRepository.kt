package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.logdb.ReviewLogEntity
import com.example.android.memoization.data.database.logdb.SessionEntity
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.domain.session.SessionEnding
import javax.inject.Inject

/** Writes the review log: one row per session, one per answer. Read side comes with the statistics. */
class ReviewLogRepository @Inject constructor(private val memoDao: MemoDao) {

    suspend fun startSession(stackId: Long, sidesOffered: Int, now: Long): Long =
        memoDao.insertSession(
            SessionEntity(
                stackId = stackId,
                startedAt = now,
                sidesOffered = sidesOffered,
            )
        )

    /** The session is over, by [ending], having got as far as [state] says. */
    suspend fun finishSession(sessionId: Long, ending: SessionEnding, state: MemorizationSession.State, now: Long) {
        memoDao.finishSession(sessionId, now, ending.code, sidesDone = state.done, sidesWaiting = state.left)
    }

    /** When each stack was last practised, for ordering the stacks a word can be shared into. */
    suspend fun lastSessionPerStack(): Map<Long, Long> =
        memoDao.getLastSessionPerStack().associate { it.stackId to it.lastStartedAt }

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
