package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.repository.WordPairRepository
import javax.inject.Inject

/**
 * Deleting a word pair hides it. The row and its sides stay, so an undo - or a future
 * "restore" screen - can bring it back with its progress intact.
 */
interface HideWordPairUseCase {
    suspend operator fun invoke(wordPairId: Long, hidden: Boolean = true)
}

class HideWordPairUseCaseImpl @Inject constructor(
    private val repository: WordPairRepository
) : HideWordPairUseCase {
    override suspend fun invoke(wordPairId: Long, hidden: Boolean) {
        repository.setVisible(wordPairId, !hidden)
    }
}
