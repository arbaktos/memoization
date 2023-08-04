package com.example.android.memoization.ui.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.R
import com.example.android.memoization.utils.Datastore
import com.example.android.memoization.utils.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun MenuDrawer(scaffoldState: ScaffoldState, preferenceStorage: DataStore<Preferences>) {
    ModalDrawer(
        drawerContent = { },
        content = {
            Column() {
                NotificationsSwitch(scaffoldState, preferenceStorage)
            }
        }
    )
}

@Composable
fun NotificationsSwitch(scaffoldState: ScaffoldState, preferenceStorage: DataStore<Preferences>) {
    val coroutineScope = rememberCoroutineScope()
    val state = preferenceStorage.getValue(
        key = Datastore.TO_SHOW_NOTIFICATIONS,
        defaultValue = true)

    SettingsSwitch(
        state = state,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = stringResource(R.string.notif_switch_desc)
            )
        },
        initialValue = ConstantsSettings.SHOW_NOTIFICATIONS,
        title = { Text(text = stringResource(R.string.notifications)) },
        onCheckedChange = {
            scaffoldState.showChange(
                coroutineScope = coroutineScope,
                key = "Notifications are on",
                state = state,
            )
        },
    )
}


private fun ScaffoldState.showChange(
    coroutineScope: CoroutineScope,
    key: String,
    state: Flow<Boolean>
) {
    coroutineScope.launch {
        snackbarHostState.currentSnackbarData?.dismiss()
//        snackbarHostState.showSnackbar(message = "$key:  ${state.collectAsState(initial = false).value}")
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    state: Flow<Boolean>,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    initialValue: Boolean,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    var storageValue = state.collectAsState(initial = initialValue).value
    val update: (Boolean) -> Unit = { boolean ->
        storageValue = boolean
        onCheckedChange(storageValue)
    }
    Surface {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .toggleable(
                    value = storageValue,
                    role = Role.Switch,
                    onValueChange = { update(!storageValue) }
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsTileIcon(icon = icon)
            SettingsTileTexts(title = title, subtitle = subtitle)
            SettingsTileAction {
                Switch(
                    checked = storageValue,
                    onCheckedChange = update
                )
            }
        }
    }
}

@Composable
internal fun SettingsTileIcon(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                icon()
            }
        }
    }
}

@Composable
internal fun RowScope.SettingsTileTexts(
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)?,
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
    ) {
        SettingsTileTitle(title)
        if (subtitle != null) {
            Spacer(modifier = Modifier.size(2.dp))
            SettingsTileSubtitle(subtitle)
        }
    }
}

@Composable
internal fun SettingsTileAction(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
internal fun SettingsTileTitle(title: @Composable () -> Unit) {
    ProvideTextStyle(value = MaterialTheme.typography.subtitle1) {
        title()
    }
}

@Composable
internal fun SettingsTileSubtitle(subtitle: @Composable () -> Unit) {
    ProvideTextStyle(value = MaterialTheme.typography.caption) {
        CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.medium,
            content = subtitle
        )
    }
}
