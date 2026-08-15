package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.model.BaseWordPair
import com.example.android.memoization.data.repository.WordPairRepository
import javax.inject.Inject

interface DeleteWordPairUseCase {
    suspend operator fun invoke(wordPair: BaseWordPair)
}

class DeleteWordPairUseCaseImpl @Inject constructor(
    val repository: WordPairRepository) : DeleteWordPairUseCase {
    override suspend fun invoke(wordPair: BaseWordPair) {
        repository.deleteWordPairFromDb(wordPair)
    }
}

