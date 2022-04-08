package com.example.android.memoization.ui.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.work.*
import com.example.android.memoization.MemoizationApp
import com.example.android.memoization.api.WordTranslationRequest
import com.example.android.memoization.database.FolderEntity
import com.example.android.memoization.database.StackEntity
import com.example.android.memoization.database.WordPairEntity
import com.example.android.memoization.model.Folder
import com.example.android.memoization.model.Stack
import com.example.android.memoization.model.WordPair
import com.example.android.memoization.repository.MemoRepository
import com.example.android.memoization.utils.ID_NO_FOLDER
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.WP_ID
import com.example.android.memoization.utils.workers.StackDeletionWorker
import com.example.android.memoization.utils.workers.WordPairInvisibleWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FolderViewModel @Inject constructor(
    private val repository: MemoRepository, application: MemoizationApp
) : ViewModel() {

    private val _appState = MutableStateFlow(AppState())
    val publicAppState: StateFlow<AppState> = _appState
    private val appState: AppState
        get() = _appState.value

    private val workManager = WorkManager.getInstance(application)

    //    var languages: ApiLanguage? = null
    val toastMessage = MutableLiveData<String>()


    init {
        addFolder()
        getFoldersWithStackFromDb()
    }

    fun getFoldersWithStackFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val folders = repository.getFoldersWithStacks().map { folderwithStacks ->
                Folder(
                    folderwithStacks.folderEntity.name,
                    folderwithStacks.folderEntity.isOpen,
                    folderwithStacks.stacks.map { stackEntity ->
                        Stack(
                            name = stackEntity.name,
                            numRep = stackEntity.numRep,
                            stackId = stackEntity.stackId,
                            hasWords = stackEntity.hasWords
                        )
                    }.toMutableList(),
                    folderwithStacks.folderEntity.folderId
                )
            }
            updateState { it.copy(folders = folders) }
        }
    }

    fun deleteFolderFromDb(folder: Folder) {
        val folderEntity = FolderEntity(folder.name, folder.isOpen, folder.folderId)
        viewModelScope.launch {
            folder.stacks.forEach {
                deleteStackFromDb(it)
            }
            repository.deleteFolderFromDb(folderEntity)
        }
    }

    fun deleteStackWithDelay(stack: Stack) {
        val inputData = Data.Builder()
            .putLong(STACK_ID, stack.stackId).build()
        val workDelayDeleteRequest = OneTimeWorkRequestBuilder<StackDeletionWorker>()
            .addTag("${stack.stackId}")
            .setInputData(inputData)
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(workDelayDeleteRequest)
    }

    fun deleteStackFromDb(stack: Stack) {
        viewModelScope.launch(Dispatchers.IO) {
            stack.words.forEach {
                deleteWordPairFromDb(it)
            }
            repository.deleteStackFomDb(stack.stackId)
        }
    }

    private fun deleteWordPairFromDb(wordPair: WordPair?) {
        wordPair?.let {
            val wordPairEntity =
                WordPairEntity(
                    wordPair.stackId,
                    wordPair.word1,
                    wordPair.word2,
                    wordPair.lastRep,
                    wordPair.toShow,
                    wordPair.levelOfKnowledge,
                    wordPairId = wordPair.wordPairId,
                    wordPair.isVisible
                )
            viewModelScope.launch {
                repository.deleteWordPairFromDb(wordPairEntity)
                getWordsFromStack()
            }
        }
    }

    private fun getWordsFromStack() {
        val stack = appState.currentStack
        viewModelScope.launch(Dispatchers.IO) {
            stack?.let {
                val wordPairs = repository
                    .getWordsFromStack(stack.stackId)
                    .map {
                        it.toWordPair()
                    }.toMutableList()
                stack.words = wordPairs
            }
        }
    }

    fun addFolder(folder: Folder? = appState.currentFolder) {
        viewModelScope.launch {
            folder?.let {
                repository.insertFolder(it.toFolderEntity())
                getFoldersWithStackFromDb()
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
        updateState { it.copy(
            currentFolder =  folder
        ) }
        viewModelScope.launch {
            repository.insertStack(stackToInsert)
            getFoldersWithStackFromDb()
        }
    }

    fun cancelDelayDeletionWork(wordPair: WordPair) {
        workManager.cancelAllWorkByTag("${wordPair.wordPairId}")
    }

    fun prepareStack(): Stack {
        val currentDate = Date()
        appState.currentStack!!.words.forEach { wordPair ->
            val timeWithoutRepetion = currentDate.time - wordPair.lastRep.time
            val daysWithoutRepetition = longToDays(timeWithoutRepetion)

            if (daysWithoutRepetition > wordPair.levelOfKnowledge.frequency) wordPair.toShow = true
        }
        return appState.currentStack!!
    }

    private fun longToDays(timeSpace: Long): Int {
        val seconds = timeSpace / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = (hours / 24).toInt()
        return days
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





