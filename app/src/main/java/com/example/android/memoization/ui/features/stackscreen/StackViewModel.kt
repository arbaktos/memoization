package com.example.android.memoization.ui.features.stackscreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.repository.DuplicatesRepository
import com.example.android.memoization.data.repository.LearningSettingsRepository
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.duplicates.CopyGroup
import com.example.android.memoization.domain.duplicates.DuplicateReport
import com.example.android.memoization.domain.duplicates.Duplicates
import com.example.android.memoization.domain.duplicates.MergeProposal
import com.example.android.memoization.domain.usecases.HideWordPairUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStackUseCase: GetStackUseCase,
    private val updateStackUseCase: UpdateStackUseCase,
    private val hideWordPairUseCase: HideWordPairUseCase,
    private val duplicatesRepository: DuplicatesRepository,
    private val stackRepository: StackRepository,
    learningSettings: LearningSettingsRepository,
) : BaseViewModel<LoadingState<MemoStack>>() {

    companion object {
        private const val NO_STACK_ID_PASSED = -1L
        private const val STOP_COLLECTING_AFTER_MS = 5_000L
    }

    private val stackId: Long = savedStateHandle.get<Long>("id") ?: NO_STACK_ID_PASSED

    val showEditStackDialog = MutableStateFlow(false)

    /** The search box's text, or null while the box is closed; kept here to survive rotation. */
    val searchQuery = MutableStateFlow<String?>(null)

    fun openSearch() {
        searchQuery.value = ""
    }

    fun closeSearch() {
        searchQuery.value = null
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

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

    fun togglePin(stack: MemoStack) =
        updateStackInDb(stack.copy(pinnedTime = if (stack.pinnedTime == null) System.currentTimeMillis() else null))

    /** Delete from the menu: hidden, never deleted - the stack list offers the undo. */
    fun hideStack(onHidden: () -> Unit) {
        viewModelScope.launch {
            stackRepository.setStackDeleted(stackId, deleted = true)
            onHidden()
        }
    }


    /**
     * Deleting hides the pairs, it does not remove them: the ids are all an undo needs, and the
     * pairs come back with their progress.
     */
    fun deletePairs(wordPairIds: Collection<Long>) {
        viewModelScope.launch { hideWordPairUseCase(wordPairIds) }
    }

    fun restorePairs(wordPairIds: Collection<Long>) {
        viewModelScope.launch { hideWordPairUseCase(wordPairIds, hidden = false) }
    }

    /** The pairs picked for deletion, or null outside selection mode; kept here to survive rotation. */
    val selection = MutableStateFlow<Set<Long>?>(null)

    fun startSelection(wordPairId: Long) {
        selection.value = setOf(wordPairId)
    }

    /** Taking the last pair out of the selection closes selection mode. */
    fun toggleSelected(wordPairId: Long) {
        val current = selection.value ?: return
        selection.value = (if (wordPairId in current) current - wordPairId else current + wordPairId).ifEmpty { null }
    }

    fun clearSelection() {
        selection.value = null
    }

    /** Hides the selected pairs, leaves selection mode and returns them for the undo. */
    fun deleteSelected(): Set<Long> {
        val deleted = selection.value.orEmpty()
        selection.value = null
        deletePairs(deleted)
        return deleted
    }

    /** Copies and merge proposals among the stack's visible pairs, recomputed on every change. */
    val duplicates: StateFlow<DuplicateReport> =
        combine(stackWithWords, duplicatesRepository.dismissedMatches) { state, dismissed ->
            if (state is LoadingState.Collected) Duplicates.find(state.content.words, dismissed) else DuplicateReport.NONE
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_COLLECTING_AFTER_MS), DuplicateReport.NONE)

    private val reviewing = MutableStateFlow(false)

    /** Cancel on the copies dialog moves on to the merges; it is not remembered beyond this review. */
    private val copiesSkipped = MutableStateFlow(false)

    /** A write is running; the dialog's buttons wait for it. */
    val duplicatesBusy = MutableStateFlow(false)

    /**
     * The dialog to show, or null: all copies at once first, then one merge proposal at a
     * time. Each answer changes the database, the report is recomputed, and the next step
     * follows from it; when nothing is left the review ends.
     */
    val reviewStep: StateFlow<ReviewStep?> =
        combine(reviewing, copiesSkipped, duplicates) { on, skipped, report ->
            when {
                !on -> null
                report.copies.isNotEmpty() && !skipped -> ReviewStep.Copies(report.copies)
                report.proposals.isNotEmpty() -> ReviewStep.Merge(report.proposals.first())
                else -> null
            }
        }.onEach { if (it == null) reviewing.value = false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_COLLECTING_AFTER_MS), null)

    fun startDuplicatesReview() {
        copiesSkipped.value = false
        reviewing.value = true
    }

    /** Tapping outside or Back: the review stops and nothing is remembered. */
    fun stopDuplicatesReview() {
        reviewing.value = false
    }

    fun removeCopies(groups: List<CopyGroup>) = write { duplicatesRepository.apply(groups.map { it.plan() }) }

    fun skipCopies() {
        copiesSkipped.value = true
    }

    fun merge(proposal: MergeProposal, word1: String, word2: String) =
        write { duplicatesRepository.apply(listOf(Duplicates.plan(proposal.pairs, word1, word2))) }

    /** Cancel on a merge: these pairs are not proposed together again. */
    fun cancelMerge(proposal: MergeProposal) = write { duplicatesRepository.dismiss(proposal.matches()) }

    private fun write(block: suspend () -> Unit) {
        if (duplicatesBusy.value) return
        duplicatesBusy.value = true
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "duplicates: write failed", e)
                reviewing.value = false
            } finally {
                duplicatesBusy.value = false
            }
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

sealed interface ReviewStep {
    data class Copies(val groups: List<CopyGroup>) : ReviewStep
    data class Merge(val proposal: MergeProposal) : ReviewStep
}