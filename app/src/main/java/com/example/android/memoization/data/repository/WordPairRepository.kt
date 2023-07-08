package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.WordPairEntity
import com.example.android.memoization.data.model.WordPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordPairRepository @Inject constructor(private val memoDao: MemoDao) {

    suspend fun insertWordPair(wordPair: WordPair) {
        memoDao.insertWordPair(wordPair.toWordPairEntity())
    }

    suspend fun deleteWordPairFromDb(wordPairEntity: WordPairEntity) {
        memoDao.deleteWordPairFromDb(wordPairEntity)
    }

    suspend fun updateWordPairInDb(wordPair: WordPair) {
        memoDao.updateWordPair(wordPair.toWordPairEntity())
    }

    fun getWordPairById(wpId: Long): Flow<WordPair> {
        return memoDao.getWordPairByIdFlow(wpId)
            .map {
                it.toWordPair()
            }
    }
}