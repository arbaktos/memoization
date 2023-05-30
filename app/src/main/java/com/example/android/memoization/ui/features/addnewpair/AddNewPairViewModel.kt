package com.example.android.memoization.ui.features.addnewpair

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.api.WordTranslationRequest
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.domain.usecases.AddWordPairUseCase
import com.example.android.memoization.domain.usecases.GetWordPairLoadingStateUseCase
import com.example.android.memoization.domain.usecases.UpdateWordPairUseCase
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewPairViewModel @Inject constructor(
    val updateWordPairUseCase: UpdateWordPairUseCase,
    val addWordPairUseCase: AddWordPairUseCase,
    val getWordPairLoadingState: GetWordPairLoadingStateUseCase
) : ViewModel() {

    private val TAG = "AddNewPairViewModel"
    private var currentWordPair: WordPair? = null
    var word1 = Empty_string
    var word2 = Empty_string
    private var currentStackId: Long? = null
    val toastMessage = MutableLiveData<String>()

    fun updateWordPairInDb() {
        Log.d(TAG, "updateWordPairInDb: word1: $word1 word2: $word2")
        val newWordPair = currentWordPair!!.copy(word1 = word1, word2 = word2)

        viewModelScope.launch {
            updateWordPairUseCase(newWordPair)
        }
        clearWordPair()
    }

    fun addWordPairToDb() {
        composeWordPairFromWords(word1, word2)
        viewModelScope.launch {
            addWordPairUseCase(currentWordPair!!)
        }
        clearWordPair()
    }

    private fun composeWordPairFromWords(word1: String, word2: String) {
        if (currentWordPair == null) {
            currentWordPair = WordPair(
                stackId = currentStackId ?: Default_folder_ID,
                word1 = word1,
                word2 = word2
            )
        } else {
            currentWordPair = currentWordPair?.copy(
                word1 = word1,
                word2 = word2
            )
        }

    }

    fun setCurrentWordPair(wordPair: WordPair?) {
        currentWordPair = wordPair
//        word1 = wordPair?.word1 ?: ""
//        word2 = wordPair?.word2 ?: ""
    }

    fun setCurrentStackId(stackId: Long) {
        currentStackId = stackId
    }


    fun clearWordPair() {
        currentWordPair = null
        word1 = ""
        word2 = ""
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
}