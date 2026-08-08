package com.example.android.memoization.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
}