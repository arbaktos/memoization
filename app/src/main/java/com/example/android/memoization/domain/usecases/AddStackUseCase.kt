package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.StackEntity
import com.example.android.memoization.data.repository.FolderRepository
import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.domain.model.Stack
import javax.inject.Inject

interface AddStackUseCase {
    suspend operator fun invoke(folder: Folder, stack: Stack)
}

class AddStackUseCaseImpl @Inject constructor( private val repository: FolderRepository): AddStackUseCase {
    override suspend fun invoke(folder: Folder, stack: Stack) {
        val stackToInsert = StackEntity(
            name = stack.name,
            numRep = stack.numRep,
            stackId = stack.stackId,
            parentFolderId = folder.folderId,
            hasWords = stack.words.isNotEmpty()
        )
        repository.insertStack(stackToInsert)
    }
}
