package com.example.android.memoization.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.android.memoization.utils.NotifConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CancelBroadcastReceiver: BroadcastReceiver() {

    @Inject
    lateinit var nm: NotificationManager
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getIntExtra(NotifConstants.NOTIFICATION_ID_LABEL, 0) ?: 0
        nm.cancel(id)
    }
}