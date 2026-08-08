package com.example.android.memoization.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity

@Database(
    entities = [StackEntity::class, WordPairEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract val memoDao: MemoDao

    companion object {
        /**
         * Every schema change needs an entry here and a bumped version. Leaving this
         * empty and bumping the version makes Room throw on open, which is the point:
         * a crash is recoverable, a wiped vocabulary is not.
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
