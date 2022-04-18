package com.example.android.memoization.ui.composables.components

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.model.Stack
import com.example.android.memoization.ui.theme.MemoButtonColors
import com.example.android.memoization.ui.theme.MemoTextFieldColors
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.utils.NavScreens

@Composable
fun StackCompleteDialog(
    viewModel: FolderViewModel,
    navController: NavController,
    onClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClick ,
        confirmButton = {
            Button(
                onClick = {
                    viewModel.getFoldersWithStackFromDb()
                    onClick()
                    navController.navigate(NavScreens.Folders.route)
                },
                content = { Text(stringResource(R.string.ok)) },
                colors = MemoButtonColors(),
                elevation = null
            )
        },

        title = { Text(stringResource(R.string.stack_complete)) },
    )
}