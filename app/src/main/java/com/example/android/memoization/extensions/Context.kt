package com.example.android.memoization.extensions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.android.memoization.utils.DefaultDataStoreName
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

//val Context.currentConnectivityState: ConnectionState
//    get() {
//        val connectivityManager =
//            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        return getCurrentConnectivityState(connectivityManager)
//    }

infix fun Context.showToast(stringId: Int?) {
    if (stringId != null) {
        Toast.makeText(this, this.resources.getText(stringId), Toast.LENGTH_SHORT).show()
    }
}

fun Context.showToast(message: String) {
    if (message.isNotBlank()) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

//private fun getCurrentConnectivityState(
//    connectivityManager: ConnectivityManager
//): ConnectionState {
//    val connected = connectivityManager.allNetworks.any { network ->
//        connectivityManager.getNetworkCapabilities(network)
//            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            ?: false
//    }
//
//    return if (connected) ConnectionState.Available else ConnectionState.Unavailable
//}

@SuppressLint("ScheduleExactAlarm")
fun Context?.scheduleAlarm(timeToTrigger: Long, alarmIntent: Intent, requestCode: Int = 0) {
    val alarmMgr: AlarmManager? = this?.getSystemService(AppCompatActivity.ALARM_SERVICE) as? AlarmManager?


    val pendingIntent = PendingIntent.getBroadcast(
        this,
        requestCode,
        alarmIntent,
        PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_IMMUTABLE
    )

    Log.d("scheduleAlarm", "scheduleAlarm: requestcode $requestCode")
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
        alarmMgr?.setAlarmClock(
            AlarmManager.AlarmClockInfo(timeToTrigger, pendingIntent),
            pendingIntent
        )
    } else {
        alarmMgr?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeToTrigger,
            pendingIntent
        )
    }
}

//sealed class ConnectionState {
//    object Available : ConnectionState()
//    object Unavailable : ConnectionState()
//}