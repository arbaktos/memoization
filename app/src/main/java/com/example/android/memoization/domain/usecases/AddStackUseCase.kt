package com.example.android.memoization.domain.usecases

import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.model.BaseStack
import com.example.android.memoization.data.repository.FolderRepository
import com.example.android.memoization.data.model.Folder
import javax.inject.Inject

interface AddStackUseCase {
    suspend operator fun invoke(folder: Folder, stack: BaseStack)
}

class AddStackUseCaseImpl @Inject constructor( private val repository: FolderRepository): AddStackUseCase {
    override suspend fun invoke(folder: Folder, stack: BaseStack) {
//        val stackToInsert = StackEntity(
//            name = stack.name,
//            numRep = stack.numRep,
//            stackId = stack.stackId,
//            hasWords = stack.hasWords
//        )
        repository.insertStack(stack as StackEntity)
    }
}
