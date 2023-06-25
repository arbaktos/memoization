package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetWordPairLoadingStateUseCase {
    operator fun invoke(wpId: Long): Flow<LoadingState<WordPair>>
}

class GetWorPairLoadingStateUseCaseImpl @Inject constructor(private val wordPairRepo: WordPairRepository) :
    GetWordPairLoadingStateUseCase {
    override fun invoke(wpId: Long): Flow<LoadingState<WordPair>> {
        return wordPairRepo.getWordPairById(wpId)
            .map {
                LoadingState.Collected(it)
            }
            .catch {
                LoadingState.Error
            }
    }

}