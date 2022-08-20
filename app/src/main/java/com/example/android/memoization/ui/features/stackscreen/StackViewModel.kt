package com.example.android.memoization.ui.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.android.memoization.data.api.WordTranslationRequest
import com.example.android.memoization.data.database.StackEntity
import com.example.android.memoization.data.database.WordPairEntity
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.data.repository.MemoRepository
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.utils.ID_NO_FOLDER
import com.example.android.memoization.utils.WP_ID
import com.example.android.memoization.utils.workers.WordPairInvisibleWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StackViewModel @Inject constructor(
    private val repository: StackRepository,
    private val wpRepo: WordPairRepository,
    application: Application
) : ViewModel() {

    private val _stackState = MutableStateFlow(StackState())
    val publicStackState: StateFlow<StackState> = _stackState
    private val stackState: StackState
        get() = _stackState.value

    val toastMessage = MutableLiveData<String>()
    private val workManager = WorkManager.getInstance(application)

    fun setCurrentStack(stack: Stack) {
        updateState {
            it.copy(
                stack = stack
            )
        }
        getStacksWithWords()
    }

    fun setWord(ind: Int, word: String) {
        when (ind) {
            1 -> updateState { it.copy(word1 = word) }
            2 -> updateState { it.copy(word2 = word) }
        }
    }

    private fun getStacksWithWords() {
        //TODO !!!!!!
//        viewModelScope.launch(Dispatchers.IO) {
//            val stackNeeded = repository.getStacksWithWords().filter { stackWithWords ->
//                stackWithWords.stack.stackId == stackState.stack?.stackId
//            }.firstOrNull()
//            stackNeeded?.let { stack ->
//                val words = stack.words.map { it.toWordPair() }.toMutableList()
//                updateState { it.copy(
//                    stack = stackState.stack?.copy(words = words)
//                ) }
//            }
//        }
    }

    fun cancelDelayDeletionWork(wordPair: WordPair) {
        workManager.cancelAllWorkByTag("${wordPair.wordPairId}")
    }

    fun delayDeletionWordPair(wordPair: WordPair) {
        val inputData = Data.Builder()
            .putLong(WP_ID, wordPair.wordPairId).build()
        val workDelayDeleteRequest = OneTimeWorkRequestBuilder<WordPairInvisibleWorker>()
            .addTag("${wordPair.wordPairId}")
            .setInputData(inputData)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(workDelayDeleteRequest)
    }

    fun onPin() {
        updateState { it.copy(stack = it.stack?.copy(pinned = !(stackState.stack?.pinned ?: true))) }
        updateStackInDb()
    }

    fun updateStackInDb() {
        val stack = stackState.stack
        stack?.let {
            val stackEntity = StackEntity(
                name = stack.name,
                numRep = stack.numRep,
                stackId = stack.stackId,
                parentFolderId = ID_NO_FOLDER,//appState.currentFolder.folderId ?: ID_NO_FOLDER,
                hasWords = stack.hasWords,
                pinned = stack.pinned
            )
            viewModelScope.launch {
                repository.updateStack(stackEntity)
            }
        }
    }

    fun updateWordPairDateInDb() {
        viewModelScope.launch {
            stackState.wordPair?.toWordPairEntity()?.let { wpRepo.updateWordPairInDb(it) }
        }
    }

    fun updateWordPairInDb() {
        val newWordPair =
            stackState.wordPair?.copy(word1 = stackState.word1 ?: "", word2 = stackState.word2)

        viewModelScope.launch {
            newWordPair?.toWordPairEntity()?.let { wpRepo.updateWordPairInDb(it) }
        }
    }

    private fun deleteWordPairFromDb(wordPair: WordPair? = stackState.wordPair) {
        wordPair?.let {
            val wordPairEntity =
                WordPairEntity(
                    wordPair.stackId,
                    wordPair.word1,
                    wordPair.word2,
                    wordPair.lastRep,
                    wordPair.toLearn,
                    wordPair.levelOfKnowledge,
                    wordPairId = wordPair.wordPairId,
                    wordPair.isVisible
                )
            viewModelScope.launch {
                wpRepo.deleteWordPairFromDb(wordPairEntity)
                getWordsFromStack()
            }
        }
    }

    private fun getWordsFromStack() { // double with folder viewmodel
        val stack = stackState.stack
        viewModelScope.launch(Dispatchers.IO) {
            stack?.let {
                val wordPairs = wpRepo
                    .getWordsFromStack(stack.stackId)
                    .map {
                        it.toWordPair()
                    }.toMutableList()
                stack.words = wordPairs
            }
        }
    }

    fun findAndUpdateWordPair() {
        stackState.stack?.let {
            it.words.forEachIndexed { ind, wordPair ->
                if (wordPair == stackState.wordPair) {
                    it.words[ind] =
                        wordPair.copy(word1 = stackState.word1 ?: "", word2 = stackState.word2)
                }
            }
        }
    }

    fun addWordPairToDb() {
        stackState.wordPair?.let {
            stackState.stack!!.words.add(stackState.wordPair!!)
            stackState.stack!!.hasWords = true
            val wordPairEntityToInsert = WordPairEntity(
                parentStackId = stackState.stack!!.stackId,
                word1 = stackState.word1 ?: "",
                word2 = stackState.word2 ?: ""
            )
            viewModelScope.launch {
                wpRepo.insertWordPair(wordPairEntityToInsert)
            }
            clearWordPair()
        }

    }

    fun updateCurrentWordPair(wordPair: WordPair?) {
        updateState {
            it.copy(
                wordPair = wordPair
            )
        }
    }

    fun composeWordPairFromWords() {
        if (!stackState.word1.isNullOrBlank() || !stackState.word2.isNullOrBlank()) {
            updateState {
                it.copy(
                    wordPair = WordPair(
                        stackState.stack?.stackId ?: 0,
                        stackState.word1 ?: "",
                        stackState.word2
                    )
                )
            }
        }
    }

    fun clearWordPair() {
        updateState {
            it.copy(
                wordPair = null,
                word1 = null,
                word2 = null
            )
        }
    }

    fun getTranslation(request: WordTranslationRequest) {
        viewModelScope.launch {
//            try {
//                val response = repository.getTranslation(request)
//                val code = response.code()
//                when (code) {
//                    in 100..300 -> {
//
//                        updateState {
//                            it.copy(
//                                word2 = response.body()?.translation
//                            )
//                        }// check the https codes to be correct for successful and erroneous messages
//                    }
//                    in 300..599 -> {
//                        toastMessage.value = response.errorBody()
//                            .toString() // there is err field in response.body() - check which one is working
//                    }
//                }
//            } catch (e: Exception) {
//                toastMessage.value = "Error accessing the Internet"
//            }

        }
    }


    private fun updateState(update: (StackState) -> StackState) {
        val updatedState = update(stackState)
        _stackState.value = updatedState
    }
}

data class StackState(
    var stack: Stack? = null,
    val words: List<WordPair> = emptyList(),
    var wordPair: WordPair? = null,// this one and below should be in another state
    var word1: String? = null,
    var word2: String? = null
)