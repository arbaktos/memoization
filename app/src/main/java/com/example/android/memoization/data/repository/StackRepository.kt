package com.example.android.memoization.data.repository

import android.util.Log
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.StackEntity
import com.example.android.memoization.data.database.StackWithWords
import com.example.android.memoization.utils.TAG
import javax.inject.Inject

class StackRepository @Inject constructor(private val memoDao: MemoDao){

    suspend fun getStacksWithWords(): List<StackWithWords> {
        return memoDao.getStacksWithWords()
    }

    suspend fun getStackWithWordsById(stackId: Long): StackWithWords {
        return memoDao.getStackWithWordsById(stackId)
    }

    suspend fun deleteStackFomDb(stackId: Long) {
        memoDao.deleteStackFomDb(stackId)
    }

    suspend fun updateStack(stackEntity: StackEntity) {
        memoDao.updateStack(stackEntity)
    }

}