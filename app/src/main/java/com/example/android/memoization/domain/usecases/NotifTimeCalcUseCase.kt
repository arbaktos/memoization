package com.example.android.memoization.domain.usecases

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.utils.DatastoreKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

interface NotifTimeCalcUseCase {
    operator fun invoke(): Flow<Long?>
}

const val TAG = "NotifUseCaseImpl"

class NotifTimeCalcUseCaseImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val dataStore: DataStore<Preferences>
) : NotifTimeCalcUseCase {

    override fun invoke(): Flow<Long?> {
        Log.d(TAG, "invoke: ")
        val result: MutableStateFlow<Long?> = MutableStateFlow(null)
        val alarmTimes = mutableListOf<Calendar>()
        var showToday = false
        val calendar = Calendar.getInstance()
        val dayName = SimpleDateFormat("EE", Locale.ENGLISH).format(calendar.time).lowercase()
        val day = getDayIndex(dayName)
        Log.d(TAG, "invoke: day = $day")
        var nextDay: Int? = null
        var prefs: Preferences
        runBlocking {
            prefs = dataStore.data.first()
        }
        val toShow = prefs[DatastoreKey.TO_SHOW_NOTIFICATIONS] ?: false
        if (!toShow) return result
        // in Calendar monday is 2, tuesday is 3, etc
        val notifWeekDays = arrayOf(
            prefs[DatastoreKey.NOTIF_MONDAY] ?: false,
            prefs[DatastoreKey.NOTIF_TUESDAY] ?: false,
            prefs[DatastoreKey.NOTIF_WEDNESDAY] ?: false,
            prefs[DatastoreKey.NOTIF_THURSDAY] ?: false,
            prefs[DatastoreKey.NOTIF_FRIDAY] ?: false,
            prefs[DatastoreKey.NOTIF_SATURDAY] ?: false,
            prefs[DatastoreKey.NOTIF_SUNDAY] ?: false
        )
        // by default only first alarm is set
        var notifTimes = listOf(
            (prefs[DatastoreKey.NOTIF_HOUR] ?: 12) to (prefs[DatastoreKey.NOTIF_MINUTE] ?: 0),
            (prefs[DatastoreKey.NOTIF_HOUR_2]) to (prefs[DatastoreKey.NOTIF_MINUTE_2]),
            (prefs[DatastoreKey.NOTIF_HOUR_3]) to (prefs[DatastoreKey.NOTIF_MINUTE_3]),
            (prefs[DatastoreKey.NOTIF_HOUR_4]) to (prefs[DatastoreKey.NOTIF_MINUTE_4]),
        )

        notifTimes.forEach {
            Log.d(TAG, "invoke: notifTimes = ${it.first} : ${it.second}")
        }


        showToday = notifWeekDays[day]

        notifTimes.forEach {
            if (it.first != null && it.second != null) {
                alarmTimes.add(Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, it.first!!)
                    set(Calendar.MINUTE, it.second!!)
                })
            }
        }

        notifTimes = notifTimes.sortedBy { it.first }
        if (showToday) {
            alarmTimes.forEach { alarm ->
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
                // if today is chosen for notification and
                if (alarm.get(Calendar.HOUR_OF_DAY) >= currentHour && alarm.get(Calendar.MINUTE) >= currentMinute) {
                    result.value = alarm.timeInMillis
                }
            }
        } else {
            if (result.value == null) {
                for (i in day + 1..6) {
                    if (notifWeekDays[i]) {
                        nextDay = i
                        break
                    }
                }
                if (nextDay == null) {
                    for (i in 0..day) {
                        if (notifWeekDays[i]) {
                            nextDay = i
                            break
                        }
                    }
                }
                val nextAlarm = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, alarmTimes[0].get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, alarmTimes[0].get(Calendar.MINUTE))
                    if (nextDay != null) {
                        if (nextDay <= day) add(Calendar.DAY_OF_WEEK, 7)
                    }
                    set(Calendar.DAY_OF_WEEK, nextDay!! + 2) //adjust to Calendar days values
                }
                result.value = nextAlarm.timeInMillis
            }

        }
        return result
    }



    private fun getDayIndex(dayName: String): Int {
        return when (dayName) {
            "mon" -> 0
            "tue" -> 1
            "wed" -> 2
            "thu" -> 3
            "fri" -> 4
            "sat" -> 5
            "sun" -> 6
            else -> 0
        }
    }
}