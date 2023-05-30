package com.example.android.memoization.utils.workers

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
//
//@Module
//@InstallIn(SingletonComponent::class)
//class CustomWorkManagerInitializer: Initializer<WorkManager> {
//
//    @Provides
//    @Singleton
//    override fun create(@ApplicationContext context: Context): WorkManager {
//        val workerFactory = getWorkerFactory(appContext = context)
//        val config = Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//        WorkManager.initialize(context, config)
//        return WorkManager.getInstance(context)
//    }
//
//    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
//
//    private fun getWorkerFactory(appContext: Context): HiltWorkerFactory {
//        val workManagerEntryPoint = EntryPointAccessors.fromApplication(
//            appContext,
//            WorkManagerInitializerEntryPoint::class.java
//        )
//        return workManagerEntryPoint.hiltWorkerFactory()
//    }
//
//    @InstallIn(SingletonComponent::class)
////    @Module
//    @EntryPoint
//    interface WorkManagerInitializerEntryPoint {
//        fun hiltWorkerFactory(): HiltWorkerFactory
//    }
//}