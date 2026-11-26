package com.example.android.memoization.ui.features.addnewpair

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.R
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.repository.WordDraft
import com.example.android.memoization.data.repository.WordDraftRepository
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.GetWordPairLoadingStateUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.FROM_LANGUAGE
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.TO_LANGUAGE
import com.example.android.memoization.utils.WORD
import com.example.android.memoization.utils.WORD_PAIR_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.data.TranslationState
import ru.vasilisasycheva.translation.domain.TranslationRepo
import javax.inject.Inject

/**
 * Adding a word, or editing one (design handoff "Добавление карточки", 11b). The two fields'
 * text lives here, so a translation or a draft can fill them and a rotation keeps them.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddNewPairViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: WordPairRepository,
    private val translationRepo: TranslationRepo,
    private val drafts: WordDraftRepository,
    private val getWordPairLoadingState: GetWordPairLoadingStateUseCase,
    getStackUseCase: GetStackUseCase,
) : BaseViewModel<LoadingState<WordPair>>() {

    // EditPairDestination carries a word pair id, NewPairDestination carries a stack id
    private val currentWpId: Long? = savedStateHandle[WORD_PAIR_ID]
    private val fromLanguage: String? = savedStateHandle[FROM_LANGUAGE]
    private val toLanguage: String? = savedStateHandle[TO_LANGUAGE]

    val editMode: Boolean = currentWpId != null

    /**
     * A word shared in from another app, already in the pair before the learner types: the
     * word field starts with it and the explanation field is the one waiting for an answer.
     */
    val sharedWord: String? = savedStateHandle[WORD]

    /** Drafts are for words typed here; an edit or a shared word starts from what it brings. */
    private val keepsDraft = !editMode && sharedWord == null

    /** Keyboard language hints for the two fields; null when the stack names no language. */
    val wordKeyboardLocale: String? = keyboardLocaleTag(fromLanguage)
    val meaningKeyboardLocale: String? = keyboardLocaleTag(toLanguage)

    /** "SR", "RU": in front of each field's label; null when the stack names no language. */
    val wordLanguage: String? = languageLabel(fromLanguage)
    val meaningLanguage: String? = languageLabel(toLanguage)

    private val _word = MutableStateFlow(sharedWord.orEmpty())
    val word: StateFlow<String> = _word.asStateFlow()

    private val _meaning = MutableStateFlow("")
    val meaning: StateFlow<String> = _meaning.asStateFlow()

    /** The meaning came from the translator and is untouched since; the field says so. */
    private val _autoTranslated = MutableStateFlow(false)
    val autoTranslated: StateFlow<Boolean> = _autoTranslated.asStateFlow()

    /** The fields hold what was left unsaved last time; the screen offers to clear it. */
    private val _fromDraft = MutableStateFlow(false)
    val fromDraft: StateFlow<Boolean> = _fromDraft.asStateFlow()

    private val _translating = MutableStateFlow(false)
    val translating: StateFlow<Boolean> = _translating.asStateFlow()

    val canSave: StateFlow<Boolean> = combine(_word, _meaning) { word, meaning ->
        word.isNotBlank() && meaning.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var currentWordPair: WordPair? = null
    private val TAG = "AddNewPairViewModel"

    /** Known at once when adding; when editing, once the pair is loaded. */
    private val stackId = MutableStateFlow<Long?>(savedStateHandle[STACK_ID])

    val stackName: StateFlow<String?> = stackId.filterNotNull()
        .flatMapLatest { getStackUseCase(it) }
        .map { (it as? LoadingState.Collected)?.content?.name }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        if (currentWpId == null && stackId.value == null) {
            updateToastMessage(R.string.someting_went_wrong)
        }
        viewModelScope.launch {
            if (currentWpId != null) {
                val pair = getWordPairLoadingState(currentWpId)
                    .filterIsInstance<LoadingState.Collected<WordPair>>().first().content
                currentWordPair = pair
                stackId.value = pair.parentStackId
                _word.value = pair.word1
                _meaning.value = pair.word2.orEmpty()
            } else if (keepsDraft) {
                val draft = stackId.value?.let { drafts.draft(it) } ?: return@launch
                // Typing already begun wins over the draft.
                if (_word.value.isEmpty() && _meaning.value.isEmpty()) {
                    _word.value = draft.word
                    _meaning.value = draft.meaning
                    _fromDraft.value = true
                }
            }
        }
    }

    fun onWordChange(word: String) {
        _word.value = word
    }

    fun onMeaningChange(meaning: String) {
        _meaning.value = meaning
        _autoTranslated.value = false
    }

    /** Both languages have to be set on the stack before translating means anything. */
    fun needsTranslation(): Boolean = !fromLanguage.isNullOrBlank() && !toLanguage.isNullOrBlank()

    fun onTranslate() {
        val from = fromLanguage
        val to = toLanguage
        val wordToTranslate = _word.value
        if (from.isNullOrBlank() || to.isNullOrBlank() || wordToTranslate.isBlank()) return

        viewModelScope.launch {
            _translating.value = true
            when (val state = translationRepo.getTranslation(from, to, wordToTranslate)) {
                is TranslationState.Success<*> -> {
                    val translated = state.content as? String
                    if (translated.isNullOrBlank()) {
                        updateToastMessage(R.string.translation_error)
                    } else {
                        _meaning.value = translated
                        _autoTranslated.value = true
                    }
                }
                is TranslationState.Error -> {
                    // The api's or the network's own words are English and technical; the
                    // learner reads ours, the log keeps theirs.
                    Log.w(TAG, "Translation failed: ${state.errorMessage}")
                    updateToastMessage(R.string.translation_error)
                }
                is TranslationState.Loading -> Unit
            }
            _translating.value = false
        }
    }

    /** Saves the pair; the caller waits for it before leaving the screen. */
    suspend fun save() {
        val pair = currentWordPair?.copy(word1 = _word.value, word2 = _meaning.value)
            ?: WordPair(parentStackId = stackId.value ?: Default_folder_ID, word1 = _word.value, word2 = _meaning.value)
        if (editMode) repo.updateWordPairInDb(pair) else repo.insertWordPair(pair)
        if (keepsDraft) stackId.value?.let { drafts.clear(it) }
    }

    /** Saves the pair and empties the fields for the next one. */
    suspend fun saveAndNext() {
        save()
        clearFields()
    }

    /** Leaving without saving: whatever is typed waits as a draft for the next visit. */
    suspend fun keepDraft() {
        if (!keepsDraft) return
        val id = stackId.value ?: return
        drafts.keep(id, WordDraft(_word.value, _meaning.value))
    }

    /** Throws away last time's draft, in the fields and in storage. */
    fun clearDraft() {
        clearFields()
        viewModelScope.launch { stackId.value?.let { drafts.clear(it) } }
    }

    private fun clearFields() {
        _word.value = ""
        _meaning.value = ""
        _autoTranslated.value = false
        _fromDraft.value = false
    }

    override fun getDataToDisplay(): Flow<LoadingState<WordPair>> = emptyFlow()
}
