package com.example.android.memoization.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.android.memoization.ui.composables.screens.TDEBUG
import com.example.android.memoization.utils.NOTIFICATION_ID_LABEL

class CancelBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getIntExtra(NOTIFICATION_ID_LABEL, 0) ?: 0
        val nm = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(id)
    }
}