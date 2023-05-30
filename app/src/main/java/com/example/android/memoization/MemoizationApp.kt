package com.example.android.memoization

import android.app.Application
import androidx.startup.AppInitializer
import androidx.work.WorkManager
//import com.example.android.memoization.utils.workers.CustomWorkManagerInitializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Inject
import javax.inject.Singleton

//@Module
//@InstallIn(ActivityComponent::class)
@HiltAndroidApp
class MemoizationApp @Inject constructor(): Application()  {

    override fun onCreate() {
        super.onCreate()
//        AppInitializer.getInstance(this).initializeComponent(CustomWorkManagerInitializer::class.java)
    }

//    @Provides
//    fun getWorkManager() =  WorkManager.getInstance(this)
}

