package com.example.android.memoization.di

import android.content.Context
import androidx.room.Room
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.MemoDatabase
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
            DATABASE_NAME
        )
            // Learning progress is irreplaceable: never add fallbackToDestructiveMigration
            // here. A schema change must ship a Migration in MemoDatabase.MIGRATIONS -
            // without one Room refuses to open the db rather than wiping it.
            .addMigrations(*MemoDatabase.MIGRATIONS)
            .build()
    }

    companion object {
        const val DATABASE_NAME = "memo_dataabase"
    }
}