package com.example.android.memoization.data.database

import androidx.room.*
import com.example.android.memoization.data.database.logdb.ReviewLogEntity
import com.example.android.memoization.data.database.logdb.SessionEntity
import com.example.android.memoization.data.database.logdb.StackLastSession
import com.example.android.memoization.data.database.sidedb.SideEntity
import com.example.android.memoization.data.database.stackdb.StackEntity
import com.example.android.memoization.data.database.stackdb.StackWithWords
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    //stacks
    @Insert(entity = StackEntity::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStack(stackEntity: StackEntity): Long

    @Query("SELECT * FROM STACK_ENTITY_TABLE WHERE stackId= :stackId")
    suspend fun getStackById(stackId: Long): StackEntity

    @Transaction
    @Query("SELECT * FROM stack_entity_table")
    fun getStacksWithWords(): Flow<List<StackWithWords>>

    @Query("SELECT * FROM stack_entity_table")
    fun getStacks(): Flow<List<StackEntity>>

    @Transaction
    @Query("SELECT * FROM stack_entity_table WHERE stackId = :stackId")
    fun getStackWithWordsById(stackId: Long): Flow<StackWithWords>

    @Update
    suspend fun updateStack(stackEntity: StackEntity)

    //words
    @Insert
    suspend fun insertWordPair(wordPairEntity: WordPairEntity): Long

    /** A pair is never without its sides, so the two inserts go together. */
    @Transaction
    suspend fun insertWordPairWithSides(wordPairEntity: WordPairEntity): Long {
        val id = insertWordPair(wordPairEntity)
        insertSides(SideEntity.newSidesFor(id))
        return id
    }

    @Query("SELECT * FROM wordpair_entity_table WHERE parentStackId LIKE :stackId")
    suspend fun getWordsFromStack(stackId: Long): List<WordPairEntity>

    @Query("SELECT * FROM wordpair_entity_table WHERE wordPairId LIKE :id" )
    suspend fun findWordPairById(id: Long): WordPairEntity

    @Update
    suspend fun updateWordPair(wordPairEntity: WordPairEntity)

    /** Deleting a pair only hides it; nothing in the learning database is ever thrown away. */
    @Query("UPDATE wordpair_entity_table SET isVisible = :visible WHERE wordPairId = :wordPairId")
    suspend fun setWordPairVisible(wordPairId: Long, visible: Boolean)

    @Query("SELECT * FROM wordpair_entity_table WHERE wordPairId LIKE :wpId")
    fun getWordPairByIdFlow(wpId: Long): Flow<WordPairEntity>

    //sides
    @Insert
    suspend fun insertSides(sides: List<SideEntity>)

    @Update
    suspend fun updateSide(side: SideEntity)

    @Query("SELECT * FROM side_entity_table WHERE wordPairId = :wordPairId ORDER BY shown")
    suspend fun getSidesForPair(wordPairId: Long): List<SideEntity>

    //review log
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("UPDATE session_table SET finishedAt = :finishedAt WHERE sessionId = :sessionId")
    suspend fun finishSession(sessionId: Long, finishedAt: Long)

    @Insert
    suspend fun insertReview(review: ReviewLogEntity): Long

    @Query("SELECT * FROM session_table WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM review_log_table WHERE sessionId = :sessionId ORDER BY ratedAt")
    suspend fun getReviewsOfSession(sessionId: Long): List<ReviewLogEntity>

    @Query("SELECT stackId, MAX(startedAt) AS lastStartedAt FROM session_table GROUP BY stackId")
    suspend fun getLastSessionPerStack(): List<StackLastSession>
}
