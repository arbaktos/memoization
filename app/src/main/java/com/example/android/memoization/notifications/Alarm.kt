package com.example.android.memoization.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.android.memoization.ui.features.folderscreen.TDEBUG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TDEBUG, "AlarmReciever onReceive")
    }

}