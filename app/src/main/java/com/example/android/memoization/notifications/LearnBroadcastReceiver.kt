package com.example.android.memoization.notifications

import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.android.memoization.MainActivity

class LearnBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("LearnBroadcastReceiver", "Start")
        val stackId = intent?.getLongExtra(NotificationReceiver.STACK_ID_LABEL, 0L)
        val learnIntent = Intent(context, MainActivity::class.java)
            .putExtra(START_TO_LEARN_LABEL, true)
            .putExtra(NotificationReceiver.STACK_ID_LABEL, stackId)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(learnIntent)
    }

   companion object {
       const val START_TO_LEARN_LABEL = "start_to_learn_from_notif"
   }

}