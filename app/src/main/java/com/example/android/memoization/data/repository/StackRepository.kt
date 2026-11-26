package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.stackdb.StackWithWords
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StackRepository @Inject constructor(private val memoDao: MemoDao){

    fun getStacksWithWords(): Flow<List<StackWithWords>> {
        return memoDao.getStacksWithWords()
    }

    fun getStackWithWordsById(stackId: Long): Flow<StackWithWords> {
        return memoDao.getStackWithWordsById(stackId)
    }

    suspend fun updateStack(stackEntity: StackEntity) {
        memoDao.updateStack(stackEntity)
    }

    /** Deleting hides the stack and notes when; restoring brings it back as it was. */
    suspend fun setStackDeleted(stackId: Long, deleted: Boolean) {
        memoDao.setStackDeleted(stackId, if (deleted) System.currentTimeMillis() else null)
    }

    /** The learner stopped a session with this stack after [answers] answers; see Stamina. */
    suspend fun recordStoppedAt(stackId: Long, answers: Int) {
        memoDao.recordStoppedAt(stackId, answers)
    }

    suspend fun insertStack(baseStack: StackEntity): Long {
        return memoDao.insertStack(baseStack)
    }

}