package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.sidedb.SideEntity
import com.example.android.memoization.data.model.Side
import javax.inject.Inject

class SideRepository @Inject constructor(private val memoDao: MemoDao) {

    suspend fun updateSide(side: Side) {
        memoDao.updateSide(SideEntity.from(side))
    }

    /** Resets a stack's sides to New and returns them as they were. */
    suspend fun resetStack(stackId: Long): List<Side> =
        memoDao.resetStackSides(stackId).map { it.toSide() }

    /** Puts sides back as they were, after a reset taken back. */
    suspend fun restore(sides: List<Side>) {
        memoDao.updateSides(sides.map { SideEntity.from(it) })
    }

    suspend fun sidesForPair(wordPairId: Long): List<Side> =
        memoDao.getSidesForPair(wordPairId).map { it.toSide() }
}
