package com.example.android.memoization.ui.composables.components

import androidx.datastore.preferences.core.Preferences

interface DataStoreItem<T> {
    val preferenceKey: Preferences.Key<T>
}