package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Composable
fun AddStack(
    viewModel: FolderViewModel,
    listState: LazyListState,
    position: Int,
) {

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") }
    var isChosen by remember { mutableStateOf(text.isNotBlank()) }
    val scope = rememberCoroutineScope()


    val onClick: () -> Unit = {
        onTextChange("")
        viewModel.addStack(stack = MemoStack(name = text))
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    AddStackTextField(
        text = text,
        onTextChange = {
            onTextChange(it)
            isChosen = true
        },
        label = stringResource(R.string.add_new_stack),
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
                        listState.animateScrollToItem(position)
//                        listState.animateScrollToItem(position)
                    }
                }
            }
    )
}