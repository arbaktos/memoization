package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.toStack
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.utils.LoadingState

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetStackUseCase {
    operator fun invoke(stackId: Long): Flow<LoadingState<MemoStack>>
}

class GetStackUseCaseImpl @Inject constructor(private val stackRepo: StackRepository) :
    GetStackUseCase {
    override fun invoke(stackId: Long): Flow<LoadingState<MemoStack>> {

        return stackRepo.getStackWithWordsById(stackId)
            .map {
                LoadingState.Collected(it.toStack())
            }
            .catch {
                LoadingState.Error
            }
    }
}