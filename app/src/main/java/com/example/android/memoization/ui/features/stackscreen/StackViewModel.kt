package com.example.android.memoization.ui.features.stackscreen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
//import com.example.android.memoization.data.SessionState.currentStack
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.domain.usecases.DeleteWordPairUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
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
    private val getStackUseCase: GetStackUseCase,
    private val deleteWordPairUseCase: DeleteWordPairUseCase
) : BaseViewModel<LoadingState<MemoStack>, Long>() {

    private var stackId: Long? = null

    val showEditStackDialog = MutableStateFlow(false)


    fun onPin() {
//       TODO  updateStackInDb()
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

    override fun getDataToDisplay(): Flow<LoadingState<MemoStack>> {
        return stackId?.let {  getStackUseCase(stackId!!) }
            ?: emptyFlow()
    }

    override fun onBackPressed(navController: NavController) {
        navController.popBackStack()
    }

    override fun setArgs(args: Long?) {
        stackId = args
    }
}
