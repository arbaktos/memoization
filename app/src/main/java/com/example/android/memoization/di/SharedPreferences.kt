package com.example.android.memoization.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

//@Binds
//fun bindWorkManager(impl: )

//@InstallIn(SingletonComponent::class)
//@Module
//class WorkManager @Inject constructor() {
//
//    @Provides
//    @Singleton
//    fun workManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)
//}