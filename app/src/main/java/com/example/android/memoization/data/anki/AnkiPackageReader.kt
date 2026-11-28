package com.example.android.memoization.data.anki

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.github.luben.zstd.ZstdInputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.inject.Inject

/** The file is not an Anki package, or one this app cannot read. */
class NotAnAnkiPackageException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Reads an Anki package (.apkg, or a whole collection's .colpkg) of any version since 2.0: the
 * collection - schema 11 with its note types and decks as JSON, or schema 18 with tables of
 * their own, zstd-compressed since 2.1.50 - and its media, which is copied into the app as is.
 */
class AnkiPackageReader @Inject constructor(@ApplicationContext private val context: Context) {

    /** Where imported media lives, under the names the notes' HTML uses for it. */
    val mediaDir: File get() = File(context.filesDir, MEDIA_DIR)

    fun read(uri: Uri): AnkiCollection {
        val work = File(context.cacheDir, "anki_import").apply { deleteRecursively(); mkdirs() }
        try {
            val packageFile = File(work, "package.zip")
            val input = context.contentResolver.openInputStream(uri)
                ?: throw NotAnAnkiPackageException("Cannot open $uri")
            input.use { src -> packageFile.outputStream().use { src.copyTo(it) } }
            return read(packageFile, work)
        } finally {
            work.deleteRecursively()
        }
    }

    /** Visible for tests: reads a package file, using [work] for what has to be unpacked. */
    fun read(packageFile: File, work: File): AnkiCollection {
        val zip = try {
            ZipFile(packageFile)
        } catch (e: ZipException) {
            throw NotAnAnkiPackageException("Not a zip", e)
        }
        zip.use {
            // The newest the package has; older ones are there for older Ankis, the oldest
            // often just a note saying to update.
            val entry = COLLECTION_ENTRIES.firstNotNullOfOrNull { name -> zip.getEntry(name) }
                ?: throw NotAnAnkiPackageException("No collection in the package")
            val compressed = entry.name == MODERN_COLLECTION
            val dbFile = File(work, "collection.sqlite")
            zip.getInputStream(entry).use { src -> unpack(src).use { s -> dbFile.outputStream().use { s.copyTo(it) } } }

            val collection = try {
                readCollection(dbFile)
            } catch (e: NotAnAnkiPackageException) {
                throw e
            } catch (e: Exception) {
                throw NotAnAnkiPackageException("Unreadable collection", e)
            }
            val media = copyMedia(zip, compressed)
            return collection.copy(mediaFiles = media)
        }
    }

    /** Anything zstd-compressed is unpacked on the way; anything else passes through. */
    private fun unpack(src: InputStream): InputStream {
        val buffered = src.buffered()
        buffered.mark(4)
        val head = ByteArray(4)
        val read = buffered.read(head)
        buffered.reset()
        return if (read == 4 && AnkiFormat.isZstd(head)) ZstdInputStream(buffered) else buffered
    }

    private fun readCollection(file: File): AnkiCollection {
        val db = SQLiteDatabase.openDatabase(
            file.path, null, SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        )
        db.use {
            val created = db.longQuery("SELECT crt FROM col") ?: 0L
            val modern = db.longQuery("SELECT count(*) FROM sqlite_master WHERE type = 'table' AND name = 'notetypes'") == 1L
            val noteTypes: Map<Long, AnkiNoteType>
            val decks: Map<Long, String>
            if (modern) {
                val fieldNames = mutableMapOf<Long, MutableList<Pair<Int, String>>>()
                db.rawQuery("SELECT ntid, ord, name FROM fields", null).use { c ->
                    while (c.moveToNext()) fieldNames.getOrPut(c.getLong(0)) { mutableListOf() } += c.getInt(1) to c.getString(2)
                }
                noteTypes = buildMap {
                    db.rawQuery("SELECT id, name, config FROM notetypes", null).use { c ->
                        while (c.moveToNext()) {
                            val id = c.getLong(0)
                            put(id, AnkiNoteType(
                                id = id,
                                name = c.getString(1),
                                kind = AnkiFormat.noteTypeKind(c.getBlob(2)),
                                fieldNames = fieldNames[id].orEmpty().sortedBy { it.first }.map { it.second },
                            ))
                        }
                    }
                }
                decks = buildMap {
                    db.rawQuery("SELECT id, name FROM decks", null).use { c ->
                        while (c.moveToNext()) put(c.getLong(0), AnkiFormat.deckName(c.getString(1)))
                    }
                }
            } else {
                val (models, deckJson) = db.rawQuery("SELECT models, decks FROM col", null).use { c ->
                    if (!c.moveToFirst()) throw NotAnAnkiPackageException("Empty col table")
                    c.getString(0) to c.getString(1)
                }
                noteTypes = AnkiFormat.legacyNoteTypes(models)
                decks = AnkiFormat.legacyDecks(deckJson)
            }

            val notes = buildList {
                db.rawQuery("SELECT id, guid, mid, flds, tags FROM notes", null).use { c ->
                    while (c.moveToNext()) add(AnkiNote(
                        id = c.getLong(0),
                        guid = c.getString(1),
                        noteTypeId = c.getLong(2),
                        fields = c.getString(3).split(AnkiFormat.FIELD_SEPARATOR),
                        tags = c.getString(4).trim(),
                    ))
                }
            }
            val cards = buildList {
                db.rawQuery(
                    "SELECT id, nid, did, ord, type, queue, due, ivl, factor, reps, lapses, odue, odid, data FROM cards",
                    null
                ).use { c ->
                    while (c.moveToNext()) add(AnkiCard(
                        id = c.getLong(0), noteId = c.getLong(1), deckId = c.getLong(2), ord = c.getInt(3),
                        type = c.getInt(4), queue = c.getInt(5), due = c.getLong(6), ivl = c.getInt(7),
                        factor = c.getInt(8), reps = c.getInt(9), lapses = c.getInt(10),
                        originalDue = c.getLong(11), originalDeckId = c.getLong(12),
                        data = if (c.isNull(13)) null else c.getString(13),
                    ))
                }
            }
            val reviews = buildList {
                db.rawQuery("SELECT id, cid, ease, ivl, lastIvl, factor, time, type FROM revlog", null).use { c ->
                    while (c.moveToNext()) add(AnkiReview(
                        id = c.getLong(0), cardId = c.getLong(1), ease = c.getInt(2), ivl = c.getInt(3),
                        lastIvl = c.getInt(4), factor = c.getInt(5), timeMs = c.getInt(6), type = c.getInt(7),
                    ))
                }
            }
            return AnkiCollection(created, decks, noteTypes, notes, cards, reviews)
        }
    }

    /**
     * Copies the media files under their collection names. Names are flat in Anki and the same
     * name is the same file there, so one already here is left as it is.
     */
    private fun copyMedia(zip: ZipFile, compressed: Boolean): Int {
        val mapEntry = zip.getEntry(MEDIA_MAP) ?: return 0
        val mapBytes = zip.getInputStream(mapEntry).use { src -> unpack(src).use { it.readBytes() } }
        val map = runCatching { AnkiFormat.mediaMap(mapBytes) }.getOrElse { return 0 }
        if (map.isEmpty()) return 0
        val dir = mediaDir.apply { mkdirs() }
        var copied = 0
        for ((zipName, name) in map) {
            val entry = zip.getEntry(zipName) ?: continue
            if (name.isBlank() || name.contains('/') || name.contains('\\') || name == "." || name == "..") continue
            val target = File(dir, name)
            if (!target.exists()) {
                val tmp = File(dir, "$name.part")
                zip.getInputStream(entry).use { src ->
                    (if (compressed) unpack(src) else src).use { s -> tmp.outputStream().use { s.copyTo(it) } }
                }
                tmp.renameTo(target)
            }
            copied++
        }
        return copied
    }

    private fun SQLiteDatabase.longQuery(sql: String): Long? =
        rawQuery(sql, null).use { c -> if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else null }

    companion object {
        const val MEDIA_DIR = "anki_media"
        private const val MODERN_COLLECTION = "collection.anki21b"
        private val COLLECTION_ENTRIES = listOf(MODERN_COLLECTION, "collection.anki21", "collection.anki2")
        private const val MEDIA_MAP = "media"
    }
}
