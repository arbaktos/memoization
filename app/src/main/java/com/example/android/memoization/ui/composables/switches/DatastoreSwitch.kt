package com.example.android.memoization.ui.composables.switches

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.settings.SettingsTileAction
import com.example.android.memoization.ui.features.settings.SettingsTileIcon
import com.example.android.memoization.ui.features.settings.SettingsTileTexts
import com.example.android.memoization.utils.getValue
import com.example.android.memoization.utils.putValueScoped
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DatastoreSwitch(
    modifier: Modifier = Modifier,
    switchItem: SwitchItem,
    dataStore: DataStore<Preferences>?,
) {
    var savedValue by remember { mutableStateOf(switchItem.initialValue) }
    LaunchedEffect(key1 = true, block = {
        dataStore?.getValue(switchItem.preferenceKey, switchItem.initialValue)?.collectLatest {
            savedValue = it
        }
    })
    val scope = rememberCoroutineScope()
    val onCheckedChanged: (Boolean) -> Unit = {
        dataStore?.putValueScoped(switchItem.preferenceKey, !savedValue, scope)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = savedValue,
                role = Role.Switch,
                onValueChange = { onCheckedChanged(it) }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsTileIcon(icon = switchItem.icon)
        SettingsTileTexts(title = switchItem.title, subtitle = switchItem.subtitle)
        SettingsTileAction {
            Switch(
                checked = savedValue,
                onCheckedChange = onCheckedChanged
            )
        }
    }
}

@Preview
@Composable
fun PreviewDatastoreSwitch() {
    DatastoreSwitch(
        switchItem = testSwitchItem,
        dataStore = null
    )
}

val testSwitchItem = object : SwitchItem {
    override val icon: ImageVector = Icons.Filled.Add
    override val title: Int = R.string.notif_title
    override val initialValue: Boolean
        get() = true
    override val preferenceKey: Preferences.Key<Boolean>
        get() = booleanPreferencesKey("testKey")
    override val subtitle: Int = R.string.notif_switch_desc
}