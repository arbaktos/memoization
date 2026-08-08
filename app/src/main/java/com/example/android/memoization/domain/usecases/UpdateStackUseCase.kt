package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.StackRepository
import javax.inject.Inject

interface UpdateStackUseCase {
    suspend operator fun invoke(stack: MemoStack)
}

class UpdateStackUseCaseImpl @Inject constructor(val repo: StackRepository): UpdateStackUseCase {

    override suspend fun invoke(stack: MemoStack) {
        // Via create() so every field travels - listing them here dropped the languages.
        repo.updateStack(StackEntity.create(stack))
    }
}