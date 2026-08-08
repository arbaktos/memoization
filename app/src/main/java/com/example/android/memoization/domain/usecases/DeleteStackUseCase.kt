package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.repository.StackRepository
import javax.inject.Inject

interface DeleteStackUseCase {
    suspend operator fun invoke(stackId: Long)
}

class DeleteStackUseCaseImpl @Inject constructor(
    private val repository: StackRepository
): DeleteStackUseCase {
    override suspend fun invoke(stackId: Long) {
        repository.deleteStackFomDb(stackId)
    }
}