package com.example.android.memoization

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.domain.usecases.DeleteStackUseCase
import com.example.android.memoization.utils.workers.StackDeletionWorker
import com.example.android.memoization.utils.workers.WordPairInvisibleWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MemoizationApp @Inject constructor() : Application(), Configuration.Provider {

    @Inject
    lateinit var memoWorkerFactory: MemoWorkerFactory
    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(memoWorkerFactory)
            .build()
}

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

