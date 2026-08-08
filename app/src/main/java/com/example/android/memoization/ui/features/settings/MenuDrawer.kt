package com.example.android.memoization.ui.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalDrawer
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.R
import com.example.android.memoization.ui.composables.labels.PrimaryBoldLabel

@Composable
fun MenuDrawer(preferenceStorage: DataStore<Preferences>) {
    ModalDrawer(
        drawerContent = {
            Column {
                Text("settings")
            }
        },
        content = { SettingsSheet(preferenceStorage = preferenceStorage) }
    )
}

@Composable
fun SettingsSheet(modifier: Modifier = Modifier, preferenceStorage: DataStore<Preferences>) {
    Scaffold(
        topBar = { PrimaryBoldLabel(text = LocalContext.current.getString(R.string.settings_top_bar_label)) }
    ) {
        Column(modifier = modifier.padding(it)) {
            NotificationsView(preferenceStorage = preferenceStorage)
        }
    }
}

@Composable
internal fun SettingsTileIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
) {
    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@Composable
internal fun RowScope.SettingsTileTexts(
    title: Int,
    subtitle: Int?,
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
internal fun SettingsTileTitle(title: Int) {
    ProvideTextStyle(value = MaterialTheme.typography.subtitle1) {
        Text(text = stringResource(id = title))
    }
}

@Composable
internal fun SettingsTileSubtitle(subtitle: Int) {
    ProvideTextStyle(value = MaterialTheme.typography.caption) {
        CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.medium,
            content = { Text(stringResource(id = subtitle)) }
        )
    }
}