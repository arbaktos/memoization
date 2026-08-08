package com.example.android.memoization.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.domain.usecases.DeleteStackUseCase
import javax.inject.Inject

class MemoWorkerFactory @Inject constructor(
    private val deleteStackUseCase: DeleteStackUseCase,
    val memoDao: MemoDao
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            StackDeletionWorker::class.java.name -> StackDeletionWorker(
                appContext,
                deleteStackUseCase,
                workerParameters
            )
            WordPairInvisibleWorker::class.java.name -> WordPairInvisibleWorker(
                appContext,
                memoDao,
                workerParameters
            )
            else -> {
                Log.e("createWorker", "No such worker found")
                null
            }
        }
    }
}