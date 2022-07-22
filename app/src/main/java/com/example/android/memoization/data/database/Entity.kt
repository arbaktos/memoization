package com.example.android.memoization.data.database

import androidx.room.*
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.domain.model.WordStatus
import com.example.android.memoization.utils.FOLDER_TABLE
import com.example.android.memoization.utils.STACK_TABLE
import java.util.*

@Entity(tableName = FOLDER_TABLE)
data class FolderEntity(
    val name: String,
    var isOpen: Boolean,
    @PrimaryKey(autoGenerate = true)
    val folderId: Long = 0
)

@Entity(tableName = STACK_TABLE)
data class StackEntity(
    val name: String,
    var numRep: Int = 0, //to schedule check days
    @PrimaryKey(autoGenerate = true)
    val stackId: Long = 0,
    val parentFolderId: Long = 0,
    var hasWords: Boolean = false,
    var isVisible: Boolean = true,
    var pinned: Boolean = false
)

data class FolderwithStacks(
    @Embedded val folderEntity: FolderEntity,
    @Relation(
        parentColumn = "folderId",
        entityColumn = "parentFolderId"
    )
    val stacks: List<StackEntity>
)


data class StackWithWords(
    @Embedded val stack: StackEntity,
    @Relation(
        parentColumn = "stackId",
        entityColumn = "parentStackId"
    )
    val words: List<WordPairEntity>
)

@Entity
data class WordPairEntity(
    val parentStackId: Long,
    var word1: String,
    var word2: String?,
    var lastRep: Date = Date(),
    var toShow: Boolean = false,
    var level: WordStatus = WordStatus.Level1(),
    @PrimaryKey(autoGenerate = true)
    val wordPairId: Long = 0,
    var isVisible: Boolean = true
) {
    fun toWordPair(): WordPair {
        return WordPair(
            this.parentStackId,
            this.word1,
            this.word2,
            this.lastRep,
            this.wordPairId,
            this.level,
            this.isVisible
        )
    }
}

