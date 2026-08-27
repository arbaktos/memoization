package com.example.android.memoization.ui.features.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.usecases.GetStacksWithWordsUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.ui.navigateFromShareToNewPair
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.NO_STACK_ID
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.WORD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A word shared in from another app is waiting for a stack to go into. The library is the whole
 * screen: pick a stack and the pair opens with the word in it, or make a stack for it first.
 */
@HiltViewModel
class ShareWordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stackRepository: StackRepository,
    private val stackShortcuts: StackShortcuts,
    getStacksWithWordsUseCase: GetStacksWithWordsUseCase,
) : BaseViewModel<LoadingState<List<MemoStack>>>() {

    /** What the other app shared, already tidied by [SharedWord]. */
    val word: String = savedStateHandle[WORD] ?: Empty_string

    /**
     * The stack the share sheet already named, when the learner shared to the stack itself
     * rather than to the app; the screen then passes straight through to the new pair.
     */
    val chosenStackId: Long? =
        savedStateHandle.get<Long>(STACK_ID)?.takeIf { it != NO_STACK_ID }

    // Built once: collectAsState keys on the flow instance, so a new one per
    // recomposition would restart collection and recompose forever.
    private val stacksWithWords: Flow<LoadingState<List<MemoStack>>> by lazy {
        getStacksWithWordsUseCase()
    }

    override fun getDataToDisplay(): Flow<LoadingState<List<MemoStack>>> = stacksWithWords

    fun onStackChosen(navController: NavController, stack: MemoStack) {
        // What the system ranks the stacks in the share sheet by: where words actually go.
        stackShortcuts.reportUsed(stack.stackId)
        navController.navigateFromShareToNewPair(
            stackId = stack.stackId,
            fromLanguage = stack.fromLanguage,
            toLanguage = stack.toLanguage,
            word = word,
        )
    }

    /** No stack to put the word in yet: the one just made takes it. */
    fun onStackCreated(navController: NavController, stack: MemoStack) {
        viewModelScope.launch {
            val stackId = stackRepository.insertStack(StackEntity.create(stack))
            stackShortcuts.reportUsed(stackId)
            navController.navigateFromShareToNewPair(
                stackId = stackId,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage,
                word = word,
            )
        }
    }
}
