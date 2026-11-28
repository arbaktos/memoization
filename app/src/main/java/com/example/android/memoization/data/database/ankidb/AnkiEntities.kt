package com.example.android.memoization.data.database.ankidb

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames

/**
 * An imported Anki note kept whole - every field as HTML with its name, the note type, tags,
 * deck - whether or not it became a word pair: what the app cannot show yet (more fields,
 * media, cloze) is here for when it can. The guid is Anki's own id for the note, so importing
 * the same deck again updates it instead of adding it twice.
 */
@Entity(
    tableName = TableNames.ANKI_NOTE_TABLE,
    indices = [Index(value = ["guid"], unique = true), Index("wordPairId")]
)
data class AnkiNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val guid: String,
    val ankiNoteId: Long,
    val noteType: String,
    /** AnkiFormat.KIND_NORMAL or KIND_CLOZE. */
    val noteTypeKind: Int,
    /** JSON array of the note type's field names, in order. */
    val fieldNames: String,
    /** JSON array of the fields as Anki stored them. */
    val fields: String,
    val tags: String,
    /** "Испанский::Глаголы". */
    val deckName: String,
    /** The pair made of it; null when the note could not be one. */
    val wordPairId: Long?,
    val importedAt: Long,
)

/**
 * An imported Anki card as Anki had it - its raw schedule included - and the side it became,
 * if any. Due dates are kept as Anki counts them, with the collection's start to count from.
 */
@Entity(
    tableName = TableNames.ANKI_CARD_TABLE,
    indices = [Index(value = ["ankiCardId"], unique = true), Index("noteGuid")]
)
data class AnkiCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ankiCardId: Long,
    val noteGuid: String,
    val ord: Int,
    val deckName: String,
    val type: Int,
    val queue: Int,
    val due: Long,
    val ivl: Int,
    val factor: Int,
    val reps: Int,
    val lapses: Int,
    val originalDue: Long,
    val originalDeckId: Long,
    val data: String?,
    val collectionCreated: Long,
    val sideId: Long?,
)

/** One answer from Anki's review history, kept as Anki logged it. */
@Entity(
    tableName = TableNames.ANKI_REVLOG_TABLE,
    indices = [Index(value = ["ankiCardId", "reviewedAt"], unique = true)]
)
data class AnkiRevlogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ankiCardId: Long,
    /** Anki's revlog id: when, in milliseconds. */
    val reviewedAt: Long,
    val ease: Int,
    val ivl: Int,
    val lastIvl: Int,
    val factor: Int,
    val timeMs: Int,
    val type: Int,
)
