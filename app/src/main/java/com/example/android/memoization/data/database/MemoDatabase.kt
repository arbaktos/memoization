package com.example.android.memoization.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.memoization.data.database.logdb.ReviewLogEntity
import com.example.android.memoization.data.database.logdb.SessionEntity
import com.example.android.memoization.data.database.sidedb.SideEntity
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity

@Database(
    entities = [
        StackEntity::class, WordPairEntity::class, SideEntity::class,
        SessionEntity::class, ReviewLogEntity::class,
    ],
    version = 5,
    exportSchema = true
)
abstract class MemoDatabase : RoomDatabase() {
    abstract val memoDao: MemoDao

    companion object {
        /**
         * Every schema change needs an entry here and a bumped version. Leaving this
         * empty and bumping the version makes Room throw on open, which is the point:
         * a crash is recoverable, a wiped vocabulary is not.
         */
        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

/**
 * The schedule moves off the word pair and onto its two sides, where FSRS keeps stability and
 * difficulty. The side the app has always shown - the word, with the meaning to recall -
 * inherits the pair's progress: its level becomes the stability in days it stood for, and its
 * next review lands where the old five-level schedule would have put it. The other side starts
 * New, because it has never been practised.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `side_entity_table` (
                `sideId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `wordPairId` INTEGER NOT NULL,
                `shown` INTEGER NOT NULL,
                `state` INTEGER NOT NULL,
                `stability` REAL,
                `difficulty` REAL,
                `due` INTEGER,
                `lastReview` INTEGER,
                `reps` INTEGER NOT NULL,
                `lapses` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_side_entity_table_wordPairId` " +
                "ON `side_entity_table` (`wordPairId`)"
        )
        db.execSQL(
            """
            INSERT INTO `side_entity_table`
                (wordPairId, shown, state, stability, difficulty, due, lastReview, reps, lapses)
            SELECT wordPairId, $SHOWN_WORD,
                CASE WHEN lastRep IS NULL THEN $STATE_NEW ELSE $STATE_REVIEW END,
                CASE WHEN lastRep IS NULL THEN NULL ELSE $LEVEL_DAYS END,
                CASE WHEN lastRep IS NULL THEN NULL ELSE $DIFFICULTY_OF_A_GOOD_ANSWER END,
                CASE WHEN lastRep IS NULL THEN NULL ELSE lastRep + $LEVEL_DAYS * $DAY_MILLIS END,
                lastRep,
                CASE WHEN lastRep IS NULL THEN 0 ELSE 1 END,
                0
            FROM `wordpair_entity_table` ORDER BY wordPairId
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `side_entity_table`
                (wordPairId, shown, state, stability, difficulty, due, lastReview, reps, lapses)
            SELECT wordPairId, $SHOWN_MEANING, $STATE_NEW, NULL, NULL, NULL, NULL, 0, 0
            FROM `wordpair_entity_table` ORDER BY wordPairId
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `wordpair_entity_table_new` (
                `parentStackId` INTEGER NOT NULL,
                `word1` TEXT NOT NULL,
                `word2` TEXT,
                `wordPairId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `isVisible` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `wordpair_entity_table_new`
                (parentStackId, word1, word2, wordPairId, isVisible)
            SELECT parentStackId, word1, word2, wordPairId, isVisible
            FROM `wordpair_entity_table`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `wordpair_entity_table`")
        db.execSQL("ALTER TABLE `wordpair_entity_table_new` RENAME TO `wordpair_entity_table`")
    }
}

/**
 * The review log: a row per session and a row per answer, for the statistics. Purely additive -
 * nothing existing is touched - and history starts here; answers given before this version
 * have no rows, only the counts the sides already carry.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `session_table` (
                `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `stackId` INTEGER NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `finishedAt` INTEGER,
                `sidesOffered` INTEGER NOT NULL,
                `sidesWaiting` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `review_log_table` (
                `reviewId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `sessionId` INTEGER NOT NULL,
                `sideId` INTEGER NOT NULL,
                `ratedAt` INTEGER NOT NULL,
                `shownMs` INTEGER NOT NULL,
                `rating` INTEGER NOT NULL,
                `requeued` INTEGER NOT NULL,
                `stabilityAfter` REAL,
                `difficultyAfter` REAL,
                `dueAfter` INTEGER
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_log_table_sessionId` ON `review_log_table` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_log_table_sideId` ON `review_log_table` (`sideId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_log_table_ratedAt` ON `review_log_table` (`ratedAt`)")
    }
}

/**
 * Sessions lose their size and gain an ending. Each stack keeps the running mean of where the
 * learner says "Enough for today" (two counters, see Stamina); a session records how it ended
 * (SessionEnding) and how many sides it closed. Additive: no stack has a stop recorded yet, and
 * a session finished before this version could only have drained its queue, so it is marked
 * so with every side it was given closed. A session left open before this version stays open:
 * whether it was walked out of or killed is not known.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `stack_entity_table` ADD COLUMN `tiredSessions` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `stack_entity_table` ADD COLUMN `tiredAnswersSum` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `session_table` ADD COLUMN `sidesDone` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `session_table` ADD COLUMN `ending` INTEGER")
        db.execSQL(
            "UPDATE `session_table` SET `ending` = $ENDING_DRAINED, `sidesDone` = `sidesOffered` " +
                "WHERE `finishedAt` IS NOT NULL"
        )
    }
}

/** SessionEnding.DRAINED.code as it was at version 5; SessionEndingTest pins the two together. */
private const val ENDING_DRAINED = 1
const val MIGRATION_ENDING_DRAINED_FOR_TEST = ENDING_DRAINED

private const val SHOWN_WORD = 0
private const val SHOWN_MEANING = 1
private const val STATE_NEW = 0
private const val STATE_REVIEW = 1
private const val DAY_MILLIS = 86_400_000L

/**
 * The five levels stood for 1, 2, 7, 14 and 30 days between repetitions; that is what
 * stability means in FSRS, so the numbers carry straight over.
 */
private const val LEVEL_DAYS =
    "(CASE level WHEN 1 THEN 1 WHEN 2 THEN 2 WHEN 3 THEN 7 WHEN 4 THEN 14 WHEN 5 THEN 30 ELSE 1 END)"

/**
 * Migrated sides are treated as if their last answer had been "Good"; WordStatusTest pins this
 * to Fsrs.initialDifficulty(Rating.Good) so the two cannot drift apart.
 */
private const val DIFFICULTY_OF_A_GOOD_ANSWER = 2.11810397045901

/** Visible for tests only. */
const val MIGRATION_D0_GOOD_FOR_TEST = DIFFICULTY_OF_A_GOOD_ANSWER
