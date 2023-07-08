package com.example.android.memoization.ui.features.memoizationscreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.repository.WordPairRepository
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MemoizationViewModel @Inject constructor(
    val repository: WordPairRepository,
    val getStackUseCase: GetStackUseCase
) : ViewModel() {

    var clicked = false

    private fun updateWordPairDateInDb(wordPair: WordPair?) {
        wordPair?.let {
            viewModelScope.launch {
                repository.updateWordPairInDb(wordPair.copy(lastRep = Date(System.currentTimeMillis())))
            }
        }
    }

    fun onStackIdReceived(stackId: Long) = getStackUseCase(stackId).transform { state ->
        when(state) {
            is LoadingState.Collected -> {
                emit(state.content.prepareStack().words.filter { wordPair ->
                    Log.d(TAG, "onStackIdReceived: wordPair $wordPair")
                    wordPair.toLearn
                })
            }
            is LoadingState.Error -> { /*TODO*/ }
            is LoadingState.Loading -> { /*TODO*/ }
        }

    }

    fun onBottomButtonClick(wordPair: WordPair, icon: Icon) {
        when (icon) {
            Icon.Easy -> wordPair.harderLevel()
            Icon.Hard -> wordPair.easierLevel()
            Icon.Wrong -> wordPair.toLevel1()
        }
        updateWordPairDateInDb(wordPair)
        clicked = true
    }
}