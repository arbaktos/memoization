package com.example.android.memoization.ui.composables.switches

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.ui.composables.components.DataStoreItem

interface SwitchItem: DataStoreItem<Boolean> {
    val icon: ImageVector
    val title: Int
    val initialValue: Boolean
    override val preferenceKey: Preferences.Key<Boolean>
    val subtitle: Int?
}