package com.example.android.memoization.data.repository

import android.util.Log
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.stackdb.StackWithWords
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.model.BaseStack
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StackRepository @Inject constructor(private val memoDao: MemoDao){

    private val TAG = "StackRepository"
    fun getStacksWithWords(): Flow<List<StackWithWords>> {
        return memoDao.getStacksWithWords()
    }

    fun getStackWithWordsById(stackId: Long): Flow<StackWithWords> {
        return memoDao.getStackWithWordsById(stackId)
    }

    suspend fun deleteStackFomDb(stackId: Long) {
        Log.d(TAG, "deleteStackFomDb: ")
        memoDao.deleteStackFomDb(stackId)
    }

    suspend fun updateStack(stackEntity: StackEntity) {
        memoDao.updateStack(stackEntity)
    }

    suspend fun insertStack(baseStack: StackEntity) {
        memoDao.insertStack(baseStack)
    }

}