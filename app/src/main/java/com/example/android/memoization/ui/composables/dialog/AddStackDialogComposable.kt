package com.example.android.memoization.ui.composables.dialog

import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import com.example.android.memoization.ui.theme.MemoTextFieldColors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddStackAlertDialog(
    viewModel: FolderViewModel = hiltViewModel(),
    isEditMode: Boolean = false,
    stack: MemoStack? = null,
    onClick: () -> Unit,
) {
    Log.d(TAG, "AddStackAlertDialog: add stack alert dialog")
    var text by rememberSaveable { mutableStateOf(if (isEditMode) stack?.name ?: "" else "") }
    val onConfirm = {
        if (isEditMode) viewModel.updateStack(stack?.copy(name = text) ?: MemoStack(""))
        else viewModel.addStack(
            stack = MemoStack(text)
        )
    }
    OkCancelDialog(
        modifier = Modifier.padding(16.dp),
        onDismissRequest = onClick,
        onConfirm = {
            onClick()
            onConfirm()
        },
        okEnabled = text.isNotEmpty(),
        onCancel = onClick
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