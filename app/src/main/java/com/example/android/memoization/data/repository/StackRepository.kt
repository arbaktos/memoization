package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.StackEntity
import com.example.android.memoization.data.database.StackWithWords
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StackRepository @Inject constructor(private val memoDao: MemoDao){

    fun getStacksWithWords(): Flow<List<StackWithWords>> {
        return memoDao.getStacksWithWords()
    }

    fun getStackWithWordsById(stackId: Long): Flow<StackWithWords> {
        return memoDao.getStackWithWordsById(stackId)
    }

    suspend fun deleteStackFomDb(stackId: Long) {
        memoDao.deleteStackFomDb(stackId)
    }

    suspend fun updateStack(stackEntity: StackEntity) {
        memoDao.updateStack(stackEntity)
    }

}