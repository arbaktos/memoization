package com.example.android.memoization

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.android.memoization.domain.usecases.NotifTimeCalcUseCase
import com.example.android.memoization.extensions.cancelScheduledAlarm
import com.example.android.memoization.extensions.scheduleAlarm
import com.example.android.memoization.notifications.NotificationReceiver
import com.example.android.memoization.utils.DatastoreKey
import com.example.android.memoization.utils.NotifConstants
import com.example.android.memoization.utils.getValue
import com.example.android.memoization.utils.putValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notifTimeCalcUseCase: NotifTimeCalcUseCase

    private lateinit var navController: NavController

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHost.navController
        scheduleFirstAlarm(lifecycleScope)
        listenNotificationSettingChanges()
    }

    private fun listenNotificationSettingChanges() {
        lifecycleScope.launch {
            dataStore.data.collect { prefs ->
                val toShow = prefs[DatastoreKey.TO_SHOW_NOTIFICATIONS] ?: true
                Log.d(TAG, "listenNotificationSettingChanges: $toShow")
                if (toShow) {
                    notifTimeCalcUseCase().collectLatest { notifTime ->
                        notifTime?.let {
                            applicationContext.scheduleAlarm(
                                notifTime,
                                Intent(applicationContext, NotificationReceiver::class.java),
                                NotifConstants.ALARM_REQUEST_CODE
                            )
                        }
                    }
                }
                else {
                    cancelScheduledAlarm(
                        getSystemService(ALARM_SERVICE) as? AlarmManager?,
                        PendingIntent.getBroadcast(
                            applicationContext,
                            NotifConstants.ALARM_REQUEST_CODE,
                            Intent(applicationContext, NotificationReceiver::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        ))
                }
            }
        }
    }

    private fun scheduleFirstAlarm(scope: CoroutineScope) {

        scope.launch {
            dataStore.getValue(DatastoreKey.FIRST_LAUNCH, true).combine(
                notifTimeCalcUseCase()
            ) { firstLaunch, notifTime ->
                if (firstLaunch) notifTime?.let {
                    scheduleAlarm(
                        it,
                        Intent(applicationContext, NotificationReceiver::class.java),
                        NotifConstants.ALARM_REQUEST_CODE
                    )
                }
                dataStore.putValue(DatastoreKey.FIRST_LAUNCH, false)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}