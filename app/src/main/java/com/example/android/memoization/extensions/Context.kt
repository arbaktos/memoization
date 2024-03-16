package com.example.android.memoization.extensions

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

infix fun Context.showToast(stringId: Int?) {
    if (stringId != null) {
        Toast.makeText(this, this.resources.getText(stringId), Toast.LENGTH_SHORT).show()
    }
}

fun Context.showToast(message: String) {
    if (message.isNotBlank()) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun cancelScheduledAlarm(alarmManager: AlarmManager?, pendingIntent: PendingIntent) {
    alarmManager?.cancel(pendingIntent)
}

@SuppressLint("ScheduleExactAlarm")
fun Context?.scheduleAlarm(timeToTrigger: Long, alarmIntent: Intent, requestCode: Int = 0) {
    this?.let {
        val alarmMgr: AlarmManager? = this.getSystemService(AppCompatActivity.ALARM_SERVICE) as? AlarmManager?

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_IMMUTABLE
        )

        cancelScheduledAlarm(alarmMgr, pendingIntent)

        Log.d("scheduleAlarm", "scheduleAlarm: requestcode $requestCode")
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            alarmMgr?.setAlarmClock(
                AlarmManager.AlarmClockInfo(timeToTrigger, pendingIntent),
                pendingIntent
            )
        } else {
            alarmMgr?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeToTrigger,
                pendingIntent
            )
        }
    }
}