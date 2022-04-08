package com.example.android.memoization.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android.memoization.database.MemoDatabase
import com.example.android.memoization.ui.composables.screens.TDEBUG
import com.example.android.memoization.utils.STACK_ID

class StackDeletionWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
    private val _context = context.applicationContext
    private val memoDao = MemoDatabase.getInstance(_context).memoDao

    override suspend fun doWork(): Result {
        return try {
            val stackId = inputData.getLong(STACK_ID, 0)
            memoDao.deleteStackFomDb(stackId)
            Log.d(TDEBUG, "deleted $stackId")
            Result.success()
        } catch(throwable: Throwable) {
            throwable.printStackTrace()
            Result.failure()
        }
    }
}