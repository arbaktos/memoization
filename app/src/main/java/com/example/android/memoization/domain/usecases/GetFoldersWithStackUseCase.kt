package com.example.android.memoization.domain.usecases

import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.data.repository.MemoRepository
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.model.Stack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GetFoldersWithStackUseCase {
    suspend operator fun invoke(): List<Folder>
}

//TODO make it a LiveData so state could subscribe on it?
class GetFoldersWithStackUseCaseImpl @Inject constructor(
    private val repository: MemoRepository,
    private val stackRepo: StackRepository
) :
    GetFoldersWithStackUseCase {
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
        val stackNeeded = stackRepo.getStackWithWordsById(this@getWords.stackId)
        this@getWords.words = stackNeeded.words.map { it.toWordPair() }.toMutableList()
        this@getWords
    }
}

