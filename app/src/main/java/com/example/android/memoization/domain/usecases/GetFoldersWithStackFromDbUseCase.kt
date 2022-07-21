package com.example.android.memoization.domain.usecases

import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.repository.MemoRepository
import com.example.android.memoization.domain.model.Stack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GetFoldersWithStackFromDbUseCase {
    suspend operator fun invoke(): List<Folder>
}

class GetFoldersWithStackFromDbUseCaseImpl @Inject constructor(
    private val repository: MemoRepository
) :
    GetFoldersWithStackFromDbUseCase {
    override suspend fun invoke(): List<Folder> {
        return repository.getFoldersWithStacks().map { folderWithStacks ->
            Folder(
                name = folderWithStacks.folderEntity.name,
                isOpen = folderWithStacks.folderEntity.isOpen,
                folderId = folderWithStacks.folderEntity.folderId,
                stacks = folderWithStacks.stacks
                    .map { stackEntity ->
                        Stack(
                            name = stackEntity.name,
                            numRep = stackEntity.numRep,
                            stackId = stackEntity.stackId,
                            hasWords = stackEntity.hasWords
                        )
                    }
                    .map { it.getWords() }
                    .map { it.prepareStack() }.toMutableList()
            )
        }
    }

    private suspend fun Stack.getWords(): Stack = withContext(Dispatchers.IO) {
        val stackNeeded = repository.getStackWithWordsById(this@getWords.stackId)
        this@getWords.words = stackNeeded.words.map { it.toWordPair() }.toMutableList()
        this@getWords
    }
}

