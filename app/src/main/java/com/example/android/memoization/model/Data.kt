package com.example.android.memoization.model

import androidx.compose.runtime.mutableStateListOf
import com.example.android.memoization.database.FolderEntity
import com.example.android.memoization.database.WordPairEntity
import java.util.*

data class Folder(
    val name: String,
    var isOpen: Boolean = false,
    val stacks: MutableList<Stack> = mutableListOf(),
    var folderId: Long = 0
//    val language1: Language,
//    val language2: Language,
) {

    fun toFolderEntity(): FolderEntity {
        return FolderEntity(name = this.name, isOpen = this.isOpen, folderId = this.folderId)
    }
}

data class Stack(
    val name: String,
    var numRep: Int = 0, //to schedule check days
    var words: MutableList<WordPair> = mutableListOf(),
    var stackId: Long = 0,
    var hasWords: Boolean = false,
    override var isVisible: Boolean = true
//    val language1: Language,
//    val language2: Language,
) : ListItem


data class WordPair (
    val stackId: Long,
    var word1: String,
    var word2: String?,
    var lastRep: Date = Date(),
    var toShow: Boolean = false,
    var wordPairId: Long = 0,
    var levelOfKnowledge: WordStatus = WordStatus.Level1(),
    override var isVisible: Boolean = true
//    val language1: Language,
//    val language2: Language,
): ListItem {
    // to get the period of the card reps
    fun harderLevel() {
        this.levelOfKnowledge = when (this.levelOfKnowledge) {
            is WordStatus.Level1 -> WordStatus.Level2()
            is WordStatus.Level2 -> WordStatus.Level3()
            is WordStatus.Level3 -> WordStatus.Level4()
            is WordStatus.Level4 -> WordStatus.Learned()
            is WordStatus.Learned -> WordStatus.Learned()
        }
    }

    fun easierLevel() {
        this.levelOfKnowledge = when (this.levelOfKnowledge) {
            is WordStatus.Level1 -> WordStatus.Level1()
            is WordStatus.Level2 -> WordStatus.Level1()
            is WordStatus.Level3 -> WordStatus.Level2()
            is WordStatus.Level4 -> WordStatus.Level3()
            is WordStatus.Learned -> WordStatus.Level4()
        }
    }

    fun toLevel1() {
        this.levelOfKnowledge = WordStatus.Level1()
    }

    fun toWordPairEntity(): WordPairEntity {
        return WordPairEntity(
            this.stackId ?: -1,
            this.word1 ?: "",
            this.word2 ?: "",
            this.lastRep,
            this.toShow,
            this.levelOfKnowledge,
            this.wordPairId,
            this.isVisible
        )
    }


//    override fun toString(): String {
//        return "$word1 $word2"
//    }
}

