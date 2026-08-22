package com.example.android.memoization.ui.features.addnewpair

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.R
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.GetWordPairLoadingStateUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.FROM_LANGUAGE
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.TO_LANGUAGE
import com.example.android.memoization.utils.WORD_PAIR_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.data.TranslationState
import ru.vasilisasycheva.translation.domain.TranslationRepo
import javax.inject.Inject

@HiltViewModel
class AddNewPairViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: WordPairRepository,
    private val translationRepo: TranslationRepo,
    val getWordPairLoadingState: GetWordPairLoadingStateUseCase,
    val getStackUseCase: GetStackUseCase
) : BaseViewModel<LoadingState<WordPair>>() {

    var word1 = Empty_string
    var word2 = Empty_string

    private val _translation = MutableStateFlow<TranslationUiState>(TranslationUiState.Idle)
    val translation: StateFlow<TranslationUiState> = _translation.asStateFlow()

    // EditPairDestination carries a word pair id, NewPairDestination carries a stack id
    private val currentWpId: Long? = savedStateHandle[WORD_PAIR_ID]
    private val fromLanguage: String? = savedStateHandle[FROM_LANGUAGE]
    private val toLanguage: String? = savedStateHandle[TO_LANGUAGE]

    /** Keyboard language hints for the two fields; null when the stack names no language. */
    val wordKeyboardLocale: String? = keyboardLocaleTag(fromLanguage)
    val meaningKeyboardLocale: String? = keyboardLocaleTag(toLanguage)
    private val editMode: Boolean = currentWpId != null

    private var currentWordPair: WordPair? = null
    private var currentStackId: Long? = savedStateHandle[STACK_ID]
    private val TAG = "AddNewPairViewModel"

    // Built once: collectAsStateWithLifecycle keys on the flow instance, so a new
    // one per recomposition would restart collection and recompose forever.
    private val wordPairToEdit: Flow<LoadingState<WordPair>> by lazy {
        val wordPairId = currentWpId ?: return@lazy emptyFlow()
        getWordPairLoadingState(wordPairId).map {
            if (it is LoadingState.Collected<WordPair>) {
                currentWordPair = it.content
                currentStackId = currentWordPair!!.parentStackId
            }
            it
        }
    }

    init {
        if (currentWpId == null && currentStackId == null) {
            updateToastMessage(R.string.someting_went_wrong)
        }
    }

    /**
     * The translated word is published as state instead of being written onto word2,
     * because the text field owns its own text and never watched that field.
     */
    fun onTranslate(wordToTranslate: String = word1) {
        val from = fromLanguage
        val to = toLanguage
        if (from.isNullOrBlank() || to.isNullOrBlank()) return
        if (wordToTranslate.isBlank()) return

        viewModelScope.launch {
            _translation.value = TranslationUiState.Translating
            when (val state = translationRepo.getTranslation(from, to, wordToTranslate)) {
                is TranslationState.Success<*> -> {
                    val translated = state.content as? String
                    if (translated.isNullOrBlank()) {
                        _translation.value = TranslationUiState.Idle
                        updateToastMessage(R.string.translation_error)
                    } else {
                        _translation.value = TranslationUiState.Translated(translated)
                    }
                }

                is TranslationState.Error -> {
                    _translation.value = TranslationUiState.Idle
                    updateToastMessage(state.errorMessage ?: R.string.translation_error)
                }

                is TranslationState.Loading -> _translation.value = TranslationUiState.Translating
            }
        }
    }

    /** Called once the field has taken the translation, so it is not re-applied on rotation. */
    fun onTranslationApplied() {
        _translation.value = TranslationUiState.Idle
    }

    fun onConfirm() {
        val wordPairToSubmit = composeWordPairFromWords(word1, word2)
        viewModelScope.launch {
            if (editMode) repo.updateWordPairInDb(wordPairToSubmit)
            else repo.insertWordPair(wordPairToSubmit)
        }
        clearWordPair()
    }

    /** Both languages have to be set on the stack before translating means anything. */
    fun needsTranslation(): Boolean {
        return !fromLanguage.isNullOrBlank() && !toLanguage.isNullOrBlank()
    }

    override fun getDataToDisplay(): Flow<LoadingState<WordPair>> {
        return wordPairToEdit
    }

//    override fun onBackPressed(navController: NavController) {
//        navController.popBackStack()
//        clearWordPair()
//    }

    private fun composeWordPairFromWords(word1: String, word2: String): WordPair {
        return currentWordPair?.copy(
            word1 = word1,
            word2 = word2
        )
            ?: WordPair(
                parentStackId = currentStackId ?: Default_folder_ID,
                word1 = word1,
                word2 = word2
            )
    }

    private fun clearWordPair() {
        currentWordPair = null
        word1 = Empty_string
        word2 = Empty_string
    }
}

sealed interface TranslationUiState {
    data object Idle : TranslationUiState
    data object Translating : TranslationUiState
    data class Translated(val word: String) : TranslationUiState
}
