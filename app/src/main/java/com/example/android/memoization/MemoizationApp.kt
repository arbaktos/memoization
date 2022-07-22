package com.example.android.memoization

import android.app.Application
import androidx.startup.AppInitializer
import com.example.android.memoization.utils.workers.CustomWorkManagerInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MemoizationApp @Inject constructor(): Application()  {

    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).initializeComponent(CustomWorkManagerInitializer::class.java)
    }
}

