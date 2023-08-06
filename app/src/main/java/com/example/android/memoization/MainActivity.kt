package com.example.android.memoization

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.android.memoization.extensions.scheduleAlarm
import com.example.android.memoization.notifications.LearnBroadcastReceiver
import com.example.android.memoization.notifications.NotificationReceiver
import com.example.android.memoization.utils.Datastore
import com.example.android.memoization.utils.putValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHost.navController

        lifecycleScope.launch {
            dataStore.putValue(Datastore.NOTIF_THRESHOLD_PERCENT, 0)
        }
        if (intent.getBooleanExtra(LearnBroadcastReceiver.START_TO_LEARN_LABEL, false)) {
            val stackIdBundle = Bundle().apply {
                putLong(NotificationReceiver.STACK_ID_LABEL, intent.getLongExtra(NotificationReceiver.STACK_ID_LABEL, 0))
            }
            navController.navigate(R.id.memorizationFragment, stackIdBundle)
        }
        scheduleAlarm(Calendar.getInstance().timeInMillis, Intent(applicationContext, NotificationReceiver::class.java), 100)
    }
}

