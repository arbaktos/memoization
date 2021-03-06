package com.example.android.memoization.ui.composables.components

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android.memoization.R
import com.example.android.memoization.model.Stack
import com.example.android.memoization.ui.theme.MemoButtonColors
import com.example.android.memoization.ui.theme.MemoTextFieldColors
import com.example.android.memoization.ui.viewmodel.FolderViewModel

@Composable
fun AddStackAlerDialog(viewModel: FolderViewModel, onClick: () -> Unit) {
    val appState = viewModel.publicAppState
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(
                onClick = {
                    onClick()
                    viewModel.addStackToFolder(
                        folder = appState.value.currentFolder,
                        stack = Stack(text)
                    )
                },
                enabled = text.isNotEmpty(),
                content = { Text(stringResource(R.string.ok)) },
                colors = MemoButtonColors(),
                elevation = null
            )
        },
        dismissButton = {
            Button(
                onClick = { onClick() },
                content = { Text(stringResource(R.string.cancel)) },
                colors = MemoButtonColors(),
                elevation = null
            )
        },
        title = { Text(stringResource(R.string.new_stack_name)) },
        text = {
            TextField(
                value = text, onValueChange = { text = it },
                colors = MemoTextFieldColors(),
                textStyle = MaterialTheme.typography.h5
            )
        }
    )
}
