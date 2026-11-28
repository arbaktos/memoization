package com.example.android.memoization.data.anki

/** What an Anki package holds, read as is: nothing here is ours yet. */
data class AnkiCollection(
    /** When the collection was created, in seconds: review due dates count days from it. */
    val createdSeconds: Long,
    val decks: Map<Long, String>,
    val noteTypes: Map<Long, AnkiNoteType>,
    val notes: List<AnkiNote>,
    val cards: List<AnkiCard>,
    val reviews: List<AnkiReview>,
    /** Media files copied into the app, by their names in the collection. */
    val mediaFiles: Int = 0,
)

data class AnkiNoteType(
    val id: Long,
    val name: String,
    val kind: Int,
    val fieldNames: List<String>,
)

data class AnkiNote(
    val id: Long,
    val guid: String,
    val noteTypeId: Long,
    /** Fields as stored: HTML, in the note type's order. */
    val fields: List<String>,
    val tags: String,
)

/** A card is one thing Anki asks of a note; its ord says which of the note type's templates. */
data class AnkiCard(
    val id: Long,
    val noteId: Long,
    val deckId: Long,
    val ord: Int,
    /** 0 new, 1 learning, 2 review, 3 relearning. */
    val type: Int,
    /** 0 new, 1 learning today, 2 review, 3 learning across days; below 0 suspended or buried. */
    val queue: Int,
    val due: Long,
    /** Days, the interval the card is on. */
    val ivl: Int,
    /** The SM-2 ease, per mille: 2500 is 250 %. */
    val factor: Int,
    val reps: Int,
    val lapses: Int,
    /** In a filtered deck: where it came from and its due date there. */
    val originalDue: Long,
    val originalDeckId: Long,
    /** JSON; the FSRS memory, when Anki had FSRS on. */
    val data: String?,
) {
    /** The deck the card belongs to: a filtered deck only borrows it. */
    val homeDeckId: Long get() = if (originalDeckId != 0L) originalDeckId else deckId
    val homeDue: Long get() = if (originalDeckId != 0L) originalDue else due
}

data class AnkiReview(
    /** Also when it happened, in milliseconds. */
    val id: Long,
    val cardId: Long,
    /** The button: 1 again, 2 hard, 3 good, 4 easy. */
    val ease: Int,
    val ivl: Int,
    val lastIvl: Int,
    val factor: Int,
    val timeMs: Int,
    val type: Int,
)
