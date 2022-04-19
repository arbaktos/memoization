package com.example.android.memoization.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.android.memoization.utils.NavScreens

class LearnBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("LearnBroadcastReceiver", "Start")
        val pm = context?.packageManager
        val launchIntent = pm?.getLaunchIntentForPackage(context?.packageName)
        context?.startActivity(launchIntent)
    }

}