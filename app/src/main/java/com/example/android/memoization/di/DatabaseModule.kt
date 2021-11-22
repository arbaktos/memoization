package com.example.android.memoization.di

import android.content.Context
import androidx.room.Room
import com.example.android.memoization.database.MemoDao
import com.example.android.memoization.database.MemoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideChannelDao(appDatabase: MemoDatabase): MemoDao {
        return appDatabase.memoDao
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): MemoDatabase {
        return Room.databaseBuilder(
            appContext,
            MemoDatabase::class.java,
            "memo_dataabase"
        ).build()
    }
}