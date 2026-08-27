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
    getStacksWithWordsUseCase: GetStacksWithWordsUseCase,
) : BaseViewModel<LoadingState<List<MemoStack>>>() {

    /** What the other app shared, already tidied by [SharedWord]. */
    val word: String = savedStateHandle[WORD] ?: Empty_string

    // Built once: collectAsState keys on the flow instance, so a new one per
    // recomposition would restart collection and recompose forever.
    private val stacksWithWords: Flow<LoadingState<List<MemoStack>>> by lazy {
        getStacksWithWordsUseCase()
    }

    override fun getDataToDisplay(): Flow<LoadingState<List<MemoStack>>> = stacksWithWords

    fun onStackChosen(navController: NavController, stack: MemoStack) {
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
            navController.navigateFromShareToNewPair(
                stackId = stackId,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage,
                word = word,
            )
        }
    }
}
