package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.sidedb.SideEntity
import com.example.android.memoization.data.model.Side
import javax.inject.Inject

class SideRepository @Inject constructor(private val memoDao: MemoDao) {

    suspend fun updateSide(side: Side) {
        memoDao.updateSide(SideEntity.from(side))
    }

    suspend fun sidesForPair(wordPairId: Long): List<Side> =
        memoDao.getSidesForPair(wordPairId).map { it.toSide() }
}
