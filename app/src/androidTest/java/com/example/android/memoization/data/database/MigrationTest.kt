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

        val db = helper.runMigrationsAndValidate(testDb, 3, true, *MemoDatabase.MIGRATIONS)

        db.query("SELECT stability FROM side_entity_table WHERE wordPairId = 1 AND shown = 0").use {
            it.moveToFirst()
            assertEquals(7.0, it.getDouble(0), 1e-9)
        }
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
