package com.example.android.memoization.ui.features.stackscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.repository.LearningSettingsRepository
import com.example.android.memoization.domain.usecases.HideWordPairUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStackUseCase: GetStackUseCase,
    private val updateStackUseCase: UpdateStackUseCase,
    private val hideWordPairUseCase: HideWordPairUseCase,
    learningSettings: LearningSettingsRepository,
) : BaseViewModel<LoadingState<MemoStack>>() {

    companion object {
        private const val NO_STACK_ID_PASSED = -1L
        private const val STOP_COLLECTING_AFTER_MS = 5_000L
    }

    private val stackId: Long = savedStateHandle.get<Long>("id") ?: NO_STACK_ID_PASSED

    val showEditStackDialog = MutableStateFlow(false)

    val practiceSides = learningSettings.practiceSides.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(STOP_COLLECTING_AFTER_MS), PracticeSides.DEFAULT
    )

    // Built once: collectAsStateWithLifecycle keys on the flow instance, so a new
    // one per recomposition would restart collection and recompose forever.
    private val stackWithWords: Flow<LoadingState<MemoStack>> by lazy { getStackUseCase(stackId) }

    fun updateStackInDb(stack: MemoStack) {
        viewModelScope.launch {
            updateStackUseCase(stack)
        }
    }


    /** Only the id is needed to bring it back: deleting hides the row, it does not remove it. */
    private var lastDeletedId: Long? = null

    fun deletePair(wordPair: WordPair) {
        lastDeletedId = wordPair.wordPairId
        viewModelScope.launch {
            hideWordPairUseCase(wordPair.wordPairId)
        }
    }

    fun undoDelete() {
        val restored = lastDeletedId ?: return
        lastDeletedId = null
        viewModelScope.launch {
            hideWordPairUseCase(restored, hidden = false)
        }
    }

    fun showEditStackDialog(toShow: Boolean) {
        showEditStackDialog.value = toShow
        Log.d(TAG, "showEditStackDialog: to Show $toShow")
    }

    override fun getDataToDisplay(): Flow<LoadingState<MemoStack>> {
        return stackWithWords
    }

}