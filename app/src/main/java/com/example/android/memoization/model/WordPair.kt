package com.example.android.memoization.model

import com.example.android.memoization.database.WordPairEntity
import com.example.android.memoization.utils.longToDays
import java.util.*

data class WordPair(
    val stackId: Long,
    var word1: String,
    var word2: String?,
    var lastRep: Date = Date(),
    var wordPairId: Long = 0,
    var levelOfKnowledge: WordStatus = WordStatus.Level1(),
    override var isVisible: Boolean = true
//    val language1: Language,
//    val language2: Language,
) : ListItem {
    var toLearn: Boolean = false
        get() = checkIfShow()

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
            this.toLearn,
            this.levelOfKnowledge,
            this.wordPairId,
            this.isVisible
        )
    }

    fun checkIfShow(currentDate: Date = Date()): Boolean {
        val timeWithoutRepetion = currentDate.time - this.lastRep.time
        val daysWithoutRepetition = longToDays(timeWithoutRepetion)
        return daysWithoutRepetition > this.levelOfKnowledge.frequency
    }

}