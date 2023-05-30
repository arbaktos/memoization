package com.example.android.memoization.domain.usecases

import android.util.Log
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.utils.TAG
import javax.inject.Inject

interface DeleteStackUseCase {
    suspend operator fun invoke(stackId: Long)
}

class DeleteStackUseCaseImpl @Inject constructor(
    private val deleteWordPairUseCase: DeleteWordPairUseCase,
    private val repository: StackRepository
): DeleteStackUseCase {

    override suspend fun invoke(stackId: Long) {
//        stack.words.forEach {
//            deleteWordPairUseCase(it)
//        }
        repository.deleteStackFomDb(stackId)
    }
}