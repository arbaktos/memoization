package com.example.android.memoization.ui.features.folderscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.usecases.GetStacksWithWordsUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.ui.navigateToMemorization
import com.example.android.memoization.ui.navigateToNewPair
import com.example.android.memoization.ui.navigateToStackScreen
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.workers.StackDeletionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class FolderViewModel @Inject constructor(
    private val workManager: WorkManager,
    private var stackRepository: StackRepository,
    val getStacksWithWordsUseCase: GetStacksWithWordsUseCase,
    val updateStackUseCase: UpdateStackUseCase,
) : BaseViewModel<LoadingState<List<MemoStack>>>() {

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

    fun deleteStackWithDelay(stack: MemoStack) {
        val inputData = Data.Builder()
            .putLong(STACK_ID, stack.stackId).build()
        val workDelayDeleteRequest = OneTimeWorkRequestBuilder<StackDeletionWorker>()
            .addTag("${stack.stackId}")
            .setInputData(inputData)
            .setInitialDelay(3, TimeUnit.SECONDS) //TODO add delay to delete after snackbar
            .build()
        workManager.enqueue(workDelayDeleteRequest)
    }

    fun cancelStackDeletion(stack: MemoStack) {
        workManager.cancelAllWorkByTag(stack.stackId.toString())
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

    fun onPlayWords(navController: NavController, stackId: Long) {
        navController.navigateToMemorization(stackId)
    }

    fun onPin(stack: MemoStack) {
        if (stack.pinnedTime == null) updateStack(stack.copy(pinnedTime = System.currentTimeMillis()))
        else updateStack(stack.copy(pinnedTime = null))
    }
}