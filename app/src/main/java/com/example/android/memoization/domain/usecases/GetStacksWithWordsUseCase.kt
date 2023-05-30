package com.example.android.memoization.domain.usecases

import android.util.Log
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.extensions.toStack
import com.example.android.memoization.utils.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

interface GetStacksWithWordsUseCase {
    suspend operator fun invoke(): Flow<List<Stack>>
}

class GetStacksWithWordsUseCaseImpl @Inject constructor(private val stackRepo: StackRepository): GetStacksWithWordsUseCase {
    override suspend fun invoke(): Flow<List<Stack>> {
        return stackRepo.getStacksWithWords()
            .transform { listStacks ->
                emit (listStacks.map {
                    it.toStack()
                })
            }
    }
}