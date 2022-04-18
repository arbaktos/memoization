package com.example.android.memoization.ui.composables.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.android.memoization.R
import com.example.android.memoization.model.Folder
import com.example.android.memoization.model.Stack
import com.example.android.memoization.ui.composables.AddStackTextField
import com.example.android.memoization.ui.composables.screens.TDEBUG
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Composable
fun AddStack(
    folder: Folder,
    viewModel: FolderViewModel,
    listState: LazyListState,
    scrollState: ScrollState,
    position: Int,
    modifier: Modifier = Modifier.onGloballyPositioned {
        val offset = it.size
        Log.d(TDEBUG, "Add stack modifier y: $it")
        val y = offset.height
    }
) {

    var y = 0

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") }
    var isChosen by remember { mutableStateOf(text.isNotBlank()) }
    val scope = rememberCoroutineScope()


    val onClick: () -> Unit = {
        onTextChange("")
        viewModel.addStackToFolder(folder = folder, stack = Stack(text))
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    AddStackTextField(
        text = text,
        onTextChange = {
            onTextChange(it)
            isChosen = true
        },
        label = stringResource(R.string.add_stack),
        onFinish = {
            onClick()
        },
        showTrailingIcon = isChosen,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusEvent { focusState ->

                if (focusState.isFocused) {
                    scope.launch {
//                        Log.d(TDEBUG, "Add stack y: $y")
                        scrollState.animateScrollTo(y)
//                        listState.animateScrollToItem(position)
                    }
                }
            }
    )
}