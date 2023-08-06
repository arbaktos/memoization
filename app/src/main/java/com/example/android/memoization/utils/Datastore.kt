package com.example.android.memoization.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object Datastore {
    val TO_SHOW_NOTIFICATIONS: Preferences.Key<Boolean> = booleanPreferencesKey("to_show_notifications")
    val NOTIF_PERIOD = longPreferencesKey("notification_repetition_period")
    val NOTIF_THRESHOLD_PERCENT = intPreferencesKey("notif_threashold_precent")
    val FILE_NAME = "settings"

}

suspend fun <T> DataStore<Preferences>.putValue(key: Preferences.Key<T>, value: T) {
    this.edit { preferences ->
        preferences[key] = value
    }
}

fun <T> DataStore<Preferences>.getValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
    return this.data.map {
        it[key] ?: defaultValue
    }
}