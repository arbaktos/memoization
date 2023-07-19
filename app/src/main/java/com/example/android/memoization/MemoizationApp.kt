package com.example.android.memoization

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.example.android.memoization.utils.workers.MemoWorkerFactory
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

