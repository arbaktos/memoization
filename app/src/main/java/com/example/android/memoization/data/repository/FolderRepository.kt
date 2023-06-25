package com.example.android.memoization.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.android.memoization.data.database.*
import com.example.android.memoization.utils.TAG
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val memoDao: MemoDao
) {

    fun getFoldersWithStacks(): LiveData<List<FolderwithStacks>> {
        val folders = memoDao.getFoldersWithStacks()
        Log.d(TAG, "getFoldersWithStacks: ${folders.value}")
        return folders
    }

    suspend fun insertStack(stackEntity: StackEntity) {
        memoDao.insertStack(stackEntity)
    }

}