package com.example.android.memoization.domain.usecases

import android.util.Log
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.model.BaseWordPair
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.utils.TAG
import javax.inject.Inject

interface DeleteWordPairUseCase {
    suspend operator fun invoke(wordPair: BaseWordPair)
}

class DeleteWordPairUseCaseImpl @Inject constructor(
    val repository: WordPairRepository) : DeleteWordPairUseCase {
    override suspend fun invoke(wordPair: BaseWordPair) {
        repository.deleteWordPairFromDb(wordPair as WordPairEntity)
    }
}

