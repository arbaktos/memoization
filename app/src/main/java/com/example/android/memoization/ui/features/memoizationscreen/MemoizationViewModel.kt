package com.example.android.memoization.ui.features.memoizationscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.LearningSettingsRepository
import com.example.android.memoization.data.repository.SideRepository
import com.example.android.memoization.domain.scheduler.Rating
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.domain.session.dueSessionSides
import com.example.android.memoization.domain.session.dueSideCount
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
    private val sideRepository: SideRepository,
    getStackUseCase: GetStackUseCase,
    learningSettings: LearningSettingsRepository,
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
            val practice = learningSettings.practiceSides.first()
            val batch = stack.dueSessionSides(practice)
            val waiting = stack.dueSideCount(practice) - batch.size
            session = MemorizationSession(batch, waiting = waiting)
                .also { _state.value = it.state() }
        }
    }

    fun onRate(rating: Rating) {
        val outcome = session?.rate(rating, System.currentTimeMillis()) ?: return
        outcome.toPersist?.let { side ->
            viewModelScope.launch { sideRepository.updateSide(side) }
        }
        _state.value = outcome.state
    }

    companion object {
        // Matches MemorizationDestination.id
        private const val STACK_ID_ARG = "id"
        private const val NO_STACK_ID_PASSED = -1L
    }
}
