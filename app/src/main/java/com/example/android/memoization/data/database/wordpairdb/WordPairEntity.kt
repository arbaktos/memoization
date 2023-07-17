package com.example.android.memoization.data.database.wordpairdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.model.WordStatus
import java.util.Date

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