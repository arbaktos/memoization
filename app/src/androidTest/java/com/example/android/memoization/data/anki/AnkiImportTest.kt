package com.example.android.memoization.data.anki

import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.memoization.data.database.MemoDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Imports real Anki packages, made by Anki 25.9 itself (assets/anki/make_decks.py.txt says how):
 * one in the current format - schema 18, zstd, protobuf media list - and one exported for older
 * Ankis - schema 11, plain. Both hold the same two decks: reviewed cards with FSRS memory, one
 * with SM-2 only and suspended, one relearning, new ones, HTML, an image, a sound, a cloze.
 */
@RunWith(AndroidJUnit4::class)
class AnkiImportTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var dir: File
    private lateinit var db: MemoDatabase
    private lateinit var reader: AnkiPackageReader
    private lateinit var importer: AnkiImporter

    // Created in Anki at 1795834800 (its day start); "today" there was day 0.
    private val created = 1_795_834_800L
    private val dayMillis = 86_400_000L
    private val now = created * 1000 + 10 * 3_600_000L

    @Before
    fun setUp() {
        val target = instrumentation.targetContext
        dir = File(target.cacheDir, "anki-test").apply { deleteRecursively(); mkdirs() }
        // Media goes to a folder of the test's own, not the app's.
        val context = object : ContextWrapper(target) {
            override fun getFilesDir(): File = File(dir, "files").apply { mkdirs() }
        }
        db = Room.inMemoryDatabaseBuilder(target, MemoDatabase::class.java).build()
        reader = AnkiPackageReader(context)
        importer = AnkiImporter(reader, db, db.memoDao, db.ankiDao)
    }

    @After
    fun tearDown() {
        db.close()
        dir.deleteRecursively()
    }

    private fun read(asset: String): AnkiCollection {
        val file = File(dir, asset.substringAfterLast('/'))
        instrumentation.context.assets.open(asset).use { src -> file.outputStream().use { src.copyTo(it) } }
        val work = File(dir, "work").apply { mkdirs() }
        return reader.read(file, work)
    }

    private fun strings(sql: String): List<String> = db.openHelper.readableDatabase.query(sql).use { c ->
        buildList { while (c.moveToNext()) add(c.getString(0)) }
    }

    private fun long(sql: String): Long = db.openHelper.readableDatabase.query(sql).use { c ->
        c.moveToFirst(); c.getLong(0)
    }

    private data class SideRow(val state: Int, val stability: Double?, val difficulty: Double?, val due: Long?, val lastReview: Long?, val reps: Int, val lapses: Int)

    private fun side(word: String, shown: Int): SideRow = db.openHelper.readableDatabase.query(
        "SELECT s.state, s.stability, s.difficulty, s.due, s.lastReview, s.reps, s.lapses FROM side_entity_table s " +
            "JOIN wordpair_entity_table w ON w.wordPairId = s.wordPairId WHERE w.word1 = ? AND s.shown = ?",
        arrayOf(word, shown)
    ).use { c ->
        assertTrue("no side $shown of $word", c.moveToFirst())
        SideRow(
            c.getInt(0),
            if (c.isNull(1)) null else c.getDouble(1),
            if (c.isNull(2)) null else c.getDouble(2),
            if (c.isNull(3)) null else c.getLong(3),
            if (c.isNull(4)) null else c.getLong(4),
            c.getInt(5), c.getInt(6),
        )
    }

    @Test
    fun currentFormat_makesAStackPerDeckAndAPairPerWordNote() = runBlocking {
        val collection = read("anki/modern.apkg")
        assertEquals(2, collection.mediaFiles)

        val result = importer.import(collection, now)

        assertEquals(5, result.added)
        assertEquals(0, result.updated)
        assertEquals("the cloze and the picture-only note wait", 2, result.keptForLater)
        assertEquals(listOf("Испанский", "Испанский · Глаголы"), strings("SELECT name FROM stack_entity_table ORDER BY name"))
        assertEquals(
            listOf("comer", "hablar", "vivir"),
            strings(
                "SELECT word1 FROM wordpair_entity_table w JOIN stack_entity_table s ON s.stackId = w.parentStackId " +
                    "WHERE s.name = 'Испанский · Глаголы' ORDER BY word1"
            )
        )
        assertEquals(listOf("говорить\nразговаривать"), strings("SELECT word2 FROM wordpair_entity_table WHERE word1 = 'hablar'"))
        assertEquals(listOf("есть"), strings("SELECT word2 FROM wordpair_entity_table WHERE word1 = 'comer'"))
        assertEquals(listOf("кот"), strings("SELECT word2 FROM wordpair_entity_table WHERE word1 = 'el gato'"))
        assertEquals(listOf("да"), strings("SELECT word2 FROM wordpair_entity_table WHERE word1 = 'a & b <c>'"))
    }

    @Test
    fun currentFormat_carriesTheProgressOver() = runBlocking {
        importer.import(read("anki/modern.apkg"), now)

        // FSRS memory taken as it is; the last review from Anki's log.
        val hablar = side("hablar", 0)
        assertEquals(1, hablar.state)
        assertEquals(14.25, hablar.stability!!, 1e-9)
        assertEquals(6.5, hablar.difficulty!!, 1e-9)
        assertEquals((created + 5 * 86_400L) * 1000, hablar.due)
        assertEquals(6, hablar.reps)
        assertEquals(1, hablar.lapses)
        assertNotNull(hablar.lastReview)
        assertTrue(hablar.lastReview!! < now)
        // The reversed card is the meaning side.
        val hablarBack = side("hablar", 1)
        assertEquals(3.5, hablarBack.stability!!, 1e-9)
        assertEquals((created - 86_400L) * 1000, hablarBack.due)

        // SM-2 only: the interval for stability, the lowest ease for the hardest.
        val comer = side("comer", 0)
        assertEquals(30.0, comer.stability!!, 1e-9)
        assertEquals(10.0, comer.difficulty!!, 1e-9)
        assertEquals(comer.due!! - 30 * dayMillis, comer.lastReview)
        assertEquals(0, side("comer", 1).state)

        // Relearning: due in seconds, today.
        val vivir = side("vivir", 0)
        assertEquals(2.0, vivir.stability!!, 1e-9)
        assertTrue(vivir.due!! in created * 1000..created * 1000 + 2 * dayMillis)

        // New in Anki, New here.
        assertEquals(0, side("el gato", 0).state)
    }

    @Test
    fun currentFormat_keepsEverythingWholeAndCopiesTheMedia() = runBlocking {
        importer.import(read("anki/modern.apkg"), now)

        assertEquals(7L, long("SELECT COUNT(*) FROM anki_note_table"))
        assertEquals(10L, long("SELECT COUNT(*) FROM anki_card_table"))
        assertEquals(1L, long("SELECT COUNT(*) FROM anki_revlog_table"))
        val cloze = strings("SELECT fields FROM anki_note_table WHERE noteTypeKind = 1")
        assertEquals(1, cloze.size)
        assertTrue(cloze[0].contains("{{c1::Мадрид}}"))
        assertEquals(listOf("[\"Front\",\"Back\"]"), strings("SELECT DISTINCT fieldNames FROM anki_note_table WHERE noteType = 'Basic'"))
        assertEquals(2L, long("SELECT COUNT(*) FROM anki_note_table WHERE wordPairId IS NULL"))
        assertEquals(0L, long("SELECT COUNT(*) FROM anki_card_table c JOIN anki_note_table n ON n.guid = c.noteGuid WHERE n.wordPairId IS NOT NULL AND c.ord < 2 AND c.sideId IS NULL"))

        val image = File(reader.mediaDir, "gato.png")
        assertArrayEquals(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte()), image.readBytes().copyOf(4))
        assertEquals(8 + 7 * 10, image.length().toInt())
        assertTrue(File(reader.mediaDir, "hablar.mp3").readBytes().decodeToString().startsWith("ID3fakemp3"))
    }

    @Test
    fun importingAgain_updatesInsteadOfAddingAndKeepsOurProgress() = runBlocking {
        importer.import(read("anki/modern.apkg"), now)
        db.openHelper.writableDatabase.execSQL(
            "UPDATE side_entity_table SET stability = 99 WHERE wordPairId = (SELECT wordPairId FROM wordpair_entity_table WHERE word1 = 'hablar') AND shown = 0"
        )

        val again = importer.import(read("anki/modern.apkg"), now + dayMillis)

        assertEquals(0, again.added)
        assertEquals(5, again.updated)
        assertEquals(5L, long("SELECT COUNT(*) FROM wordpair_entity_table"))
        assertEquals(2L, long("SELECT COUNT(*) FROM stack_entity_table"))
        assertEquals(7L, long("SELECT COUNT(*) FROM anki_note_table"))
        assertEquals(10L, long("SELECT COUNT(*) FROM anki_card_table"))
        assertEquals(1L, long("SELECT COUNT(*) FROM anki_revlog_table"))
        assertEquals(99.0, side("hablar", 0).stability!!, 1e-9)
    }

    @Test
    fun olderFormat_readsTheSame() = runBlocking {
        val collection = read("anki/legacy.apkg")
        assertEquals(2, collection.mediaFiles)

        val result = importer.import(collection, now)

        assertEquals(5, result.added)
        assertEquals(2, result.keptForLater)
        assertEquals(listOf("Испанский", "Испанский · Глаголы"), strings("SELECT name FROM stack_entity_table ORDER BY name"))
        assertEquals(1, side("hablar", 0).state)
        assertEquals(30.0, side("comer", 0).stability!!, 1e-9)
        assertTrue(File(reader.mediaDir, "gato.png").exists())
    }

    @Test(expected = NotAnAnkiPackageException::class)
    fun notAPackage_saysSo() {
        val file = File(dir, "notes.txt").apply { writeText("hablar\tговорить") }
        reader.read(file, File(dir, "work").apply { mkdirs() })
    }
}
