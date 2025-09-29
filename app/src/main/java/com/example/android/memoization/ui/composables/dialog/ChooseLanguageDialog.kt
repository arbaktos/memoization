package com.example.android.memoization.ui.composables.dialog

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun ChooseLanguageDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit,

) {
    OkCancelDialog(
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        onCancel = onCancel) {
        LazyColumn {

        }
    }
}