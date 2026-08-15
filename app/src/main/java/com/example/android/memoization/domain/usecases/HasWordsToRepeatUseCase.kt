package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.stackdb.toMemoStack
import com.example.android.memoization.data.repository.LearningSettingsRepository
import com.example.android.memoization.data.repository.StackRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface HasWordsToRepeatUseCase {
    suspend operator fun invoke(): Boolean
}

class HasWordsToRepeatUseCaseImpl @Inject constructor(
    private val stackRepo: StackRepository,
    private val learningSettings: LearningSettingsRepository,
) : HasWordsToRepeatUseCase {

    override suspend fun invoke(): Boolean {
        val practice = learningSettings.practiceSides.first()
        return stackRepo.getStacksWithWords().first()
            .map { it.toMemoStack() }
            .filter { it.isVisible }
            .any { it.hasDue(practice) }
    }
}
