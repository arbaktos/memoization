package com.example.android.memoization.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.android.memoization.utils.Db_name

@Database(
    entities = [FolderEntity::class, StackEntity::class, WordPairEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract val memoDao: MemoDao

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: MemoDatabase? = null

        fun getInstance(context: Context): MemoDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    MemoDatabase::class.java,
                    Db_name
                ).build()
            }
            return instance as MemoDatabase
        }
    }
}