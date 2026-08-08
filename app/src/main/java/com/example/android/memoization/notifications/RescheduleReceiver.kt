package com.example.android.memoization.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A pending alarm is a fixed moment in time, so anything that moves the clock
 * underneath it has to re-arm the reminder at the new local 12:00 - otherwise a
 * flight to another timezone leaves it firing an hour early for good. A reboot or
 * an app update drops the alarm outright.
 */
@AndroidEntryPoint
class RescheduleReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action !in RESCHEDULE_ACTIONS) return
        Log.d(TAG, "onReceive: re-arming reminder after $action")

        val pendingResult = goAsync()
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                notificationScheduler.reschedule()
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: could not re-arm reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "RescheduleReceiver"

        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
