package com.example.android.memoization.data.database

import androidx.room.*

@Dao
interface MemoDao {

    //folders
    @Insert(entity = FolderEntity::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folderEntity: FolderEntity): Long

    @Query("SELECT * FROM folder_entity_table")
    suspend fun getFolders():List<FolderEntity>

    @Transaction
    @Query("SELECT * FROM folder_entity_table WHERE folderId LIKE :folderId")
    suspend fun getSingleFolderWithStacks(folderId: Long): FolderwithStacks

    @Transaction
    @Query("SELECT * FROM folder_entity_table")
    suspend fun getFoldersWithStacks(): List<FolderwithStacks>

    @Delete
    suspend fun deleteFolderFromDb(folderEntity: FolderEntity)

    //stacks
    @Insert(entity = StackEntity::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStack(stackEntity: StackEntity): Long

    @Query("SELECT * FROM STACK_ENTITY_TABLE WHERE stackId= :stackId")
    suspend fun getStackById(stackId: Long): StackEntity

    @Transaction
    @Query("SELECT * FROM stack_entity_table")
    suspend fun getStacksWithWords(): List<StackWithWords>

    @Transaction
    @Query("SELECT * FROM stack_entity_table WHERE stackId = :stackId")
    suspend fun getStackWithWordsById(stackId: Long): StackWithWords

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
}