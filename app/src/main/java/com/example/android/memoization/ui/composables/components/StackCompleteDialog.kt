package com.example.android.memoization.ui.composables.components

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoButtonColors
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import com.example.android.memoization.utils.NavScreens

@Composable
fun StackCompleteDialog(
    onClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClick ,
        confirmButton = {
            Button(
                onClick = onClick,
                content = { Text(stringResource(R.string.ok)) },
                colors = MemoButtonColors(),
                elevation = null
            )
        },

        title = { Text(stringResource(R.string.stack_complete)) },
    )
}