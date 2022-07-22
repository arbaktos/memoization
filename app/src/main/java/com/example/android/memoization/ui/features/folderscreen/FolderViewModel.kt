package com.example.android.memoization.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.example.android.memoization.MemoizationApp
import com.example.android.memoization.data.database.StackEntity
import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.domain.usecases.GetFoldersWithStackUseCase
import com.example.android.memoization.data.repository.MemoRepository
import com.example.android.memoization.domain.usecases.DeleteStackUseCase
import com.example.android.memoization.ui.composables.screens.TDEBUG
import com.example.android.memoization.utils.ID_NO_FOLDER
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.TAG
import com.example.android.memoization.utils.workers.StackDeletionWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FolderViewModel @Inject constructor(
    private val repository: MemoRepository,
    application: MemoizationApp,
    private val getFoldersWithWords: GetFoldersWithStackUseCase,
) : ViewModel() {

    private val _appState = MutableStateFlow(AppState())
    val publicAppState: StateFlow<AppState> = _appState
    private val appState: AppState
        get() = _appState.value

    private val workManager = WorkManager.getInstance(application)

    //    var languages: ApiLanguage? = null
    val toastMessage = MutableLiveData<String>()


    init {
//        addFolder()
        getFoldersWithStackFromDb()
    }

    fun getFoldersWithStackFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val folders = getFoldersWithWords()
            updateState { it.copy(folders = folders) }
        }
    }

    fun deleteStackWithDelay(stack: Stack) {
        val inputData = Data.Builder()
            .putLong(STACK_ID, stack.stackId).build()
        val workDelayDeleteRequest = OneTimeWorkRequestBuilder<StackDeletionWorker>()
            .addTag("${stack.stackId}")
            .setInputData(inputData)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(workDelayDeleteRequest)
    }

    fun cancelStackDeletion(stack: Stack) {
        Log.d(TDEBUG, "cancelled deletion $stack")
        workManager.cancelAllWorkByTag(stack.stackId.toString())
    }
//
//    private fun getWordsFromStack() {
//        val stack = appState.currentStack
//        viewModelScope.launch(Dispatchers.IO) {
//            stack?.let {
//                val wordPairs = repository
//                    .getWordsFromStack(stack.stackId)
//                    .map {
//                        it.toWordPair()
//                    }.toMutableList()
//                stack.words = wordPairs
//            }
//        }
//    }

    private fun addFolder(folder: Folder? = appState.currentFolder) {
        viewModelScope.launch {
            folder?.let {
                repository.insertFolder(it.toFolderEntity())
            }
        }
    }

    fun addStackToFolder(folder: Folder, stack: Stack) {
        folder.stacks.add(stack)
        val stackToInsert = StackEntity(
            stack.name,
            stack.numRep,
            stack.stackId,
            folder.folderId,
            hasWords = stack.words.isNotEmpty()
        )
        updateState {
            it.copy(
                currentFolder = folder
            )
        }
        viewModelScope.launch {
            repository.insertStack(stackToInsert)
            getFoldersWithStackFromDb()
        }
    }

    fun cancelDelayDeletionWork(wordPair: WordPair) {
        workManager.cancelAllWorkByTag("${wordPair.wordPairId}")
    }

    fun changeCurrentStack(stack: Stack) {
        updateState { it.copy(currentStack = stack) }
    }

    private fun updateState(update: (AppState) -> AppState) {
        val updatedState = update(appState)
        _appState.value = updatedState
    }

    fun getLanguages() {
        viewModelScope.launch {
            val response = repository.getLanguages()
            if (response.isSuccessful) {
//                languages = response.body()
//                Log.d ("viewmodel", " response ${response.body().toString()}")
//                Log.d ("viewmodel", " languages $languages")
            } else {
                Log.d(
                    "viewmodel",
                    "response was not a success ${response.errorBody().toString()}"
                )
            }
        }
    }
}

data class AppState(
    var folders: List<Folder> = emptyList(),
    var currentStack: Stack? = null,
    var currentFolder: Folder = Folder(name = "NoFolder", folderId = ID_NO_FOLDER),
)