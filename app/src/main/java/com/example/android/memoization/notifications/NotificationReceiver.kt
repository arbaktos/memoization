package com.example.android.memoization.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.android.memoization.MainActivity
import com.example.android.memoization.R
import com.example.android.memoization.utils.NOTIFICATION_ID
import com.example.android.memoization.utils.NOTIFICATION_ID_LABEL

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            createShortReminderNotification(
                context,
                context.getString(R.string.notif_title),
                context.getString(R.string.notif_content)
            )
        }
    }

    private val REMINDER_CHANNEL_ID = "reminder_channel"

    private fun createReminderChannel(context: Context) {
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


    private fun createShortReminderNotification(context: Context, title: String, content: String) {
        createReminderChannel(context)
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val cancelIntent = Intent(context, CancelBroadcastReceiver::class.java)
        cancelIntent.putExtra(NOTIFICATION_ID_LABEL, NOTIFICATION_ID)
        val cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        val startIntent = Intent(context, LearnBroadcastReceiver::class.java)
        val startPendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, PendingIntent.FLAG_IMMUTABLE)

        val ntfBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .addAction(R.drawable.ic_play_arrow_24, context.getString(R.string.start), startPendingIntent)
            .addAction(R.drawable.ic_cancel, context.getString(R.string.cancel), cancelPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID, ntfBuilder.build())
            }
        }
    }
}