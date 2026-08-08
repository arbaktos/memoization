package com.example.android.memoization

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.android.memoization.notifications.NotificationScheduler
import com.example.android.memoization.ui.AppComposable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        askForNotificationPermission()
        keepReminderInSyncWithSettings()

        setContent {
            AppComposable(preferenceStorage = dataStore)
        }
    }

    /** Without this the notification is built and then silently dropped on Android 13+. */
    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /** Re-arms the alarm on every launch and whenever a notification setting changes. */
    private fun keepReminderInSyncWithSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataStore.data.distinctUntilChanged().collect {
                    notificationScheduler.reschedule()
                }
            }
        }
    }
}
