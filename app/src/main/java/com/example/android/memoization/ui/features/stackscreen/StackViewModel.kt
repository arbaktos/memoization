package com.example.android.memoization.ui.features.stackscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.usecases.DeleteWordPairUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStackUseCase: GetStackUseCase,
    private val updateStackUseCase: UpdateStackUseCase,
    private val deleteWordPairUseCase: DeleteWordPairUseCase,
    private val wordPairRepository: WordPairRepository,
) : BaseViewModel<LoadingState<MemoStack>>() {

    companion object {
        private const val NO_STACK_ID_PASSED = -1L
    }

    private val stackId: Long = savedStateHandle.get<Long>("id") ?: NO_STACK_ID_PASSED

    val showEditStackDialog = MutableStateFlow(false)

    // Built once: collectAsStateWithLifecycle keys on the flow instance, so a new
    // one per recomposition would restart collection and recompose forever.
    private val stackWithWords: Flow<LoadingState<MemoStack>> by lazy { getStackUseCase(stackId) }

    fun updateStackInDb(stack: MemoStack) {
        viewModelScope.launch {
            updateStackUseCase(stack)
        }
    }


    /** Kept so the snackbar can put it back; the row carries its own id, so it returns intact. */
    private var lastDeleted: WordPair? = null

    fun deletePair(wordPair: WordPair) {
        lastDeleted = wordPair
        viewModelScope.launch {
            deleteWordPairUseCase(wordPair)
        }
    }

    fun undoDelete() {
        Log.d(TAG, "undoDelete: called, lastDeleted=${lastDeleted?.word1}")
        val restored = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            wordPairRepository.insertWordPair(restored)
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