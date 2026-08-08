package com.example.android.memoization.data.database.wordpairdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames
import com.example.android.memoization.data.model.BaseWordPair
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.model.WordStatus
import java.util.Date

@Entity(tableName = TableNames.WORD_PAIR_TABLE)
data class WordPairEntity(
    override val parentStackId: Long,
    override var word1: String,
    override var word2: String?,
    override var lastRep: Date = Date(),
    override var toShow: Boolean = false,
    override var level: WordStatus = WordStatus.Level1(),
    @PrimaryKey(autoGenerate = true)
    override val wordPairId: Long = 0,
    override var isVisible: Boolean = true
) : BaseWordPair {
    fun toWordPair(): WordPair {
        return WordPair(
            parentStackId = this.parentStackId,
            word1 = this.word1,
            word2 = this.word2,
            lastRep = this.lastRep,
            toShow = this.toShow,
            level = this.level,
            wordPairId = this.wordPairId,
            isVisible = this.isVisible
        )
    }

    companion object {
        fun create(wordPair: BaseWordPair): WordPairEntity {
            return WordPairEntity(
                parentStackId = wordPair.parentStackId,
                word1 = wordPair.word1,
                word2 = wordPair.word2,
                lastRep = wordPair.lastRep,
                toShow = wordPair.toShow,
                level = wordPair.level,
                wordPairId = wordPair.wordPairId,
                isVisible = wordPair.isVisible
            )
        }
    }
}