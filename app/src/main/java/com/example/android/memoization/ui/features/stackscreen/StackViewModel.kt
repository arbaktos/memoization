package com.example.android.memoization.ui.features.stackscreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
//import com.example.android.memoization.data.SessionState.currentStack
import com.example.android.memoization.domain.model.MemoStack
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.domain.usecases.DeleteWordPairUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.WP_ID
import com.example.android.memoization.utils.workers.WordPairInvisibleWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class StackViewModel @Inject constructor(
    private val updateStackUseCase: UpdateStackUseCase,
    private val getStackUseCase: GetStackUseCase,
    private val workManager: WorkManager,
    private val deleteWordPairUseCase: DeleteWordPairUseCase
) : ViewModel() {

    val showEditStackDialog = MutableStateFlow(false)
    fun cancelDelayDeletionWork(wordPair: WordPair) {
        workManager.cancelAllWorkByTag("${wordPair.wordPairId}")
    }


    fun delayDeletionWordPair(wordPair: WordPair) {
        val inputData = Data.Builder()
            .putLong(WP_ID, wordPair.wordPairId).build()
        val workDelayDeleteRequest = OneTimeWorkRequestBuilder<WordPairInvisibleWorker>()
            .addTag("${wordPair.wordPairId}")
            .setInputData(inputData)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(workDelayDeleteRequest)
    }

    fun onPin() {
//        updateStackInDb()
    }

    fun onStackIdReceived(stackId: Long): Flow<LoadingState<MemoStack>> {
        return getStackUseCase(stackId)
    }

    fun updateStackInDb(stack: MemoStack?) {
        viewModelScope.launch {
            stack?.let {
                updateStackUseCase(stack)
            }
        }
    }

    fun deleteWordPairFromDb(wordPair: WordPair) {
        viewModelScope.launch {
            deleteWordPairUseCase(wordPair)
        }
    }

    fun showEditStackDialog(toShow: Boolean) {
        showEditStackDialog.value = toShow
        Log.d(TAG, "showEditStackDialog: to Show $toShow")
    }
}
