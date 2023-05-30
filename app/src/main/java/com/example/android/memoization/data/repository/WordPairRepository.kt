package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.WordPairEntity
import com.example.android.memoization.domain.model.WordPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun getWordPairById(wpId: Long): Flow<WordPair> {
        return memoDao.getWordPairByIdFlow(wpId)
            .map {
                it.toWordPair()
            }
    }
}