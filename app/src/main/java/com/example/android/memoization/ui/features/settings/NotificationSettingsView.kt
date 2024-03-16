package com.example.android.memoization.ui.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.R
import com.example.android.memoization.ui.composables.components.DatastoreChip
import com.example.android.memoization.ui.composables.components.TimePickerDialog
import com.example.android.memoization.ui.composables.switches.DatastoreSwitch
import com.example.android.memoization.ui.composables.switches.SwitchItem
import com.example.android.memoization.utils.DatastoreKey
import com.example.android.memoization.utils.SettingsDefaults
import com.example.android.memoization.utils.getValue
import com.example.android.memoization.utils.putValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun NotificationsView(modifier: Modifier = Modifier, preferenceStorage: DataStore<Preferences>) {
    val notificationsSwitch = object : SwitchItem {
        override val icon: ImageVector = Icons.Filled.Notifications
        override val title: Int = R.string.notifications
        override val initialValue: Boolean
            get() = true
        override val preferenceKey: Preferences.Key<Boolean> =
            DatastoreKey.TO_SHOW_NOTIFICATIONS
        override val subtitle: Int? = null
    }

    val showNotificationSettings = preferenceStorage.getValue(
        notificationsSwitch.preferenceKey,
        notificationsSwitch.initialValue
    ).collectAsState(initial = notificationsSwitch.initialValue)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        DatastoreSwitch(
            modifier = modifier,
            switchItem = notificationsSwitch,
            dataStore = preferenceStorage
        )
        if (showNotificationSettings.value) {
            NotificationSettingsView(dataStore = preferenceStorage)
        }
    }

}

@Composable
fun NotificationSettingsView(modifier: Modifier = Modifier, dataStore: DataStore<Preferences>) {
    Column {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = modifier
                .fillMaxWidth()
        ) {
            TimesAdayMenu(dataStore = dataStore)
        }
        Row {
            repeat(
                dataStore.getValue(DatastoreKey.TIMES_A_DAY, SettingsDefaults.MIN_TIMES_A_DAY)
                    .collectAsState(initial = 1).value
            ) {
                ClockText(dataStore = dataStore, index = it, modifier = Modifier.padding(8.dp))
            }
        }
        TimesAWeek(dataStore = dataStore, modifier = Modifier.padding(8.dp))
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimesAWeek(dataStore: DataStore<Preferences>, modifier: Modifier = Modifier) {

    FlowRow(modifier = modifier) {
        DatastoreChip(
            label = R.string.lbl_monday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_MONDAY
        )
        DatastoreChip(
            label = R.string.lbl_tuesday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_TUESDAY
        )
        DatastoreChip(
            label = R.string.lbl_wednesday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_WEDNESDAY
        )
        DatastoreChip(
            label = R.string.lbl_thursday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_THURSDAY
        )
        DatastoreChip(
            label = R.string.lbl_friday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_FRIDAY
        )
        DatastoreChip(
            label = R.string.lbl_saturday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_SATURDAY
        )
        DatastoreChip(
            label = R.string.lbl_sunday,
            dataStore = dataStore,
            dataStoreKey = DatastoreKey.NOTIF_SUNDAY
        )
    }
}


@Composable
fun TimesAdayMenu(modifier: Modifier = Modifier,dataStore: DataStore<Preferences>) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        val scope = rememberCoroutineScope()
        var menuExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { menuExpanded = true }, modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    dataStore.getValue(DatastoreKey.TIMES_A_DAY, SettingsDefaults.MIN_TIMES_A_DAY)
                        .collectAsState(initial = SettingsDefaults.MIN_TIMES_A_DAY).value.toString()
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                (SettingsDefaults.MIN_TIMES_A_DAY..SettingsDefaults.MAX_TIMES_A_DAY).forEach {
                    Text(fontSize = 16.sp, text = it.toString(), modifier = Modifier
                        .clickable {
                            scope.launch(Dispatchers.IO) {
                                dataStore.putValue(DatastoreKey.TIMES_A_DAY, it)
                                menuExpanded = false
                            }
                        }
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp))
                }
            }
        }
        Text(text = stringResource(id = R.string.lbl_times_per_day), modifier = Modifier.padding(start = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockText(modifier: Modifier = Modifier, dataStore: DataStore<Preferences>, index: Int) {
    var timePickerVisible by remember { mutableStateOf(false) }

    val hour =
        dataStore.getValue(getHourKey(index), SettingsDefaults.DEFAULT_HOUR_NOTIFICATION)
            .collectAsState(
                initial = SettingsDefaults.DEFAULT_HOUR_NOTIFICATION
            )
    val minute =
        dataStore.getValue(getMinuteKey(index), SettingsDefaults.DEFAULT_MINUTE_NOTIFICATION)
            .collectAsState(
                initial = SettingsDefaults.DEFAULT_MINUTE_NOTIFICATION
            )

    TextButton(
        onClick = { timePickerVisible = true },
        border = BorderStroke(1.dp, Color.Black),
        modifier = modifier
    ) {
        Text(text = "${hour.value}:${if (minute.value < 10) "0" else ""}${minute.value}")
    }

    if (timePickerVisible) {
        TimePickerDialog(
            dismiss = { timePickerVisible = false },
            dataStore = dataStore,
            timePickerState = rememberTimePickerState(
                initialHour = hour.value,
                initialMinute = minute.value,
                is24Hour = true
            ),
            index = index
        )
    }
}


fun getMinuteKey(index: Int): Preferences.Key<Int> {
    return when (index) {
        1 -> DatastoreKey.NOTIF_MINUTE
        2 -> DatastoreKey.NOTIF_MINUTE_2
        3 -> DatastoreKey.NOTIF_MINUTE_3
        else -> DatastoreKey.NOTIF_MINUTE_4
    }
}

fun getHourKey(index: Int): Preferences.Key<Int> {
    return when (index) {
        1 -> DatastoreKey.NOTIF_HOUR
        2 -> DatastoreKey.NOTIF_HOUR_2
        3 -> DatastoreKey.NOTIF_HOUR_3
        else -> DatastoreKey.NOTIF_HOUR_4
    }
}

fun is24HourFormat(): Boolean {
    val dateFormat =
        DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()) as SimpleDateFormat
    val pattern = dateFormat.toPattern()
    return pattern.contains("H") || pattern.contains("k")
}