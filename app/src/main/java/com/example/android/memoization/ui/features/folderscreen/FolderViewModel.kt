package com.example.android.memoization.ui.features.folderscreen

import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
//import com.example.android.memoization.data.SessionState
import com.example.android.memoization.domain.model.Folder
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.data.repository.FolderRepository
import com.example.android.memoization.domain.usecases.AddStackUseCase
import com.example.android.memoization.domain.usecases.GetStacksWithWordsUseCase
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.TAG
import com.example.android.memoization.utils.workers.StackDeletionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repository: FolderRepository,
    private val workManager: WorkManager,
    private val addStackUseCase: AddStackUseCase,
    private val getStacksWithWordsUseCase: GetStacksWithWordsUseCase
) : ViewModel() {


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

    fun addStackToFolder(folder: Folder = Folder("empty", folderId = Default_folder_ID), stack: Stack) {
        viewModelScope.launch {
            addStackUseCase(folder, stack)
        }
    }

    fun getLanguages() {
        viewModelScope.launch {
//            val response = repository.getLanguages()
//            if (response.isSuccessful) {
//                languages = response.body()
//
//            } else {
//                Log.d(
//                    "viewmodel",
//                    "response was not a success ${response.errorBody().toString()}"
//                )
//            }
        }
    }

}