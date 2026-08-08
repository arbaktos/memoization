package com.example.android.memoization.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.utils.DatastoreKey
import com.example.android.memoization.utils.NotifConstants
import com.example.android.memoization.utils.SettingsDefaults
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the daily reminder alarm: works out when the next reminder is due from the
 * settings the user picked and hands that single moment to AlarmManager. The alarm
 * is one-shot - NotificationReceiver asks for the next one every time it fires.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun reschedule() {
        val prefs = dataStore.data.first()
        if (prefs[DatastoreKey.TO_SHOW_NOTIFICATIONS] != false) {
            val nextTrigger = nextTriggerAt(prefs)
            if (nextTrigger != null) setAlarm(nextTrigger) else cancel()
        } else {
            cancel()
        }
    }

    fun cancel() {
        alarmManager()?.cancel(alarmIntent())
        Log.d(TAG, "cancel: reminder alarm cancelled")
    }

    /** Calendar.getInstance() reads the device timezone, so this is local wall-clock. */
    private fun nextTriggerAt(prefs: Preferences): Long? =
        ReminderSchedule.nextTriggerAt(notifTimes(prefs), enabledWeekDays(prefs))

    /** Times of day the user asked for, sorted, defaulting to a single 12:00 reminder. */
    private fun notifTimes(prefs: Preferences): List<Pair<Int, Int>> {
        val timesADay = (prefs[DatastoreKey.TIMES_A_DAY] ?: SettingsDefaults.MIN_TIMES_A_DAY)
            .coerceIn(SettingsDefaults.MIN_TIMES_A_DAY, SettingsDefaults.MAX_TIMES_A_DAY)
        val hourKeys = listOf(
            DatastoreKey.NOTIF_HOUR,
            DatastoreKey.NOTIF_HOUR_2,
            DatastoreKey.NOTIF_HOUR_3,
            DatastoreKey.NOTIF_HOUR_4
        )
        val minuteKeys = listOf(
            DatastoreKey.NOTIF_MINUTE,
            DatastoreKey.NOTIF_MINUTE_2,
            DatastoreKey.NOTIF_MINUTE_3,
            DatastoreKey.NOTIF_MINUTE_4
        )
        return (0 until timesADay).map { index ->
            val hour = prefs[hourKeys[index]] ?: SettingsDefaults.DEFAULT_HOUR_NOTIFICATION
            val minute = prefs[minuteKeys[index]] ?: SettingsDefaults.DEFAULT_MINUTE_NOTIFICATION
            hour to minute
        }
    }

    /** Calendar.DAY_OF_WEEK values the user enabled; every day until they say otherwise. */
    private fun enabledWeekDays(prefs: Preferences): Set<Int> {
        val byDay = mapOf(
            Calendar.MONDAY to DatastoreKey.NOTIF_MONDAY,
            Calendar.TUESDAY to DatastoreKey.NOTIF_TUESDAY,
            Calendar.WEDNESDAY to DatastoreKey.NOTIF_WEDNESDAY,
            Calendar.THURSDAY to DatastoreKey.NOTIF_THURSDAY,
            Calendar.FRIDAY to DatastoreKey.NOTIF_FRIDAY,
            Calendar.SATURDAY to DatastoreKey.NOTIF_SATURDAY,
            Calendar.SUNDAY to DatastoreKey.NOTIF_SUNDAY,
        )
        return byDay.filterValues { prefs[it] ?: true }.keys
    }

    /**
     * Exact alarms need a permission the user has to grant by hand on Android 12+.
     * Without it a reminder within a 15 minute window is better than none at all.
     */
    private fun setAlarm(triggerAtMillis: Long) {
        val alarmManager = alarmManager() ?: return
        val intent = alarmIntent()
        alarmManager.cancel(intent)

        if (canScheduleExactAlarms(alarmManager)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
        } else {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                INEXACT_WINDOW_MILLIS,
                intent
            )
        }
        Log.d(TAG, "setAlarm: next reminder at ${formatted(triggerAtMillis)}")
    }

    private fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    private fun alarmManager(): AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun alarmIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        NotifConstants.ALARM_REQUEST_CODE,
        Intent(context, NotificationReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun formatted(millis: Long): String =
        SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(millis)

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val INEXACT_WINDOW_MILLIS = 15 * 60 * 1000L
    }
}
