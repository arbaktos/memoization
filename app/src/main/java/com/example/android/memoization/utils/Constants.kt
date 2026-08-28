package com.example.android.memoization.utils


const val Default_folder_ID: Long = -1

const val STACK_ID = "stackId"

// Navigation argument keys - must match the destination property names
const val WORD_PAIR_ID = "wordPairId"
const val FROM_LANGUAGE = "fromLanguage"
const val TO_LANGUAGE = "toLanguage"
const val WORD = "word"

/**
 * "No stack named" in a navigation argument: a route argument is a primitive, and a nullable
 * Long is not one of the types navigation knows how to carry.
 */
const val NO_STACK_ID = -1L

const val TAG = "debug"
const val Empty_string = ""

object NotifConstants {
    const val ALARM_REQUEST_CODE = 367
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_ID_LABEL = "notificationId"
}
object SettingsDefaults {
    const val MIN_TIMES_A_DAY = 1
    const val MAX_TIMES_A_DAY = 4
    const val DEFAULT_HOUR_NOTIFICATION = 12
    const val DEFAULT_MINUTE_NOTIFICATION = 0
}