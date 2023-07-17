package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.*
import com.example.android.memoization.data.database.stackdb.StackEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val memoDao: MemoDao
) {

//    fun getFoldersWithStacks(): LiveData<List<FolderwithStacks>> {
//        val folders = memoDao.getFoldersWithStacks()
//        Log.d(TAG, "getFoldersWithStacks: ${folders.value}")
//        return folders
//    }

    suspend fun insertStack(stackEntity: StackEntity) {
        memoDao.insertStack(stackEntity)
    }

}