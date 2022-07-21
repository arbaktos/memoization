package com.example.android.memoization.ui.composables.components

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.android.memoization.R
import com.example.android.memoization.utils.to_show_notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

@Composable
fun MenuDrawer(scaffoldState: ScaffoldState) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl ) {
        ModalDrawer(
            drawerContent = { Text("Hello y'all drawer content") },
            content = { NotificationsSwitch(scaffoldState) }
        )
    }
}

@Composable
fun NotificationsSwitch(scaffoldState: ScaffoldState) {
    val coroutineScope = rememberCoroutineScope()
    val preferenceStorage = rememberPreferenceBooleanSettingState(
        key = to_show_notifications,
        defaultValue = true
    )
    SettingsSwitch(
        state = preferenceStorage,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = stringResource(R.string.notif_switch_desc)
            )
        },
        title = { Text(text = stringResource(R.string.notifications)) },
        onCheckedChange = {
            scaffoldState.showChange(
                coroutineScope = coroutineScope,
                key = "Notifications are on",
                state = preferenceStorage,
            )
        },
    )
}

//TODO why notifications are behind the drawer, pass drawer state maybe?

private fun ScaffoldState.showChange(
    coroutineScope: CoroutineScope,
    key: String,
    state: SettingValueState<Boolean>
) {
    coroutineScope.launch {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = "$key:  ${state.value}")
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    state: SettingValueState<Boolean> = rememberBooleanSettingState(),
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    var storageValue by state
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

@Composable
fun rememberPreferenceBooleanSettingState(
    key: String,
    defaultValue: Boolean,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current),
): BooleanPreferenceSettingValueState {
    return remember {
        BooleanPreferenceSettingValueState(
            preferences = preferences,
            key = key,
            defaultValue = defaultValue
        )
    }
}

@Composable
fun rememberBooleanSettingState(defaultValue: Boolean = true): SettingValueState<Boolean> {
    return remember { InMemoryBooleanSettingValueState(defaultValue) }
}

class BooleanPreferenceSettingValueState(
    private val preferences: SharedPreferences,
    val key: String,
    val defaultValue: Boolean = true,
) : SettingValueState<Boolean> {

    private var _value by mutableStateOf(preferences.getBoolean(key, defaultValue))

    override var value: Boolean
        set(value) {
            _value = value
            preferences.edit { putBoolean(key, value) }
        }
        get() = _value

    override fun reset() {
        value = defaultValue
    }
}

class InMemoryBooleanSettingValueState(private val defaultValue: Boolean) : SettingValueState<Boolean> {
    override var value: Boolean by mutableStateOf(defaultValue)
    override fun reset() {
        value = defaultValue
    }
}

interface SettingValueState<T> {
    fun reset()
    var value: T
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> SettingValueState<T>.setValue(
    thisObj: Any?,
    property: KProperty<*>,
    value: T
) {
    this.value = value
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> SettingValueState<T>.getValue(thisObj: Any?, property: KProperty<*>): T =
    value