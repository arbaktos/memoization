package com.example.android.memoization.domain.usecases

import android.util.Log
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.extensions.toStack
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.TAG

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetStackUseCase {
    operator fun invoke(stackId: Long): Flow<LoadingState<Stack>>
}

class GetStackUseCaseImpl @Inject constructor(private val stackRepo: StackRepository) :
    GetStackUseCase {
    override fun invoke(stackId: Long): Flow<LoadingState<Stack>> {

        return stackRepo.getStackWithWordsById(stackId)
            .map {
                LoadingState.Collected(it.toStack())
            }
            .catch {
                LoadingState.Error
            }
    }
}