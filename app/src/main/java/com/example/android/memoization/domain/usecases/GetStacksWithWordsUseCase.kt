package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.stackdb.toMemoStack
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetStacksWithWordsUseCase {
    operator fun invoke(): Flow<LoadingState<List<MemoStack>>>
}

class GetStacksWithWordsUseCaseImpl @Inject constructor(private val stackRepo: StackRepository) :
    GetStacksWithWordsUseCase {
    private val TAG = "GetStacksWithWords"
    override fun invoke(): Flow<LoadingState<List<MemoStack>>> {
        return stackRepo.getStacksWithWords()
            .map { stackList ->
                val finalList = stackList.map { it.toMemoStack() }.filter { it.isVisible }.sortedWith(compareBy(nullsLast<Long>()) { it.pinnedTime })
                LoadingState.Collected(finalList)
            }
            .catch {
                LoadingState.Error
            }
    }
}