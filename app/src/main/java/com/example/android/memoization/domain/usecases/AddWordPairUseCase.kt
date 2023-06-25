package com.example.android.memoization.domain.usecases

//import com.example.android.memoization.data.SessionState
import com.example.android.memoization.data.database.WordPairEntity
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.data.model.WordPair
import javax.inject.Inject

interface AddWordPairUseCase {
    suspend operator fun invoke(wordPair: WordPair)
}

class AddWordPairUseCaseImpl @Inject constructor(
    val repo: WordPairRepository
): AddWordPairUseCase {
    override suspend fun invoke(wordPair: WordPair) {
        repo.insertWordPair(wordPair.toWordPairEntity())
    }

}