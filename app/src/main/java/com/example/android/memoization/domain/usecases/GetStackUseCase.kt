package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.utils.workers.toStack
import javax.inject.Inject

interface GetStackUseCase {
    suspend operator fun invoke(stackId: Long): Stack
}

class GetStackUseCaseImpl @Inject constructor(private val stackRepo: StackRepository): GetStackUseCase {
    override suspend fun invoke(stackId: Long): Stack {
        return stackRepo.getStackWithWordsById(stackId).toStack()
    }
}