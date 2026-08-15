package com.example.android.memoization.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity

@Database(
    entities = [StackEntity::class, WordPairEntity::class],
    version = 2,
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
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}

/**
 * Word pairs gain the New state (lastRep NULL = never repeated) and lose the dead toShow
 * column. SQLite cannot relax NOT NULL or drop a column in place, so the table is rebuilt.
 * Rows carry their ids across, and inserting explicit ids advances sqlite_sequence, so no id
 * is ever reused. Existing rows keep their lastRep - nothing becomes New retroactively.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `wordpair_entity_table_new` (
                `parentStackId` INTEGER NOT NULL,
                `word1` TEXT NOT NULL,
                `word2` TEXT,
                `lastRep` INTEGER,
                `level` INTEGER NOT NULL,
                `wordPairId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `isVisible` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `wordpair_entity_table_new`
                (parentStackId, word1, word2, lastRep, level, wordPairId, isVisible)
            SELECT parentStackId, word1, word2, lastRep, level, wordPairId, isVisible
            FROM `wordpair_entity_table`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `wordpair_entity_table`")
        db.execSQL("ALTER TABLE `wordpair_entity_table_new` RENAME TO `wordpair_entity_table`")
    }
}
