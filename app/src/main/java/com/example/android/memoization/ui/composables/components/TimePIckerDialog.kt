package com.example.android.memoization.ui.composables.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.settings.getHourKey
import com.example.android.memoization.ui.features.settings.getMinuteKey
import com.example.android.memoization.utils.putValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    dismiss: () -> Unit,
    timePickerState: TimePickerState,
    dataStore: DataStore<Preferences>,
    index: Int
) {

    val scope = rememberCoroutineScope()

    val onDismiss = {
        dismiss()
    }
    val onConfirm = {
        scope.launch(Dispatchers.IO) {
            dataStore.apply {
                putValue(getHourKey(index), timePickerState.hour)
                putValue(getMinuteKey(index), timePickerState.minute)
            }
        }
        dismiss()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        text = { TimePicker(state = timePickerState) })

}