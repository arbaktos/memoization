package com.example.android.memoization.ui.composables.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/** A dialog whose content owns its own actions - picking a row is the confirmation. */
@Composable
fun PlainDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.padding(16.dp),
            shape = RoundedCornerShape(4.dp),
            elevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}
