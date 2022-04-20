package com.example.android.memoization.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.android.memoization.MainActivity
import com.example.android.memoization.R

class Notification (val context: Context) {

    //TODO implement timed notification

    private val REMINDER_CHANNEL_ID = "reminder_channel"

    init {
        createReminderChannel()
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "channel_name"
            val descriptionText = "reminder to learn the words"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }


    fun shortReminderNotification(title: String, content: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, tapIntent, 0)

        val notificationId = 0 // should it be unique?

        val postponeIntent = Intent(context, PostponeBroadcastReceiver::class.java)
        val postponePendingIntent = PendingIntent.getBroadcast(context, 0, postponeIntent, 0)

        val cancelIntent = Intent(context, CancelBroadcastReceiver::class.java)
        val cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, 0)

        val startIntent = Intent(context, LearnBroadcastReceiver::class.java)
        val startPendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, 0)


        val ntfBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_play_arrow_24, "Start", startPendingIntent)
            .addAction(R.drawable.ic_more_time_24, "Postpone", postponePendingIntent)
            .addAction(R.drawable.ic_cancel, "Cancel", cancelPendingIntent)


        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, ntfBuilder.build())
        }
    }
}