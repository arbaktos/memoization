package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.StackEntity
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.utils.Default_folder_ID
import javax.inject.Inject

interface UpdateStackUseCase {
    suspend operator fun invoke(stack: Stack)
}

class UpdateStackUseCaseImpl @Inject constructor(val repo: StackRepository): UpdateStackUseCase {

    override suspend fun invoke(stack: Stack) {
            val stackEntity = StackEntity(
                name = stack.name,
                numRep = stack.numRep,
                stackId = stack.stackId,
                parentFolderId = Default_folder_ID,//appState.currentFolder.folderId ?: ID_NO_FOLDER,
                hasWords = stack.hasWords,
                pinned = stack.pinned
            )
        repo.updateStack(stackEntity)
    }

}