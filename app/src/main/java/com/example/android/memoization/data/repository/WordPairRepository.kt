package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.model.BaseWordPair
import com.example.android.memoization.data.model.WordPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordPairRepository @Inject constructor(private val memoDao: MemoDao) {

    /** Inserts the pair together with its two sides and returns the new pair's id. */
    suspend fun insertWordPair(wordPair: BaseWordPair): Long =
        memoDao.insertWordPairWithSides(WordPairEntity.create(wordPair))

    /** Deleting only hides the pair; its sides and their progress stay for an undo. */
    suspend fun setVisible(wordPairId: Long, visible: Boolean) {
        memoDao.setWordPairVisible(wordPairId, visible)
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