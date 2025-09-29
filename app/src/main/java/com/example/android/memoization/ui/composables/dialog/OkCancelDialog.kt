package com.example.android.memoization.ui.composables.dialog

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.android.memoization.ui.composables.buttons.CancelButton
import com.example.android.memoization.ui.composables.buttons.OkButton

@Composable
fun OkCancelDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    okEnabled: Boolean = true,
    onCancel: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.padding(16.dp), shape = RoundedCornerShape(4.dp),
            elevation = 4.dp,
        ) {
            Column {
                Column (content = content, modifier = Modifier.fillMaxWidth())
                Row {
                    Log.d(TAG, "OkCancelDialog: show ok cancel row")
                    CancelButton(onClick = onCancel)
                    OkButton(onClick = onConfirm, enabled = okEnabled)
                }
            }
        }
    }
}

const val TAG = "OkCancelDialog"