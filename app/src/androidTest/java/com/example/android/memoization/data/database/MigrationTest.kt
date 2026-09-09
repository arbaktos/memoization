package com.example.android.memoization.data.database

import android.content.ContentValues
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The learning database is never wiped, so every migration is checked against a real database
 * built at the old version - the one case where a mistake costs the learner their progress.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"
    private val lastRep = 1_760_000_000_000L
    private val dayMillis = 86_400_000L

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MemoDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migration2To3_movesTheScheduleOntoSides() {
        helper.createDatabase(testDb, 2).use { db ->
            db.insert("stack_entity_table", 0, ContentValues().apply {
                put("name", "Serbian")
                put("numRep", 0)
                put("stackId", 1L)
                put("hasWords", 1)
                put("isVisible", 1)
            })
            // A pair never practised, one at Level3, one at Learned, one already hidden.
            insertPairV2(db, id = 1, level = 1, lastRep = null, isVisible = 1)
            insertPairV2(db, id = 2, level = 3, lastRep = lastRep, isVisible = 1)
            insertPairV2(db, id = 3, level = 5, lastRep = lastRep, isVisible = 1)
            insertPairV2(db, id = 4, level = 2, lastRep = lastRep, isVisible = 0)
        }

        val db = helper.runMigrationsAndValidate(testDb, 3, true, MIGRATION_2_3)

        db.query("SELECT COUNT(*) FROM side_entity_table").use {
            it.moveToFirst()
            assertEquals("every pair gets both sides", 8, it.getInt(0))
        }

        // The side the app has always shown inherits the old schedule.
        db.query(
            "SELECT state, stability, difficulty, due, lastReview, reps, lapses " +
                "FROM side_entity_table WHERE wordPairId = 2 AND shown = 0"
        ).use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
            assertEquals(7.0, it.getDouble(1), 1e-9)
            assertEquals(MIGRATION_D0_GOOD_FOR_TEST, it.getDouble(2), 1e-9)
            assertEquals(lastRep + 7 * dayMillis, it.getLong(3))
            assertEquals(lastRep, it.getLong(4))
            assertEquals(1, it.getInt(5))
            assertEquals(0, it.getInt(6))
        }
        db.query("SELECT stability, due FROM side_entity_table WHERE wordPairId = 3 AND shown = 0").use {
            it.moveToFirst()
            assertEquals(30.0, it.getDouble(0), 1e-9)
            assertEquals(lastRep + 30 * dayMillis, it.getLong(1))
        }

        // A pair that was never practised starts New, and so does every meaning side.
        db.query(
            "SELECT state, stability, due, lastReview, reps FROM side_entity_table " +
                "WHERE wordPairId = 1 AND shown = 0"
        ).use {
            it.moveToFirst()
            assertEquals(0, it.getInt(0))
            assertTrue(it.isNull(1))
            assertTrue(it.isNull(2))
            assertTrue(it.isNull(3))
            assertEquals(0, it.getInt(4))
        }
        db.query("SELECT COUNT(*) FROM side_entity_table WHERE shown = 1 AND state = 0").use {
            it.moveToFirst()
            assertEquals(4, it.getInt(0))
        }

        // The pair keeps its id and its visibility, and loses the schedule columns.
        db.query("SELECT wordPairId, word1, isVisible FROM wordpair_entity_table ORDER BY wordPairId").use {
            assertEquals(4, it.count)
            it.moveToFirst()
            assertEquals(1L, it.getLong(0))
            assertEquals("word1", it.getString(1))
            it.moveToPosition(3)
            assertEquals(4L, it.getLong(0))
            assertEquals(0, it.getInt(2))
        }
        db.query("PRAGMA table_info(wordpair_entity_table)").use {
            val columns = mutableListOf<String>()
            while (it.moveToNext()) columns += it.getString(it.getColumnIndexOrThrow("name"))
            assertFalse(columns.contains("level"))
            assertFalse(columns.contains("lastRep"))
        }
    }

    @Test
    fun everyMigrationRunsFromTheFirstVersion() {
        helper.createDatabase(testDb, 1).use { db ->
            db.execSQL(
                "INSERT INTO wordpair_entity_table " +
                    "(parentStackId, word1, word2, lastRep, toShow, level, wordPairId, isVisible) " +
                    "VALUES (1, 'kuca', 'house', $lastRep, 0, 3, 1, 1)"
            )
        }

        val db = helper.runMigrationsAndValidate(testDb, 5, true, *MemoDatabase.MIGRATIONS)

        db.query("SELECT stability FROM side_entity_table WHERE wordPairId = 1 AND shown = 0").use {
            it.moveToFirst()
            assertEquals(7.0, it.getDouble(0), 1e-9)
        }
    }

    @Test
    fun migration3To4_addsTheReviewLogAndTouchesNothingElse() {
        helper.createDatabase(testDb, 3).use { db ->
            db.insert("stack_entity_table", 0, ContentValues().apply {
                put("name", "Serbian"); put("numRep", 0); put("stackId", 1L)
                put("hasWords", 1); put("isVisible", 1)
            })
            db.execSQL(
                "INSERT INTO wordpair_entity_table (parentStackId, word1, word2, wordPairId, isVisible) " +
                    "VALUES (1, 'kuca', 'house', 1, 1)"
            )
            db.execSQL(
                "INSERT INTO side_entity_table (sideId, wordPairId, shown, state, stability, difficulty, due, lastReview, reps, lapses) " +
                    "VALUES (1, 1, 0, 1, 7.0, 5.0, $lastRep, $lastRep, 3, 1)"
            )
        }

        val db = helper.runMigrationsAndValidate(testDb, 4, true, MIGRATION_3_4)

        // Existing rows are exactly as they were.
        db.query("SELECT stability, reps, lapses FROM side_entity_table WHERE sideId = 1").use {
            it.moveToFirst()
            assertEquals(7.0, it.getDouble(0), 1e-9)
            assertEquals(3, it.getInt(1))
            assertEquals(1, it.getInt(2))
        }
        // The log starts empty: history begins with this version.
        db.query("SELECT COUNT(*) FROM session_table").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
        db.query("SELECT COUNT(*) FROM review_log_table").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
    }

    @Test
    fun migration4To5_givesEveryStackAnEmptyStopRecordAndTouchesNothingElse() {
        helper.createDatabase(testDb, 4).use { db ->
            db.insert("stack_entity_table", 0, ContentValues().apply {
                put("name", "Serbian"); put("numRep", 0); put("stackId", 1L)
                put("hasWords", 1); put("isVisible", 1); put("pinnedTime", lastRep)
            })
            db.execSQL(
                "INSERT INTO session_table (sessionId, stackId, startedAt, finishedAt, sidesOffered, sidesWaiting) " +
                    "VALUES (1, 1, $lastRep, NULL, 37, 4), (2, 1, $lastRep, ${lastRep + 60_000}, 12, 0)"
            )
        }

        val db = helper.runMigrationsAndValidate(testDb, 5, true, MIGRATION_4_5)

        db.query("SELECT name, pinnedTime, tiredSessions, tiredAnswersSum FROM stack_entity_table WHERE stackId = 1").use {
            it.moveToFirst()
            assertEquals("Serbian", it.getString(0))
            assertEquals(lastRep, it.getLong(1))
            assertEquals(0, it.getInt(2))
            assertEquals(0, it.getInt(3))
        }
        // An old session left open stays open: how it ended is not known.
        db.query("SELECT finishedAt, ending, sidesDone, sidesWaiting FROM session_table WHERE sessionId = 1").use {
            it.moveToFirst()
            assertTrue(it.isNull(0))
            assertTrue(it.isNull(1))
            assertEquals(0, it.getInt(2))
            assertEquals(4, it.getInt(3))
        }
        // An old finished session could only have drained its queue.
        db.query("SELECT ending, sidesDone, sidesWaiting FROM session_table WHERE sessionId = 2").use {
            it.moveToFirst()
            assertEquals(MIGRATION_ENDING_DRAINED_FOR_TEST, it.getInt(0))
            assertEquals(12, it.getInt(1))
            assertEquals(0, it.getInt(2))
        }
    }

    @Test
    fun stoppingAStackAddsUpInPlace() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MemoDatabase::class.java
        ).build()
        val stackId = db.memoDao.insertStack(
            com.example.android.memoization.data.database.stackdb.StackEntity(name = "Serbian")
        )

        db.memoDao.recordStoppedAt(stackId, 30)
        db.memoDao.recordStoppedAt(stackId, 36)

        val stack = db.memoDao.getStackById(stackId)
        assertEquals(2, stack.tiredSessions)
        assertEquals(66, stack.tiredAnswersSum)
        db.close()
    }

    @Test
    fun aSessionAndItsAnswersRoundTripThroughTheLog() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MemoDatabase::class.java
        ).build()

        val sessionId = db.memoDao.insertSession(
            com.example.android.memoization.data.database.logdb.SessionEntity(
                stackId = 1, startedAt = lastRep, sidesOffered = 2
            )
        )
        db.memoDao.insertReview(
            com.example.android.memoization.data.database.logdb.ReviewLogEntity(
                sessionId = sessionId, sideId = 1, ratedAt = lastRep + 4_000, shownMs = 4_000,
                rating = 1, requeued = false, stabilityAfter = 0.3, difficultyAfter = 6.0,
                dueAfter = lastRep + dayMillis,
            )
        )
        db.memoDao.insertReview(
            com.example.android.memoization.data.database.logdb.ReviewLogEntity(
                sessionId = sessionId, sideId = 1, ratedAt = lastRep + 9_000, shownMs = 2_000,
                rating = 3, requeued = true, stabilityAfter = 0.3, difficultyAfter = 6.0,
                dueAfter = null,
            )
        )
        db.memoDao.finishSession(sessionId, lastRep + 10_000, ending = 2, sidesDone = 1, sidesWaiting = 1)

        val session = db.memoDao.getSession(sessionId)!!
        assertEquals(lastRep + 10_000, session.finishedAt)
        assertEquals(2, session.ending)
        assertEquals(1, session.sidesDone)
        assertEquals(1, session.sidesWaiting)
        val reviews = db.memoDao.getReviewsOfSession(sessionId)
        assertEquals(2, reviews.size)
        assertFalse(reviews[0].requeued)
        assertTrue(reviews[1].requeued)
        assertNull(reviews[1].dueAfter)
        assertEquals(lastRep + dayMillis, reviews[0].dueAfter)
        db.close()
    }

    @Test
    fun aNewPairIsInsertedWithBothItsSides() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MemoDatabase::class.java
        ).build()

        val id = db.memoDao.insertWordPairWithSides(
            com.example.android.memoization.data.database.wordpairdb.WordPairEntity(
                parentStackId = 1, word1 = "kuca", word2 = "house"
            )
        )
        val sides = db.memoDao.getSidesForPair(id)

        assertEquals(2, sides.size)
        assertEquals(listOf(0, 1), sides.map { it.shown })
        assertTrue(sides.all { it.state == 0 })
        assertNull(sides.first().stability)
        db.close()
    }

    private fun insertPairV2(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        id: Long,
        level: Int,
        lastRep: Long?,
        isVisible: Int,
    ) {
        db.insert("wordpair_entity_table", 0, ContentValues().apply {
            put("parentStackId", 1L)
            put("word1", "word$id")
            put("word2", "meaning$id")
            if (lastRep == null) putNull("lastRep") else put("lastRep", lastRep)
            put("level", level)
            put("wordPairId", id)
            put("isVisible", isVisible)
        })
    }
}
