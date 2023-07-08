package com.example.android.memoization.ui.features.addnewpair

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.GetWordPairLoadingStateUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.NewPairNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.data.TranslationState
import ru.vasilisasycheva.translation.domain.TranslationRepo
import javax.inject.Inject

@HiltViewModel
class AddNewPairViewModel @Inject constructor(
    private val repo: WordPairRepository,
    private val translationRepo: TranslationRepo,
    val getWordPairLoadingState: GetWordPairLoadingStateUseCase,
    val getStackUseCase: GetStackUseCase
) : BaseViewModel<LoadingState<WordPair>, NewPairNavArgs>() {

    var word1 = Empty_string
    var word2 = Empty_string
    var translationLoading = false

    private var currentWpId: Long? = null
    private var currentWordPair: WordPair? = null
    private var currentStackId: Long? = null
    private var fromLanguage: String? = null
    private var toLanguage: String? = null
    private var editMode: Boolean = false
    private val TAG = "AddNewPairViewModel"

    fun onTranslate(
        wordToTranslate: String = word1
    ) {
        if (fromLanguage != null && toLanguage != null) {
            val translationState = translationRepo.getTranslation(fromLanguage!!, toLanguage!!, wordToTranslate)
            when (translationState) {
                is TranslationState.Loading -> translationLoading = true
                is TranslationState.Success<*> -> word2 = translationState.content as String
                is TranslationState.Error -> updateToastMessage(
                    translationState.errorMessage ?: R.string.translation_error
                )
            }
        }
    }

    fun onConfirm() {
        val wordPairToSubmit = composeWordPairFromWords(word1, word2)
        viewModelScope.launch {
            if (editMode) repo.updateWordPairInDb(wordPairToSubmit)
            else repo.insertWordPair(wordPairToSubmit)
        }
        clearWordPair()
    }

    override fun setArgs(args: NewPairNavArgs?) {
        args?.let {
            editMode = args.editMode
            when (args) {
                is NewPairNavArgs.NewWordPair -> {
                    setCurrentStackId(args.stackId)
                }

                is NewPairNavArgs.EditPair -> {
                    currentWpId = args.wordPairId
                }
            }
        } ?: updateToastMessage(R.string.someting_went_wrong)

    }

    override fun getDataToDisplay(): Flow<LoadingState<WordPair>> {
        return if (currentWpId != null) getWordPairLoadingState(currentWpId!!).map {
            if (it is LoadingState.Collected<WordPair>){
                currentWordPair = it.content
                currentStackId = currentWordPair!!.stackId
                setLanguages(currentStackId)
            }
            it
        }
        else emptyFlow()
    }

    override fun onBackPressed(navController: NavController) {
        navController.popBackStack()
        clearWordPair()
    }

    private fun composeWordPairFromWords(word1: String, word2: String): WordPair {
        return currentWordPair?.copy(
            word1 = word1,
            word2 = word2
        )
            ?: WordPair(
                stackId = currentStackId ?: Default_folder_ID,
                word1 = word1,
                word2 = word2
            )
    }

    private fun setCurrentStackId(stackId: Long) {
        currentStackId = stackId
        viewModelScope.launch(Dispatchers.IO) {
            setLanguages(stackId)
        }
    }

    private fun setLanguages(stackId: Long? = currentStackId) {
        stackId?.let {
            getStackUseCase(currentStackId!!).map { state ->
                if (state is LoadingState.Collected<MemoStack>) {
                    fromLanguage = state.content.fromLanguage?.codeName
                    toLanguage = state.content.toLanguage?.codeName
                }
            }
        }
    }

    private fun clearWordPair() {
        currentWordPair = null
        word1 = Empty_string
        word2 = Empty_string
    }


}