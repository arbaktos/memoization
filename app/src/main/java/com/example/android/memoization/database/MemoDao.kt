package com.example.android.memoization.database

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

    @Transaction
    @Query("SELECT * FROM stack_entity_table")
    suspend fun getStacksWithWords(): List<StackWithWords>

    @Update
    suspend fun updateStack(stackEntity: StackEntity)

    @Delete
    suspend fun deleteStackFomDb(stackEntity: StackEntity)

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