package com.example.android.memoization.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
                    "memo_dataabase"
                ).build()
            }
            return instance as MemoDatabase
//            return instance ?: synchronized(this) {
//                instance ?: buildDatabase(context).also { instance = it }
//            }
        }

//        private fun buildDatabase(context: Context) =
//            Room.databaseBuilder(context, MemoDatabase::class.java, "userdb")
//                .build()
//    }
    }
}