package com.example.android.memoization.utils.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.utils.WP_ID
import javax.inject.Inject

class WordPairInvisibleWorker @Inject constructor(
    context: Context, val memoDao: MemoDao, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val wpId = inputData.getLong(WP_ID, 0)
            val wordPairEntity = memoDao.findWordPairById(wpId)
            memoDao.updateWordPair(wordPairEntity.copy(isVisible = false))
            Result.success()
        } catch(throwable: Throwable) {
            throwable.printStackTrace()
            Result.failure()
        }
    }
}