package com.example.android.memoization.ui.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.R
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.utils.DatastoreKey
import com.example.android.memoization.utils.getValue
import com.example.android.memoization.utils.putValueScoped

/**
 * Which sides of a pair to practise. Both sides always exist; this only decides which of them
 * are scheduled, so switching back and forth never loses progress. Smart switch, the default,
 * starts word to meaning and hands over to the meaning side once the word side is known -
 * one side of a pair at a time; Both sides is the setting that schedules the two together.
 */
@Composable
fun LearningView(modifier: Modifier = Modifier, preferenceStorage: DataStore<Preferences>) {
    val scope = rememberCoroutineScope()
    val storedName by preferenceStorage
        .getValue(DatastoreKey.PRACTICE_SIDES, PracticeSides.DEFAULT.name)
        .collectAsState(initial = PracticeSides.DEFAULT.name)
    val selected = PracticeSides.fromName(storedName)

    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) {
            SettingsTileTitle(R.string.learning_section)
        }
        PracticeSidesOption(R.string.practice_smart_switch, PracticeSides.SMART_SWITCH, selected) {
            preferenceStorage.putValueScoped(DatastoreKey.PRACTICE_SIDES, it.name, scope)
        }
        PracticeSidesOption(R.string.practice_word_to_meaning, PracticeSides.WORD_TO_MEANING, selected) {
            preferenceStorage.putValueScoped(DatastoreKey.PRACTICE_SIDES, it.name, scope)
        }
        PracticeSidesOption(R.string.practice_meaning_to_word, PracticeSides.MEANING_TO_WORD, selected) {
            preferenceStorage.putValueScoped(DatastoreKey.PRACTICE_SIDES, it.name, scope)
        }
        PracticeSidesOption(R.string.practice_both, PracticeSides.BOTH, selected) {
            preferenceStorage.putValueScoped(DatastoreKey.PRACTICE_SIDES, it.name, scope)
        }
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)) {
            SettingsTileSubtitle(R.string.practice_smart_switch_caption)
        }
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)) {
            SettingsTileSubtitle(R.string.practice_new_side_caption)
        }
    }
}

@Composable
private fun PracticeSidesOption(
    label: Int,
    option: PracticeSides,
    selected: PracticeSides,
    onSelect: (PracticeSides) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = option == selected,
                role = Role.RadioButton,
                onClick = { onSelect(option) }
            )
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        RadioButton(selected = option == selected, onClick = null)
        Row(modifier = Modifier.padding(start = 12.dp)) {
            SettingsTileTitle(label)
        }
    }
}
