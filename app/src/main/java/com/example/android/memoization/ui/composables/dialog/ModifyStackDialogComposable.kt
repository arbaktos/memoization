package com.example.android.memoization.ui.composables.dialog

import com.example.android.memoization.ui.features.addnewpair.languageDisplayName
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.RadioButton
import androidx.compose.ui.semantics.Role
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.ui.features.settings.labelRes
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
    onImportFromAnki: ((AnkiImportOptions) -> Unit)? = null,
) {
    ModifyStackDialog(
        onDismiss = onDismiss,
        stack = stack,
        onConfirm = onStackAdded,
        modifier = modifier,
        onImportFromAnki = onImportFromAnki,
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
    onImportFromAnki: ((AnkiImportOptions) -> Unit)? = null,
    languagesViewModel: LanguagesViewModel = hiltViewModel(),
    stackDialogViewModel: StackDialogViewModel = hiltViewModel(),
) {
    var text by rememberSaveable { mutableStateOf(stack?.name ?: "") }
    // The stack's own Practice sides by name; null follows the library's.
    var practice by rememberSaveable { mutableStateOf(stack?.practiceSides) }
    var resetAnkiProgress by rememberSaveable { mutableStateOf(false) }
    val libraryPractice by stackDialogViewModel.libraryPractice.collectAsStateWithLifecycle()
    var fromLanguage by rememberSaveable { mutableStateOf(stack?.fromLanguage) }
    var toLanguage by rememberSaveable { mutableStateOf(stack?.toLanguage) }
    var pickerFor by remember { mutableStateOf<LanguageSlot?>(null) }

    // The stack stores the api code; the learner should read "Английский", not "en_GB".
    val languages by languagesViewModel.languages.collectAsStateWithLifecycle()
    val nameOf: (String?) -> String? = { code ->
        code?.let {
            val apiName = (languages as? LoadingState.Collected)?.content
                ?.firstOrNull { language -> language.full_code == it }
                ?.englishName ?: it
            languageDisplayName(it, apiName)
        }
    }

    OkCancelDialog(
        modifier = modifier.padding(16.dp),
        onDismissRequest = onDismiss,
        onConfirm = {
            val edited = stack?.copy(
                name = text,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                practiceSides = practice,
            ) ?: MemoStack(
                name = text,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                practiceSides = practice,
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

        Text(
            text = stringResource(R.string.practice_section_stack),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        PracticeChoice(
            label = stringResource(R.string.practice_as_in_settings, stringResource(libraryPractice.labelRes)),
            selected = practice == null,
            onClick = { practice = null }
        )
        PracticeSides.entries.forEach { option ->
            PracticeChoice(
                label = stringResource(option.labelRes),
                selected = practice == option.name,
                onClick = { practice = option.name }
            )
        }

        // A new stack can also come whole, from a deck made in Anki; the choice above goes to
        // the stacks it makes.
        if (onImportFromAnki != null) {
            Text(
                text = stringResource(R.string.import_from_anki),
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImportFromAnki(AnkiImportOptions(practice, resetAnkiProgress)) }
                    .padding(top = 12.dp, bottom = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = resetAnkiProgress, role = Role.Checkbox) { resetAnkiProgress = it }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(checked = resetAnkiProgress, onCheckedChange = null)
                Text(
                    text = stringResource(R.string.anki_reset_progress),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
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

/** What the learner chose for an Anki import in the New stack dialog. */
data class AnkiImportOptions(val practiceSides: String?, val resetProgress: Boolean)

@Composable
private fun PracticeChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, modifier = Modifier.padding(start = 12.dp))
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
