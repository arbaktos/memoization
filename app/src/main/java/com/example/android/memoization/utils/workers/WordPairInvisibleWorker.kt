package com.example.android.memoization.utils.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.memoization.database.MemoDatabase
import com.example.android.memoization.utils.WP_ID

class WordPairInvisibleWorker (context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val _context = context
    private val memoDao = MemoDatabase.getInstance(_context).memoDao

    override suspend fun doWork(): Result {
        return try {
            val wpId = inputData.getLong(WP_ID, 0)
            val wordPairEntity = memoDao.findWordPairById(wpId)
            wordPairEntity.isVisible = false
            memoDao.updateWordPair(wordPairEntity)
            Result.success()
        } catch(throwable: Throwable) {
            throwable.printStackTrace()
            Result.failure()
        }
    }
}