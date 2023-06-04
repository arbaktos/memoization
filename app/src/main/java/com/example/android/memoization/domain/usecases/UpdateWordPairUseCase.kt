package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.model.WordPair
import javax.inject.Inject

interface UpdateWordPairUseCase {
    suspend operator fun invoke(wordPair: WordPair)
}

class UpdateWordPairUseCaseImpl @Inject constructor(private val repo: WordPairRepository): UpdateWordPairUseCase {
    override suspend fun invoke(wordPair: WordPair) {
        repo.updateWordPairInDb(wordPair.toWordPairEntity())
    }

}