package com.example.android.memoization.ui.features.memoizationscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.Rating
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoizationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WordPairRepository,
    getStackUseCase: GetStackUseCase
) : ViewModel() {

    private val stackId: Long = savedStateHandle.get<Long>(STACK_ID_ARG) ?: NO_STACK_ID_PASSED

    private var session: MemorizationSession? = null

    /** null while the stack is loading. */
    private val _state = MutableStateFlow<MemorizationSession.State?>(null)
    val state: StateFlow<MemorizationSession.State?> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // One snapshot: the session owns the queue from here on, so the database
            // re-emitting after each rating must not reshuffle or shrink it.
            val stack = getStackUseCase(stackId)
                .filterIsInstance<LoadingState.Collected<MemoStack>>()
                .first()
                .content
            session = MemorizationSession(stack.dueWords()).also { _state.value = it.state() }
        }
    }

    fun onRate(rating: Rating) {
        val outcome = session?.rate(rating, System.currentTimeMillis()) ?: return
        outcome.toPersist?.let { rated ->
            viewModelScope.launch { repository.updateWordPairInDb(rated) }
        }
        _state.value = outcome.state
    }

    companion object {
        // Matches MemorizationDestination.id
        private const val STACK_ID_ARG = "id"
        private const val NO_STACK_ID_PASSED = -1L
    }
}
