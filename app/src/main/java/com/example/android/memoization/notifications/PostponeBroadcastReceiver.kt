package com.example.android.memoization.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PostponeBroadcastReceiver: BroadcastReceiver () {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("PostponeBroadcast", "postponed")
    }
}