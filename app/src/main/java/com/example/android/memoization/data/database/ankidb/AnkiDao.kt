package com.example.android.memoization.data.database.ankidb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AnkiDao {

    @Query("SELECT * FROM anki_note_table WHERE guid IN (:guids)")
    suspend fun notesByGuid(guids: List<String>): List<AnkiNoteEntity>

    @Upsert
    suspend fun upsertNotes(notes: List<AnkiNoteEntity>)

    @Query("SELECT * FROM anki_card_table WHERE ankiCardId IN (:ids)")
    suspend fun cardsById(ids: List<Long>): List<AnkiCardEntity>

    @Upsert
    suspend fun upsertCards(cards: List<AnkiCardEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviews(reviews: List<AnkiRevlogEntity>)

    /** The stack this deck's words went to last time, unless it has been deleted since. */
    @Query(
        """
        SELECT w.parentStackId FROM anki_note_table n
        JOIN wordpair_entity_table w ON w.wordPairId = n.wordPairId
        JOIN stack_entity_table s ON s.stackId = w.parentStackId
        WHERE n.deckName = :deckName AND s.deletedAt IS NULL
        LIMIT 1
        """
    )
    suspend fun stackOfDeck(deckName: String): Long?
}
