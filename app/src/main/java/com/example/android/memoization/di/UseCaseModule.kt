package com.example.android.memoization.di

import com.example.android.memoization.domain.usecases.GetFoldersWithStackFromDbUseCase
import com.example.android.memoization.domain.usecases.GetFoldersWithStackFromDbUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class UseCaseModule {

    @Binds
    abstract fun bingFoldersWithStackFromDbUseCase(useCaseImpl: GetFoldersWithStackFromDbUseCaseImpl):
            GetFoldersWithStackFromDbUseCase
}