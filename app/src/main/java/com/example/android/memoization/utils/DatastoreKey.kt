package com.example.android.memoization.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object DatastoreKey {
    val FIRST_LAUNCH: Preferences.Key<Boolean> = booleanPreferencesKey("first_launch")
    val TIMES_A_DAY = intPreferencesKey("times_a_day")
    val TO_SHOW_NOTIFICATIONS: Preferences.Key<Boolean> = booleanPreferencesKey("to_show_notifications")
    val NOTIF_HOUR = intPreferencesKey("notif_hour_1")
    val NOTIF_HOUR_2 = intPreferencesKey("notif_hour_2")
    val NOTIF_HOUR_3 = intPreferencesKey("notif_hour_3")
    val NOTIF_HOUR_4 = intPreferencesKey("notif_hour_4")
    val NOTIF_MINUTE = intPreferencesKey("notif_minute")
    val NOTIF_MINUTE_2 = intPreferencesKey("notif_minute_2")
    val NOTIF_MINUTE_3 = intPreferencesKey("notif_minute_3")
    val NOTIF_MINUTE_4 = intPreferencesKey("notif_minute_4")
    val NOTIF_MONDAY = booleanPreferencesKey("notif_monday")
    val NOTIF_TUESDAY = booleanPreferencesKey("notif_tuesday")
    val NOTIF_WEDNESDAY = booleanPreferencesKey("notif_wednesday")
    val NOTIF_THURSDAY = booleanPreferencesKey("notif_thursday")
    val NOTIF_FRIDAY = booleanPreferencesKey("notif_friday")
    val NOTIF_SATURDAY = booleanPreferencesKey("notif_saturday")
    val NOTIF_SUNDAY = booleanPreferencesKey("notif_sunday")
    const val FILE_NAME = "settings"

}

suspend fun <T> DataStore<Preferences>.putValue(key: Preferences.Key<T>, value: T) {
    this.edit { preferences ->
        preferences[key] = value
    }
}

fun <T> DataStore<Preferences>.putValueScoped(key: Preferences.Key<T>, value: T, scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
        this@putValueScoped.edit { preferences ->
            preferences[key] = value
        }
    }
}

fun <T> DataStore<Preferences>.getValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
    return this.data.map {
        it[key] ?: defaultValue
    }
}