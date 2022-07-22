package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.WordPairEntity
import javax.inject.Inject

class WordPairRepository @Inject constructor(private val memoDao: MemoDao) {

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
}