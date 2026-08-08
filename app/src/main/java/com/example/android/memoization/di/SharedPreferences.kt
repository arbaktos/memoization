package com.example.android.memoization.di

import android.content.Context
import android.content.SharedPreferences
import com.example.android.memoization.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class SharedPreferences @Inject constructor() {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }
}