package com.example.android.memoization.ui.features.folderscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.repository.LearningSettingsRepository
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.usecases.GetStacksWithWordsUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.ui.navigateToMemorization
import com.example.android.memoization.ui.navigateToNewPair
import com.example.android.memoization.ui.navigateToStackScreen
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import com.example.android.memoization.data.database.stackdb.toMemoStack
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FolderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private var stackRepository: StackRepository,
    val getStacksWithWordsUseCase: GetStacksWithWordsUseCase,
    val updateStackUseCase: UpdateStackUseCase,
    learningSettings: LearningSettingsRepository,
) : BaseViewModel<LoadingState<List<MemoStack>>>() {

    val practiceSides = learningSettings.practiceSides.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(STOP_COLLECTING_AFTER_MS), PracticeSides.DEFAULT
    )

    private var _showAddStackDialog: MutableLiveData<Boolean> = MutableLiveData(false)
    val showAddStackDialog: LiveData<Boolean> = _showAddStackDialog

    // Built once: collectAsState keys on the flow instance, so a new one per
    // recomposition would restart collection and recompose forever.
    private val stacksWithWords: Flow<LoadingState<List<MemoStack>>> by lazy {
        getStacksWithWordsUseCase()
    }

    fun updateStack(stack: MemoStack) {
        viewModelScope.launch(Dispatchers.IO) {
            updateStackUseCase(stack)
        }
    }

    fun showAddStackDialog(toShow: Boolean) {
        _showAddStackDialog.postValue(toShow)
    }

    /** Deleting a stack hides it; the words stay, so an undo or a restore costs nothing. */
    fun hideStack(stack: MemoStack) = unhideStack(stack.stackId, hidden = true)

    fun unhideStack(stack: MemoStack) = unhideStack(stack.stackId)

    /** The stacks the learner deleted, the latest first: the list's Deleted section. */
    val deletedStacks: StateFlow<List<MemoStack>> = stackRepository.getStacksWithWords()
        .map { stacks -> stacks.map { it.toMemoStack() }.filter { it.deletedAt != null }.sortedByDescending { it.deletedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_COLLECTING_AFTER_MS), emptyList())

    /** A stack deleted from its own screen, handed back here so the list can offer the undo. */
    val hiddenFromStackScreen: StateFlow<Long?> = savedStateHandle.getStateFlow(HIDDEN_STACK_ID, null)
    private val handle = savedStateHandle

    fun hiddenStackName(): String = handle.get<String>(HIDDEN_STACK_NAME).orEmpty()

    fun onHiddenStackShown() {
        handle[HIDDEN_STACK_ID] = null
    }

    fun unhideStack(stackId: Long, hidden: Boolean = false) {
        viewModelScope.launch { stackRepository.setStackDeleted(stackId, deleted = hidden) }
    }

    fun addStackAndOpenIt(stack: MemoStack, navController: NavController) {
        viewModelScope.launch {
            val stackId = stackRepository.insertStack(StackEntity.create(stack))
            navController.navigateToStackScreen(stackId)
        }
    }

    fun onNavigateToStack(navController: NavController, stackId: Long) {
        navController.navigateToStackScreen(stackId)
    }

    fun onAddNewWord(navController: NavController, stack: MemoStack) {
        navController.navigateToNewPair(
            stackId = stack.stackId,
            fromLanguage = stack.fromLanguage,
            toLanguage = stack.toLanguage
        )
    }

    override fun getDataToDisplay(): Flow<LoadingState<List<MemoStack>>> {
        return stacksWithWords
    }

    /** Study: what is due, or - with nothing due - every practised side ahead of time. */
    fun onPlayWords(navController: NavController, stackId: Long, ahead: Boolean) {
        navController.navigateToMemorization(stackId, ahead)
    }

    companion object {
        private const val STOP_COLLECTING_AFTER_MS = 5_000L
        /** Keys in the stack list's saved state, set by the stack screen as it closes. */
        const val HIDDEN_STACK_ID = "hiddenStackId"
        const val HIDDEN_STACK_NAME = "hiddenStackName"
    }

    fun onPin(stack: MemoStack) {
        if (stack.pinnedTime == null) updateStack(stack.copy(pinnedTime = System.currentTimeMillis()))
        else updateStack(stack.copy(pinnedTime = null))
    }
}