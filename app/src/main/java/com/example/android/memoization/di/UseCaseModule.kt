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
    abstract fun bindGetStackUseCase(useCaseImpl: GetStackUseCaseImpl):
            GetStackUseCase

    @Binds
    abstract fun bindDeleteWordPairUseCase(useCaseImpl: DeleteWordPairUseCaseImpl):
            DeleteWordPairUseCase

    @Binds
    abstract fun bindDeleteStackUseCase(useCaseImpl: DeleteStackUseCaseImpl):
            DeleteStackUseCase

    @Binds
    abstract fun bindGetStacksUseCase(useCaseImpl: GetStacksWithWordsUseCaseImpl):
            GetStacksWithWordsUseCase

    @Binds
    abstract fun bindUpdateStackUseCase(impl: UpdateStackUseCaseImpl): UpdateStackUseCase

    @Binds
    abstract fun bindGetWorPairUseCase(impl: GetWorPairLoadingStateUseCaseImpl): GetWordPairLoadingStateUseCase

    @Binds
    abstract fun bindNotifThresholdUseCase(impl: NotifThresholdCalcUseCaseImpl): NotifThresholdCalcUseCase

}