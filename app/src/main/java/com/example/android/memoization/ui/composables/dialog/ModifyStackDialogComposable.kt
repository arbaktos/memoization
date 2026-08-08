package com.example.android.memoization.ui.composables.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.theme.MemoTextFieldColors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddStackDialog(
//    viewModel: FolderViewModel = hiltViewModel(),
//    isEditMode: Boolean = false,
    modifier: Modifier = Modifier,
    onStackAdded: (stack: MemoStack) -> Unit,
    stack: MemoStack? = null,
    onDismiss: () -> Unit,
) {
    ModifyStackDialog(onDismiss = onDismiss,
        stack = stack,
        onConfirm = onStackAdded,
        modifier = modifier
    )
//    Log.d(TAG, "AddStackAlertDialog: add stack alert dialog")
//    val isEditMode = stack != null
//    var text by rememberSaveable { mutableStateOf(if (isEditMode) stack.name else "") }
//    val onConfirm = {
//        if (isEditMode) onStackUpdated(stack.copy(name = text))
//        else onStackAdded(MemoStack(text))
//    }
//    OkCancelDialog(
//        modifier = Modifier.padding(16.dp),
//        onDismissRequest = onDismiss,
//        onConfirm = {
//            onStackAdded(stack)
//            onDismiss()
//        },
//        okEnabled = text.isNotEmpty(),
//        onCancel = onDismiss
//    ) {
//
//        Text(
//            text = stringResource(id = R.string.new_stack_name)
//        )
//        TextField(
//            value = text, onValueChange = { text = it },
//            colors = MemoTextFieldColors(),
//            textStyle = MaterialTheme.typography.body1,
//            modifier = Modifier.border(
//                border = BorderStroke(1.dp, color = Color.Gray),
//                shape = RoundedCornerShape(4.dp)
//            )
//        )
//        var langMenuVisible by remember { mutableStateOf(false) }
//        var chooseLanguageDialogVisible by remember { mutableStateOf(false) }
//        Row {
//            Chip(
//                onClick = { langMenuVisible = !langMenuVisible },
//                border = BorderStroke(1.dp, if (langMenuVisible) Color.Black else Color.Gray),
//                shape = RoundedCornerShape(8.dp),
//                colors = ChipDefaults.chipColors(
//                    backgroundColor = Color.Transparent
//                )
//            ) {
//                Text(
//                    text = stringResource(R.string.chip_language_stack),
//                    color = if (langMenuVisible) Color.Black else Color.Gray
//                )
//            }
//            if (langMenuVisible) {
//                ChooseLanguageMenu {
//                    chooseLanguageDialogVisible = true
//                }
//            }
//        }
//        if (chooseLanguageDialogVisible) {
//            ChooseLanguageDialog(
//                onConfirm = { langMenuVisible = false },
//                onCancel = { langMenuVisible = false },
//                onDismissRequest = { langMenuVisible = false }
//            )
//        }
//
//    }
}

@Composable
fun EditStackDialog(
    stack: MemoStack,
    onStackUpdated: (stack: MemoStack) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModifyStackDialog(onDismiss = onDismiss,
        stack = stack,
        onConfirm = onStackUpdated,
        modifier = modifier
        )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModifyStackDialog(
    onDismiss: () -> Unit,
    onConfirm: (stack: MemoStack) -> Unit,
    stack: MemoStack? = null,
//    onStackUpdated: (stack: MemoStack) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditMode = stack != null
    var text by rememberSaveable { mutableStateOf(if (isEditMode) stack.name else "") }
//    val onConfirm = {
//        if (isEditMode) onStackUpdated(stack.copy(name = text))
//        else onConfirm(MemoStack(text))
//    }
    OkCancelDialog(
        modifier = modifier.padding(16.dp),
        onDismissRequest = onDismiss,
        onConfirm = {
            onConfirm(stack?.let { stack.copy(name = text) } ?: MemoStack(text))
            onDismiss()
        },
        okEnabled = text.isNotEmpty(),
        onCancel = onDismiss
    ) {

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
        var chooseLanguageDialogVisible by remember { mutableStateOf(false) }
        Row {
            Chip(
                onClick = { langMenuVisible = !langMenuVisible },
                border = BorderStroke(1.dp, if (langMenuVisible) Color.Black else Color.Gray),
                shape = RoundedCornerShape(8.dp),
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color.Transparent
                )
            ) {
                Text(
                    text = stringResource(R.string.chip_language_stack),
                    color = if (langMenuVisible) Color.Black else Color.Gray
                )
            }
            if (langMenuVisible) {
                ChooseLanguageMenu {
                    chooseLanguageDialogVisible = true
                }
            }
        }
        if (chooseLanguageDialogVisible) {
            ChooseLanguageDialog(
                onConfirm = { langMenuVisible = false },
                onCancel = { langMenuVisible = false },
                onDismissRequest = { langMenuVisible = false }
            )
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChooseLanguageMenu(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column {
        Chip(
            onClick = onClick,
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(8.dp),
            colors = ChipDefaults.chipColors(
                backgroundColor = Color.Transparent
            )
        ) {
            Text(
                text = stringResource(R.string.label_from_language)
            )
        }
        Chip(
            onClick = onClick,
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(8.dp),
            colors = ChipDefaults.chipColors(
                backgroundColor = Color.Transparent
            )
        ) {
            Text(
                text = stringResource(R.string.label_to_language)
            )
        }
    }
}