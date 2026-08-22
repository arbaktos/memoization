package com.example.android.memoization.ui.composables.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.memoButtonColors

/**
 * Shown when the session's queue is empty. "Done for today" only if nothing else in the stack
 * is due; otherwise it says how many sides are waiting for another sitting.
 */
@Composable
fun StackCompleteDialog(
    waiting: Int,
    onClick: () -> Unit,
) {
    val title = if (waiting == 0) {
        stringResource(R.string.stack_complete)
    } else {
        pluralStringResource(R.plurals.session_complete_waiting, waiting, waiting)
    }
    AlertDialog(
        onDismissRequest = onClick,
        confirmButton = {
            Button(
                onClick = onClick,
                content = { Text(stringResource(R.string.ok)) },
                colors = memoButtonColors(),
                elevation = null
            )
        },
        title = { Text(title) },
    )
}
