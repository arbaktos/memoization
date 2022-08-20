package com.example.android.memoization.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.repository.MemoRepository
import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.domain.usecases.GetFoldersWithStackUseCase
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import com.example.android.memoization.utils.ID_NO_FOLDER
import com.example.android.memoization.utils.TAG
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseViewModel @Inject constructor(
    val getFoldersWithStackUseCase: GetFoldersWithStackUseCase,
    val repository: MemoRepository
) : ViewModel() {

    init {
//        viewModelScope.launch {
//            getFoldersWithStackUseCase().observe(viewModelScope) { folders ->
////                if (folders.isEmpty()) addFolder(folderState.value.currentFolder)
//
//                updateState {
//                    it.copy(currentFolder = folders)
//                }
//                repository.getFoldersWithStacks2()
//            }
//            Log.d(TAG, "${folderState.value.currentFolder.stacks}")
//        }

    }

    private fun addFolder(folder: Folder) {
        viewModelScope.launch {
            repository.insertFolder(folder.toFolderEntity())
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}

data class AppState(
    var folders: List<Folder> = emptyList(),
    var currentStack: Stack? = null,
    var currentFolder: Folder = Folder(name = "NoFolder", folderId = ID_NO_FOLDER)
)



object State {

    private var appStateFlow = MutableStateFlow(AppState())
    val appStatePublic: StateFlow<AppState> = appStateFlow
    private val appState = appStateFlow.value


    fun updateState(update: (AppState) -> AppState) {
        val updatedState = update(appState)
        appStateFlow.value = updatedState
    }
}




