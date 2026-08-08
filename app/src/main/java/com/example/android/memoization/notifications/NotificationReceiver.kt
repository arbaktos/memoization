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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.MainActivity
import com.example.android.memoization.R
import com.example.android.memoization.domain.usecases.HasWordsToRepeatUseCase
import com.example.android.memoization.notifications.NotificationReceiver.NotificationChannel.REMINDER_CHANNEL_DESCRIPTION
import com.example.android.memoization.notifications.NotificationReceiver.NotificationChannel.REMINDER_CHANNEL_ID
import com.example.android.memoization.notifications.NotificationReceiver.NotificationChannel.REMINDER_CHANNEL_NAME
import com.example.android.memoization.utils.NotifConstants.NOTIFICATION_ID
import com.example.android.memoization.utils.NotifConstants.NOTIFICATION_ID_LABEL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver @Inject constructor() : BroadcastReceiver() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var hasWordsToRepeatUseCase: HasWordsToRepeatUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ")
        val appContext = context?.applicationContext ?: return

        // The work below touches the database, so hold the broadcast open for it.
        val pendingResult = goAsync()
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                if (hasWordsToRepeatUseCase()) {
                    createShortReminderNotification(
                        appContext,
                        appContext.getString(R.string.notif_title),
                        appContext.getString(R.string.notif_content),
                    )
                } else {
                    Log.d(TAG, "onReceive: nothing to repeat today, staying quiet")
                }
                // One-shot alarm: line up the next one whether or not we notified.
                notificationScheduler.reschedule()
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: reminder failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    object NotificationChannel {
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val REMINDER_CHANNEL_NAME = "reminder_channel_name"
        const val REMINDER_CHANNEL_DESCRIPTION = "reminder to learn the words"
    }

    private fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(REMINDER_CHANNEL_ID, REMINDER_CHANNEL_NAME, importance)
            mChannel.description = REMINDER_CHANNEL_DESCRIPTION
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun createShortReminderNotification(
        context: Context,
        title: String,
        content: String
    ) {
        createReminderChannel(context)
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(context, CancelBroadcastReceiver::class.java)
        cancelIntent.putExtra(NOTIFICATION_ID_LABEL, NOTIFICATION_ID)
        val cancelPendingIntent =
            PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        val startIntent = Intent(context, LearnBroadcastReceiver::class.java)
        val startPendingIntent =
            PendingIntent.getBroadcast(context, 0, startIntent, PendingIntent.FLAG_IMMUTABLE)

        val ntfBuilder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .addAction(
                R.drawable.ic_play_arrow_24,
                context.getString(R.string.start),
                startPendingIntent
            )
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.cancel),
                cancelPendingIntent
            )
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID, ntfBuilder.build())
            }
        }
    }

    companion object {
        const val STACK_ID_LABEL = "stackId"
        private const val TAG = "NotificationReceiver"
    }
}