package com.example.android.memoization.ui.features.memoizationscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.LearningSettingsRepository
import com.example.android.memoization.data.repository.ReviewLogRepository
import com.example.android.memoization.data.repository.SideRepository
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.di.ApplicationScope
import com.example.android.memoization.domain.scheduler.Rating
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.domain.session.SessionEnding
import com.example.android.memoization.domain.session.dueSessionSides
import com.example.android.memoization.domain.session.stamina
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
    private val reviewLog: ReviewLogRepository,
    private val stackRepository: StackRepository,
    @ApplicationScope private val appScope: CoroutineScope,
    getStackUseCase: GetStackUseCase,
    learningSettings: LearningSettingsRepository,
) : ViewModel() {

    private val stackId: Long = savedStateHandle.get<Long>(STACK_ID_ARG) ?: NO_STACK_ID_PASSED

    private var session: MemorizationSession? = null

    /** The log row of this sitting; answers are written against it. */
    private var sessionId: Long = 0

    /** When the side now on screen appeared, so an answer can record how long it took. */
    private var shownAt: Long = 0

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
            val now = System.currentTimeMillis()
            // The session row is written before the first side is shown, so every answer has
            // a session to belong to; an empty sitting is recorded too, and finished at once.
            sessionId = reviewLog.startSession(stackId, batch.size, now)
            shownAt = now
            val started = MemorizationSession(batch, offerStopFrom = stack.stamina.offerFrom)
                .also { session = it; _state.value = it.state() }
            if (batch.isEmpty()) reviewLog.finishSession(sessionId, SessionEnding.DRAINED, started.state(), now)
        }
    }

    fun onRate(rating: Rating) {
        val now = System.currentTimeMillis()
        val outcome = session?.rate(rating, now) ?: return
        val shownMs = now - shownAt
        shownAt = now
        val id = sessionId
        viewModelScope.launch {
            outcome.toPersist?.let { side -> sideRepository.updateSide(side) }
            reviewLog.logAnswer(id, outcome.answer, shownMs, now)
            if (outcome.state.isFinished) reviewLog.finishSession(id, SessionEnding.DRAINED, outcome.state, now)
        }
        _state.value = outcome.state
    }

    /**
     * Enough for today: the session ends where it is. The stack remembers how far the learner
     * got, so next time the offer comes a little before that point; see Stamina.
     */
    fun onStop() {
        val s = session ?: return
        if (!s.state().stopOffered) return
        val now = System.currentTimeMillis()
        val answered = s.answered
        val id = sessionId
        val stopped = s.stop()
        viewModelScope.launch {
            reviewLog.finishSession(id, SessionEnding.ENOUGH, stopped, now)
            stackRepository.recordStoppedAt(stackId, answered)
        }
        _state.value = stopped
    }

    /**
     * Leaving the screen ends the session too: it is closed in the log with whatever was left
     * in the queue. The ViewModel's own scope is already cancelled here, so the write goes to
     * a scope that outlives the screen.
     */
    override fun onCleared() {
        val s = session ?: return
        val state = s.state()
        if (state.isFinished) return
        val id = sessionId
        val now = System.currentTimeMillis()
        appScope.launch { reviewLog.finishSession(id, SessionEnding.LEFT, state, now) }
    }

    companion object {
        // Matches MemorizationDestination.id
        private const val STACK_ID_ARG = "id"
        private const val NO_STACK_ID_PASSED = -1L
    }
}
