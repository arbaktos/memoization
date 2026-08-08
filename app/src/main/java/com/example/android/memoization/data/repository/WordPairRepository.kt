package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.model.BaseWordPair
import com.example.android.memoization.data.model.WordPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordPairRepository @Inject constructor(private val memoDao: MemoDao) {

    suspend fun insertWordPair(wordPair: BaseWordPair) {
        memoDao.insertWordPair(WordPairEntity.create(wordPair))
    }

    suspend fun deleteWordPairFromDb(wordPair: BaseWordPair) {
        memoDao.deleteWordPairFromDb(WordPairEntity.create(wordPair))
    }

    suspend fun updateWordPairInDb(wordPair: BaseWordPair) {
        memoDao.updateWordPair(WordPairEntity.create(wordPair))
    }

    fun getWordPairById(wpId: Long): Flow<BaseWordPair> {
        return memoDao.getWordPairByIdFlow(wpId)
            .map {
                it.toWordPair()
            }
    }
}