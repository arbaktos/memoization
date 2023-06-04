package com.example.android.memoization.domain.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.data.repository.FolderRepository
import com.example.android.memoization.domain.model.MemoStack
import javax.inject.Inject

interface GetFoldersWithStackUseCase {
    suspend operator  fun invoke(): LiveData<List<Folder>>
}

class GetFoldersWithStackUseCaseImpl @Inject constructor(
    private val repository: FolderRepository,
    private val getStackUseCase: GetStackUseCase
) :
    GetFoldersWithStackUseCase {
    override suspend fun invoke(): LiveData<List<Folder>> {
        val folders = repository.getFoldersWithStacks()//.asFlow()
            .map { folderWithStacksList->
                folderWithStacksList.map { folderWithStacks ->
                    Folder(
                        name = folderWithStacks.folderEntity.name,
                        isOpen = folderWithStacks.folderEntity.isOpen,
                        folderId = folderWithStacks.folderEntity.folderId,
                        stacks = folderWithStacks.stacks
                            .map { stackEntity ->
                                MemoStack(
                                    name = stackEntity.name,
                                    numRep = stackEntity.numRep,
                                    stackId = stackEntity.stackId,
                                    hasWords = stackEntity.hasWords
                                )
                            }
//                            .map { getStackUseCase(it.stackId) }
                            .map { it.prepareStack() }.toMutableList()
                    )
                }
            }
       // Log.d(TAG, "invoke: ${folders.asLiveData().value}")
        return folders
    }
}

