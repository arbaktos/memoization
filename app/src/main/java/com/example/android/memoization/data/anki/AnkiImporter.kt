package com.example.android.memoization.data.anki

import android.net.Uri
import androidx.room.withTransaction
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.MemoDatabase
import com.example.android.memoization.data.database.ankidb.AnkiCardEntity
import com.example.android.memoization.data.database.ankidb.AnkiDao
import com.example.android.memoization.data.database.ankidb.AnkiNoteEntity
import com.example.android.memoization.data.database.ankidb.AnkiRevlogEntity
import com.example.android.memoization.data.database.sidedb.SideEntity
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.Side
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** What an import did, for the learner's one-line report. */
data class AnkiImportResult(
    /** New word pairs. */
    val added: Int,
    /** Pairs already imported before, their words refreshed; their progress stays ours. */
    val updated: Int,
    /** Notes kept whole that are not a word and its meaning: cloze, media only, one field. */
    val keptForLater: Int,
    /** The stacks the new pairs went into, new or from the deck's last import. */
    val stackIds: List<Long>,
)

/**
 * Brings an Anki package in: a stack per deck, a word pair per note that is a word and its
 * meaning (first field, second field), each side carrying on from the Anki card it was, and
 * every note, card and review kept whole beside them. All in one transaction - half a deck is
 * worse than none.
 */
class AnkiImporter @Inject constructor(
    private val reader: AnkiPackageReader,
    private val database: MemoDatabase,
    private val memoDao: MemoDao,
    private val ankiDao: AnkiDao,
) {

    /**
     * [practiceSides] goes to the stacks this import makes (null: they follow the library).
     * With [resetProgress] the pairs it adds start New, as if never studied; Anki's progress is
     * still kept with the imported cards. Pairs from an earlier import keep what they have.
     */
    suspend fun import(
        uri: Uri,
        practiceSides: String? = null,
        resetProgress: Boolean = false,
        now: Long = System.currentTimeMillis(),
    ): AnkiImportResult = import(reader.read(uri), practiceSides, resetProgress, now)

    suspend fun import(
        collection: AnkiCollection,
        practiceSides: String? = null,
        resetProgress: Boolean = false,
        now: Long = System.currentTimeMillis(),
    ): AnkiImportResult {
        val cardsByNote = collection.cards.groupBy { it.noteId }
        val lastReview = collection.reviews.groupBy { it.cardId }.mapValues { (_, r) -> r.maxOf { it.id } }
        val stringList = ListSerializer(String.serializer())

        return database.withTransaction {
            val known = collection.notes.map { it.guid }.chunked(QUERY_CHUNK)
                .flatMap { ankiDao.notesByGuid(it) }.associateBy { it.guid }
            val knownCards = collection.cards.map { it.id }.chunked(QUERY_CHUNK)
                .flatMap { ankiDao.cardsById(it) }.associateBy { it.ankiCardId }
            val stackOfDeck = mutableMapOf<String, Long>()
            var added = 0
            var updated = 0
            var kept = 0
            val noteRows = mutableListOf<AnkiNoteEntity>()
            val cardRows = mutableListOf<AnkiCardEntity>()

            for (note in collection.notes) {
                val type = collection.noteTypes[note.noteTypeId]
                val cards = cardsByNote[note.id].orEmpty().sortedBy { it.ord }
                val deckName = cards.firstOrNull()?.let { collection.decks[it.homeDeckId] } ?: DEFAULT_DECK
                val pair = pairOf(note, type)
                val before = known[note.guid]
                var pairId = before?.wordPairId
                val sideOfCard = mutableMapOf<Long, Long>()

                when {
                    pairId != null -> {
                        if (pair != null) memoDao.setWordPairWords(pairId, pair.first, pair.second)
                        updated++
                    }
                    pair != null -> {
                        val stackId = stackOfDeck.getOrPut(deckName) {
                            ankiDao.stackOfDeck(deckName) ?: memoDao.insertStack(
                                StackEntity(
                                    name = AnkiFormat.stackName(deckName),
                                    hasWords = true,
                                    practiceSides = practiceSides,
                                )
                            )
                        }
                        val id = memoDao.insertWordPair(
                            WordPairEntity(parentStackId = stackId, word1 = pair.first, word2 = pair.second)
                        )
                        pairId = id
                        for (shown in Shown.entries) {
                            val card = cards.firstOrNull { AnkiSchedule.shownOf(it.ord) == shown }
                            val side = card?.takeUnless { resetProgress }?.let {
                                AnkiSchedule.side(it, id, shown, collection.createdSeconds, lastReview[it.id], now)
                            } ?: Side(wordPairId = id, shown = shown)
                            val sideId = memoDao.insertSide(SideEntity.from(side))
                            if (card != null) sideOfCard[card.id] = sideId
                        }
                        added++
                    }
                    else -> kept++
                }

                noteRows += AnkiNoteEntity(
                    id = before?.id ?: 0,
                    guid = note.guid,
                    ankiNoteId = note.id,
                    noteType = type?.name.orEmpty(),
                    noteTypeKind = type?.kind ?: AnkiFormat.KIND_NORMAL,
                    fieldNames = Json.encodeToString(stringList, type?.fieldNames.orEmpty()),
                    fields = Json.encodeToString(stringList, note.fields),
                    tags = note.tags,
                    deckName = deckName,
                    wordPairId = pairId,
                    importedAt = now,
                )
                cards.mapTo(cardRows) { card ->
                    val previous = knownCards[card.id]
                    AnkiCardEntity(
                        id = previous?.id ?: 0,
                        ankiCardId = card.id,
                        noteGuid = note.guid,
                        ord = card.ord,
                        deckName = collection.decks[card.homeDeckId] ?: deckName,
                        type = card.type, queue = card.queue, due = card.due, ivl = card.ivl,
                        factor = card.factor, reps = card.reps, lapses = card.lapses,
                        originalDue = card.originalDue, originalDeckId = card.originalDeckId,
                        data = card.data,
                        collectionCreated = collection.createdSeconds,
                        sideId = sideOfCard[card.id] ?: previous?.sideId,
                    )
                }
            }

            noteRows.chunked(WRITE_CHUNK).forEach { ankiDao.upsertNotes(it) }
            cardRows.chunked(WRITE_CHUNK).forEach { ankiDao.upsertCards(it) }
            collection.reviews.map {
                AnkiRevlogEntity(
                    ankiCardId = it.cardId, reviewedAt = it.id, ease = it.ease, ivl = it.ivl,
                    lastIvl = it.lastIvl, factor = it.factor, timeMs = it.timeMs, type = it.type,
                )
            }.chunked(WRITE_CHUNK).forEach { ankiDao.insertReviews(it) }

            AnkiImportResult(added, updated, kept, stackOfDeck.values.distinct())
        }
    }

    /**
     * The word and its meaning, if the note is one: a normal note type, the first two fields
     * with text left once formatting and media are out, and no cloze gaps in the first.
     */
    private fun pairOf(note: AnkiNote, type: AnkiNoteType?): Pair<String, String>? {
        if (type?.kind == AnkiFormat.KIND_CLOZE || note.fields.size < 2) return null
        if (AnkiText.hasCloze(note.fields[0])) return null
        val word = AnkiText.toPlain(note.fields[0])
        val meaning = AnkiText.toPlain(note.fields[1])
        return if (word.isBlank() || meaning.isBlank()) null else word to meaning
    }

    private companion object {
        /** Under SQLite's 999 bound variables. */
        const val QUERY_CHUNK = 500
        const val WRITE_CHUNK = 500
        const val DEFAULT_DECK = "Default"
    }
}
