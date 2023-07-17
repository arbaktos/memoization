package com.example.android.memoization.data.database

import androidx.room.*
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

    @Transaction
    @Query("SELECT * FROM stack_entity_table WHERE stackId = :stackId")
    fun getStackWithWordsById(stackId: Long): Flow<StackWithWords>

    @Update
    suspend fun updateStack(stackEntity: StackEntity)

    @Query("DELETE FROM stack_entity_table WHERE stackId = :stackId")
    suspend fun deleteStackFomDb(stackId: Long)

    //words
    @Insert
    suspend fun insertWordPair(wordPairEntity: WordPairEntity)

    @Query("SELECT * FROM wordpairentity WHERE parentStackId LIKE :stackId")
    suspend fun getWordsFromStack(stackId: Long): List<WordPairEntity>

    @Query("SELECT * FROM wordpairentity WHERE wordPairId LIKE :id" )
    suspend fun findWordPairById(id: Long): WordPairEntity

    @Update
    suspend fun updateWordPair(wordPairEntity: WordPairEntity)

    @Delete
    suspend fun deleteWordPairFromDb(wordPairEntity: WordPairEntity)

    @Query("SELECT * FROM wordpairentity WHERE wordPairId LIKE :wpId")
    fun getWordPairByIdFlow(wpId: Long): Flow<WordPairEntity>
}