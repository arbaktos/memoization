package com.example.android.memoization.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.android.memoization.data.api.ApiLanguage
import com.example.android.memoization.data.api.Retrofit
import com.example.android.memoization.data.api.WordTranslationRequest
import com.example.android.memoization.data.api.WordTranslationResponse
import com.example.android.memoization.data.database.*
import com.example.android.memoization.utils.TAG
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val memoDao: MemoDao, private val retrofit: Retrofit
) {
    //folders
    suspend fun insertFolder(folderEntity: FolderEntity): Long {
        return memoDao.insertFolder(folderEntity)
    }

    fun getFoldersWithStacks(): LiveData<List<FolderwithStacks>> {
        val folders = memoDao.getFoldersWithStacks()
        Log.d(TAG, "getFoldersWithStacks: ${folders.value}")
        return folders
    }

    suspend fun getFoldersWithStacks2(): List<FolderwithStacks> {
        val folders = memoDao.getFoldersWithStacks2()
        Log.d(TAG, "getFoldersWithStacks2: ${folders}")
        return folders
    }

    suspend fun getTranslation(request: WordTranslationRequest): Response<WordTranslationResponse> {
        return retrofit.linganexApi.getTranslation(request)
    }

    suspend fun deleteFolderFromDb(folderEntity: FolderEntity) {
        memoDao.deleteFolderFromDb(folderEntity)
    }

    //stacks
    suspend fun insertStack(stackEntity: StackEntity) {
        memoDao.insertStack(stackEntity)
    }

//    suspend fun getStacksWithWords(): List<StackWithWords> {
//        return memoDao.getStacksWithWords()
//    }
//
//    suspend fun getStackWithWordsById(stackId: Long): StackWithWords {
//        return memoDao.getStackWithWordsById(stackId)
//    }
//
//    suspend fun deleteStackFomDb(stackId: Long) {
//        val a = memoDao.deleteStackFomDb(stackId)
//    }
//
//    suspend fun updateStack(stackEntity: StackEntity) {
//        memoDao.updateStack(stackEntity)
//    }

    //wordPairs
    suspend fun insertWordPair(wordPairEntity: WordPairEntity) {
        memoDao.insertWordPair(wordPairEntity)
    }

    suspend fun deleteWordPairFromDb(wordPairEntity: WordPairEntity) {
        memoDao.deleteWordPairFromDb(wordPairEntity)
    }

    suspend fun updateWordPairInDb(wordPairEntity: WordPairEntity) {
        memoDao.updateWordPair(wordPairEntity)
    }

    suspend fun getWordsFromStack(stackId: Long): List<WordPairEntity> {
        return memoDao.getWordsFromStack(stackId)
    }

    suspend fun findWordPairInDb(id:Long): WordPairEntity {
        return memoDao.findWordPairById(id)
    }

    //languages
    suspend fun getLanguages(): Response<ApiLanguage> {
        return retrofit.linganexApi.getLanguages()
    }

    //not needed?
    suspend fun getSingleFolderWithStacks(folderId: Long): FolderwithStacks {
        return memoDao.getSingleFolderWithStacks(folderId)
    }

    suspend fun getFoldersEntities(): List<FolderEntity> {
        return memoDao.getFolders()
    }
}