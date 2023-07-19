package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.theme.MemoButtonColors
import com.example.android.memoization.ui.theme.MemoTextFieldColors
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddStackAlertDialog(
    viewModel: FolderViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    stack: MemoStack? = null,
    onClick: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(if (isEditMode) stack!!.name else "") }
    val onConfirm = {
        if (isEditMode) viewModel.updateStack(stack!!.copy(name = text))
        else viewModel.addStack(
            stack = MemoStack(text)
        )
    }
    Dialog(onDismissRequest = { onClick() }) {
        Card(
            shape = RoundedCornerShape(4.dp),
            elevation = 4.dp,

        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.new_stack_name)
                )
                TextField(
                    value = text, onValueChange = { text = it },
                    colors = MemoTextFieldColors(),
                    textStyle = MaterialTheme.typography.body1,
                    modifier = Modifier.border(
                        border = BorderStroke(1.dp, color = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    )
                )
                var langMenuVisible by remember { mutableStateOf(false) }
                Chip(
                    onClick = { langMenuVisible = !langMenuVisible },
                    border = BorderStroke(1.dp, if (langMenuVisible) Color.Black else Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    colors = ChipDefaults.chipColors(
                        backgroundColor = Color.Transparent)
                ) {
                    Text(text = stringResource(R.string.chip_language_stack),
                    color = if (langMenuVisible) Color.Black else Color.Gray)
                }
                Row() {
                    Column() {

                    }
                }
                Row() {
                    Button(
                        onClick = {
                            onClick()
                            onConfirm()
                        },
                        enabled = text.isNotEmpty(),
                        content = { Text(stringResource(R.string.ok)) },
                        colors = MemoButtonColors(),
                        elevation = null
                    )
                    Button(
                        onClick = { onClick() },
                        content = { Text(stringResource(R.string.cancel)) },
                        colors = MemoButtonColors(),
                        elevation = null
                    )
                }
            }
        }
    }

}
