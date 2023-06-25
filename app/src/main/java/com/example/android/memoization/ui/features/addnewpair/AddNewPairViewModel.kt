package com.example.android.memoization.ui.features.addnewpair

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.domain.usecases.AddWordPairUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.GetWordPairLoadingStateUseCase
import com.example.android.memoization.domain.usecases.UpdateWordPairUseCase
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.data.TranslationState
import ru.vasilisasycheva.translation.domain.TranslationRepo
import javax.inject.Inject

@HiltViewModel
class AddNewPairViewModel @Inject constructor(
    val updateWordPairUseCase: UpdateWordPairUseCase,
    val addWordPairUseCase: AddWordPairUseCase,
    val getWordPairLoadingState: GetWordPairLoadingStateUseCase,
    val translationRepo: TranslationRepo,
    val getStackUseCase: GetStackUseCase
) : ViewModel() {

    private val TAG = "AddNewPairViewModel"
    private var currentWordPair: WordPair? = null
    var word1 = Empty_string
    var word2 = Empty_string
    private var frLang: String? = null
    private var tLang: String? = null
    private var currentStackId: Long? = null
    var translationLoading = false
    val toastMessage = MutableLiveData<String>()

    fun updateWordPairInDb() {
        Log.d(TAG, "updateWordPairInDb: word1: $word1 word2: $word2")
        val newWordPair = currentWordPair!!.copy(word1 = word1, word2 = word2)

        viewModelScope.launch {
            updateWordPairUseCase(newWordPair)
        }
        clearWordPair()
    }

    fun getTranslation(fromLang: String? = frLang, toLang: String? = tLang, wordToTranslate: String = word1) {
        Log.d(TAG, "getTranslation: ")
        if (fromLang != null && toLang != null) {
            val translationState =  translationRepo.getTranslation(fromLang, toLang, wordToTranslate)
            when (translationState) {
                is TranslationState.Loading -> translationLoading = true
                is TranslationState.Success<*> -> word2 = translationState.content as String
                is TranslationState.Error -> updateToastMessage(translationState.errorMessage ?: "Error")
            }
        }
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

    private fun updateToastMessage(message: String) {
        toastMessage.value = message
    }

    fun setCurrentWordPair(wordPair: WordPair?) {
        currentWordPair = wordPair
//        word1 = wordPair?.word1 ?: ""
//        word2 = wordPair?.word2 ?: ""
    }

    fun setCurrentStackId(stackId: Long) {
        currentStackId = stackId
        viewModelScope.launch(Dispatchers.IO) {
            setLanguages(stackId)
        }
    }

    private fun setLanguages(stackId: Long? = currentStackId) {
        stackId?.let{
            getStackUseCase(currentStackId!!).map { state ->
                if (state is LoadingState.Collected<MemoStack>) {
                    frLang = state.content.fromLanguage?.codeName
                    tLang = state.content.toLanguage?.codeName
                }
            }
        }
    }


    fun clearWordPair() {
        currentWordPair = null
        word1 = ""
        word2 = ""
    }
}