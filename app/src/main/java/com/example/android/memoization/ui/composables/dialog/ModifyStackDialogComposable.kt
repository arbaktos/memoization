package com.example.android.memoization.ui.composables.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.features.language.LanguagesViewModel
import com.example.android.memoization.ui.theme.MemoTextFieldColors
import com.example.android.memoization.utils.LoadingState

@Composable
fun AddStackDialog(
    modifier: Modifier = Modifier,
    onStackAdded: (stack: MemoStack) -> Unit,
    stack: MemoStack? = null,
    onDismiss: () -> Unit,
) {
    ModifyStackDialog(
        onDismiss = onDismiss,
        stack = stack,
        onConfirm = onStackAdded,
        modifier = modifier
    )
}

@Composable
fun EditStackDialog(
    stack: MemoStack,
    onStackUpdated: (stack: MemoStack) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModifyStackDialog(
        onDismiss = onDismiss,
        stack = stack,
        onConfirm = onStackUpdated,
        modifier = modifier
    )
}

/** Which of the two language slots the picker is currently open for. */
private enum class LanguageSlot { From, To }

@Composable
fun ModifyStackDialog(
    onDismiss: () -> Unit,
    onConfirm: (stack: MemoStack) -> Unit,
    stack: MemoStack? = null,
    modifier: Modifier = Modifier,
    languagesViewModel: LanguagesViewModel = hiltViewModel(),
) {
    var text by rememberSaveable { mutableStateOf(stack?.name ?: "") }
    var fromLanguage by rememberSaveable { mutableStateOf(stack?.fromLanguage) }
    var toLanguage by rememberSaveable { mutableStateOf(stack?.toLanguage) }
    var pickerFor by remember { mutableStateOf<LanguageSlot?>(null) }

    // The stack stores the api code; the learner should read "English", not "en_GB".
    val languages by languagesViewModel.languages.collectAsStateWithLifecycle()
    val nameOf: (String?) -> String? = { code ->
        code?.let {
            (languages as? LoadingState.Collected)?.content
                ?.firstOrNull { language -> language.full_code == it }
                ?.englishName ?: it
        }
    }

    OkCancelDialog(
        modifier = modifier.padding(16.dp),
        onDismissRequest = onDismiss,
        onConfirm = {
            val edited = stack?.copy(
                name = text,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage
            ) ?: MemoStack(
                name = text,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage
            )
            onConfirm(edited)
            onDismiss()
        },
        okEnabled = text.isNotEmpty(),
        onCancel = onDismiss
    ) {
        Text(text = stringResource(id = R.string.new_stack_name))
        TextField(
            value = text,
            onValueChange = { text = it },
            colors = MemoTextFieldColors(),
            textStyle = MaterialTheme.typography.body1,
            modifier = Modifier.border(
                border = BorderStroke(1.dp, color = Color.Gray),
                shape = RoundedCornerShape(4.dp)
            )
        )

        // Both languages set is what switches the translate button on when adding a word.
        LanguageRow(
            label = stringResource(R.string.label_from_language),
            value = nameOf(fromLanguage),
            onClick = { pickerFor = LanguageSlot.From }
        )
        LanguageRow(
            label = stringResource(R.string.label_to_language),
            value = nameOf(toLanguage),
            onClick = { pickerFor = LanguageSlot.To }
        )
    }

    pickerFor?.let { slot ->
        ChooseLanguageDialog(
            title = stringResource(
                if (slot == LanguageSlot.From) R.string.label_from_language
                else R.string.label_to_language
            ),
            onLanguagePicked = { language ->
                when (slot) {
                    LanguageSlot.From -> fromLanguage = language.full_code
                    LanguageSlot.To -> toLanguage = language.full_code
                }
                pickerFor = null
            },
            onDismissRequest = { pickerFor = null }
        )
    }
}

@Composable
private fun LanguageRow(label: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Text(
            text = value ?: stringResource(R.string.choose_language),
            color = if (value == null) Color.Gray else MaterialTheme.colors.primary
        )
    }
}
