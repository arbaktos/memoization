package com.example.android.memoization.ui.viewmodel

import android.util.Log
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
import com.example.android.memoization.utils.WP_ID
import com.example.android.memoization.utils.workers.WordPairInvisibleWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MemoizationViewModel @Inject constructor(
    private val repository: MemoRepository, application: MemoizationApp
) : ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders = _folders
    val currentFolder = MutableStateFlow<Folder>(Folder(name = "NoFolder", folderId = ID_NO_FOLDER))

    private var _currentStack: MutableStateFlow<Stack?> = MutableStateFlow(Stack(""))
    var currentStack = _currentStack

    private val _wordPair = MutableLiveData<WordPair>()
    var newWordPair: LiveData<WordPair> = _wordPair

    val word1 = MutableLiveData<String>()
    val word2 = MutableLiveData<String?>()

    //    var languages: ApiLanguage? = null
    val toastMessage = MutableLiveData<String>()

    val workManager = WorkManager.getInstance(application)
//    val outputWorkInfo: LiveData<List<WorkInfo>>


    fun getTranslation(request: WordTranslationRequest) {
        viewModelScope.launch {
            val response = repository.getTranslation(request)
            val code = response.code()
            when (code) {
                in 100..300 -> { // check the https codes to be correct for successfull and erroneos messages
                    word2.value = response.body()?.translation
                }
                in 300..599 -> {
                    toastMessage.value = response.errorBody()
                        .toString() // there is err field in response.body() - check which one is working
                }
            }
        }
    }

    fun getFoldersWithStackFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            _folders.value = repository.getFoldersWithStacks().map { folderwithStacks ->
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
        }
        addFolder()
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

    fun updateStackInDb() {
        val stack = currentStack.value
        stack?.let {
            val stackEntity = StackEntity(stack.name, stack.numRep, stack.stackId, currentFolder.value.folderId, stack.hasWords)
            viewModelScope.launch {
                repository.updateStack(stackEntity)
            }
        }
    }

    fun deleteStackFromDb(stack: Stack) {
        val stackEntity = StackEntity(stack.name, stack.numRep, stack.stackId)
        viewModelScope.launch(Dispatchers.IO) {
            stack.words.forEach {
                deleteWordPairFromDb(it)
            }
            repository.deleteStackFomDb(stackEntity)
        }
    }

    fun delayDeletionWordPair(wordPair: WordPair) {
        val inputData = Data.Builder()
            .putLong(WP_ID, wordPair.wordPairId).build()
        val workDealyDeleteRequest = OneTimeWorkRequestBuilder<WordPairInvisibleWorker>()
            .addTag("${wordPair.wordPairId}")
            .setInputData(inputData)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(workDealyDeleteRequest)
    }

    fun cancelDelayDeletionWork(wordPair: WordPair) {
        workManager.cancelAllWorkByTag("${wordPair.wordPairId}")
    }

    fun updateWordPairInDb(wordPair: WordPair) {
        viewModelScope.launch {
            val wordPairToUpdate =
                WordPairEntity(
                    currentStack.value!!.stackId,
                    wordPair.word1,
                    wordPair.word2,
                    wordPair.lastRep,
                    wordPair.toShow,
                    wordPair.levelOfKnowledge,
                    wordPairId = wordPair.wordPairId,
                    wordPair.isVisible
                )
            repository.updateWordPairInDb(wordPairToUpdate)
        }
    }

    fun deleteWordPairFromDb(wordPair: WordPair? = _wordPair.value) {
        wordPair?.let {
            val wordPairEntity =
            WordPairEntity(
                currentStack.value!!.stackId,
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
        viewModelScope.launch(Dispatchers.IO) {
            val wordPairs = repository
                .getWordsFromStack(currentStack.value!!.stackId)
                .map {
                    it.toWordPair()
                }.toMutableList()
            currentStack.value?.words = wordPairs
        }

    }

    fun findAndRemoveWordPair(
        stack: Stack = currentStack.value!!,
        wordPair: WordPair = newWordPair.value!!
    ) {
//        stack.words.forEach {
//            if (it == wordPair) stack.words.remove(it)
//        }
        stack.words.filter { it == wordPair }
    }

    fun updateCurrentWordPair(wordPair: WordPair? = _wordPair.value) {
        wordPair?.let {
            _wordPair.value = wordPair
        }
    }

    fun composeWordPairFromWords() {
        if (!word1.value.isNullOrBlank() || !word2.value.isNullOrBlank()) {
            _wordPair.value =
                WordPair(currentStack.value?.stackId ?: 0, word1.value!!, word2.value)
        }
    }

    private fun clearWordPair() {
        _wordPair.value = null
        word1.value = ""
        word2.value = null
    }

    fun addFolder(folder: Folder = currentFolder.value) {
        viewModelScope.launch {
            repository.insertFolder(folder.toFolderEntity())
            getFoldersWithStackFromDb()
        }
    }

    fun addStackToFolder(folder: Folder?, stack: Stack) {
        folder?.stacks?.add(stack)
        val stackToInsert = StackEntity(
            stack.name,
            stack.numRep,
            stack.stackId,
            folder?.folderId ?: ID_NO_FOLDER,
            hasWords = stack.words.isNotEmpty()
        )
        viewModelScope.launch {
            repository.insertStack(stackToInsert)
            getFoldersWithStackFromDb()
        }
    }

    fun addWordPairToDb() {
        _wordPair.value?.let {
            currentStack.value!!.words.add(_wordPair.value!!)
            currentStack.value!!.hasWords = true
            val wordPairEntityToInsert = WordPairEntity(
                parentStackId = currentStack.value!!.stackId,
                word1 = _wordPair.value?.word1 ?: "",
                word2 = _wordPair.value?.word2 ?: ""
            )
            viewModelScope.launch {
                repository.insertWordPair(wordPairEntityToInsert)
            }
            clearWordPair()
        }

    }

    fun prepareStack(): Stack {
        val currentDate = Date()
        currentStack.value!!.words.forEach { wordPair ->
            val timeWithoutRepetion = currentDate.time - wordPair.lastRep.time
            val daysWithoutRepetition = longToDays(timeWithoutRepetion)
//            Log.d("viewmodel", "$time")
//            Log.d("viewmodel", "$it  ${it.toShow}")

            if (daysWithoutRepetition > wordPair.levelOfKnowledge.frequency) wordPair.toShow =
                true
        }
        return currentStack.value!!
    }

    private fun longToDays(timeSpace: Long): Int {
        val seconds = timeSpace / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = (hours / 24).toInt()
        return days
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

    fun changeCurrentStack(stack: Stack) {
        _currentStack.value = stack
        getStacksWithWords()
    }


    private fun getStacksWithWords() {
        viewModelScope.launch(Dispatchers.IO) {
            val stackNeeded = repository.getStacksWithWords().filter { stackWithWords ->
                stackWithWords.stack.stackId == currentStack.value!!.stackId
            }.first()
            val words = stackNeeded.words.map { it.toWordPair() }.toMutableList()
            currentStack.value?.words = words
        }
    }

    //    fun getNoFolderWithStacksFromDb(): Folder {
//        var noFolder: Folder = Folder("")
//        viewModelScope.launch(Dispatchers.IO) {
//            val noFolWithStacks = repository.getSingleFolderWithStacks(ID_NO_FOLDER)
//            noFolWithStacks?.let {
//                Log.d(TAG, "$noFolWithStacks")
//                val noFolEnt = noFolWithStacks.folderEntity
//                val stacks = noFolWithStacks.stacks.
//                map { Stack(it.name, it.numRep, stackId = it.stackId) }.toMutableList()
//                noFolder = Folder(noFolEnt.name, true, stacks, noFolEnt.folderId )
//            }
//        }
//        return noFolder
//    }
}

