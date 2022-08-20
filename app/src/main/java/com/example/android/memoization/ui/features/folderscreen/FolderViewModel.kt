package com.example.android.memoization.ui.features.folderscreen

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.*
import androidx.work.*
import com.example.android.memoization.MemoizationApp
import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.domain.usecases.GetFoldersWithStackUseCase
import com.example.android.memoization.data.repository.MemoRepository
import com.example.android.memoization.domain.usecases.AddStackUseCase
import com.example.android.memoization.domain.usecases.GetStacksWithWordsUseCase
import com.example.android.memoization.ui.viewmodel.AppState
import com.example.android.memoization.ui.viewmodel.State.appStatePublic
import com.example.android.memoization.ui.viewmodel.State.updateState
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.TAG
import com.example.android.memoization.utils.workers.StackDeletionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


//@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repository: MemoRepository,
//    private val workManager: WorkManager,
    application: MemoizationApp,
    private val addStackUseCase: AddStackUseCase,
    private val getStacksWithWordsUseCase: GetStacksWithWordsUseCase
) : ViewModel() {

    val folderState = appStatePublic

    private val workManager = WorkManager.getInstance(application)
    lateinit var stacksWithWords: Flow<List<Stack>>

    //    var languages: ApiLanguage? = null
    val toastMessage = MutableLiveData<String>()


    init {
        viewModelScope.launch {
            stacksWithWords = getStacksWithWordsUseCase()
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
        workManager.cancelAllWorkByTag(stack.stackId.toString())
    }

    fun addStackToFolder(folder: Folder, stack: Stack) {
        viewModelScope.launch {
            addStackUseCase(folder, stack)
        }
    }

    fun changeCurrentStack(stack: Stack) {
        updateState { it.copy(currentStack = stack) }
    }

    fun getLanguages() {
        viewModelScope.launch {
            val response = repository.getLanguages()
            if (response.isSuccessful) {
//                languages = response.body()

            } else {
                Log.d(
                    "viewmodel",
                    "response was not a success ${response.errorBody().toString()}"
                )
            }
        }
    }

    data class FolderState(
        var folders: List<Folder>,
        var currentStack: Stack?,
        var currentFolder: Folder
    )

    private fun MutableStateFlow<AppState>.toFoldersState(): StateFlow<FolderState> {
        return MutableStateFlow(
            FolderState(
                folders = this.value.folders,
                currentStack = this.value.currentStack,
                currentFolder = this.value.currentFolder
            )
        )
    }
}