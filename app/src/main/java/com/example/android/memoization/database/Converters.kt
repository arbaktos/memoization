package com.example.android.memoization.database

import androidx.room.TypeConverter
import com.example.android.memoization.domain.model.WordStatus
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }


    @TypeConverter
    fun intToLevel(level: Int): WordStatus {
        return when (level) {
            1 -> WordStatus.Level1()
            2 -> WordStatus.Level2()
            3 -> WordStatus.Level3()
            4 -> WordStatus.Level4()
            5 -> WordStatus.Learned()
            else -> WordStatus.Level1()
        }
    }

    @TypeConverter
    fun levelToInt(level: WordStatus): Int {
        return when (level) {
            is WordStatus.Level1 -> 1
            is WordStatus.Level2 -> 2
            is WordStatus.Level3 -> 3
            is WordStatus.Level4 -> 4
            is WordStatus.Learned -> 5
        }
    }
}
