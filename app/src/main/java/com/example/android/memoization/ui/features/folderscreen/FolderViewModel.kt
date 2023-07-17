package com.example.android.memoization.ui.features.folderscreen

import android.util.Log
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.work.*
import com.example.android.memoization.data.model.Folder
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.domain.usecases.AddStackUseCase
import com.example.android.memoization.domain.usecases.GetStacksWithWordsUseCase
import com.example.android.memoization.domain.usecases.UpdateStackUseCase
import com.example.android.memoization.ui.features.BaseViewModel
import com.example.android.memoization.utils.Default_folder_ID
import com.example.android.memoization.utils.Empty_folder_name
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.NewPairNavArgs
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.workers.StackDeletionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.api.LanguageItem
import ru.vasilisasycheva.translation.data.TranslationState
import ru.vasilisasycheva.translation.domain.TranslationRepo
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class FolderViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val languageRepo: TranslationRepo,
    val addStackUseCase: AddStackUseCase,
    val getStacksWithWordsUseCase: GetStacksWithWordsUseCase,
    val updateStackUseCase: UpdateStackUseCase,
) : BaseViewModel<LoadingState<List<MemoStack>>, Any>() {

    var languages: List<LanguageItem>? = null
    private var _showAddStackDialog: MutableLiveData<Boolean> = MutableLiveData(false)
    val showAddStackDialog: LiveData<Boolean> = _showAddStackDialog

    fun updateStack(stack: MemoStack) {
        viewModelScope.launch(Dispatchers.IO) {
            updateStackUseCase(stack)
        }
    }

    fun showAddSTackDialog(toShow: Boolean) {
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

    fun addStackToFolder(
        folder: Folder = Folder(Empty_folder_name, folderId = Default_folder_ID),
        stack: MemoStack
    ) {
        viewModelScope.launch {
            addStackUseCase(folder, stack)
        }
    }

    fun getLanguagesList(): List<LanguageItem>? {
        viewModelScope.launch {
            val response = languageRepo.getLanguages()
            when (response) {
                is TranslationState.Loading -> ShowLoadingLangs()
                is TranslationState.Error -> response.errorMessage?.let {
                    updateToastMessage(
                        response.errorMessage!!
                    )
                }

                is TranslationState.Success<*> -> languages = response.content as List<LanguageItem>
            }
        }
        return languages
    }

    fun onNavigateTosStack(navController: NavController, stackId: Long) {
        navController.navigate(FolderScreenFragmentDirections.toStackScreen(stackId))
    }

    fun onAddNewWord(navController: NavController, stackId: Long) {
        navController.navigate(
            FolderScreenFragmentDirections.toNewPairFragment(
                NewPairNavArgs.NewWordPair(stackId = stackId)
            )
        )
    }

    override fun getDataToDisplay(): Flow<LoadingState<List<MemoStack>>> {
        return getStacksWithWordsUseCase()
    }

    override fun onBackPressed(navController: NavController) {

    }

    override fun setArgs(args: Any?) {
    }

    fun ShowLoadingLangs() {}
    fun onPlayWords(navController: NavController, stackId: Long) {
        val action = FolderScreenFragmentDirections.toMemorization(stackId)
        navController.navigate(action)
    }

}
