package com.example.android.memoization.di

import com.example.android.memoization.domain.usecases.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class UseCaseModule {

    @Binds
    abstract fun bindFoldersWithStackFromDbUseCase(useCaseImpl: GetFoldersWithStackUseCaseImpl):
            GetFoldersWithStackUseCase

    @Binds
    abstract fun bindGetStackUseCase(useCaseImpl: GetStackUseCaseImpl):
            GetStackUseCase

    @Binds
    abstract fun bindDeleteWordPairUseCase(useCaseImpl: DeleteWordPairUseCaseImpl):
            DeleteWordPairUseCase

    @Binds
    abstract fun bindDeleteStackUseCase(useCaseImpl: DeleteStackUseCaseImpl):
            DeleteStackUseCase


}