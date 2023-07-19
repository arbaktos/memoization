package com.example.android.memoization.data.model

import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.utils.longToDays
import java.util.*

data class WordPair(
    override val parentStackId: Long,
    override var word1: String,
    override var word2: String?,
    override var lastRep: Date = Date(),
    override var wordPairId: Long = 0,
    override var isVisible: Boolean = true,
    override var toShow: Boolean = false,
    override var level: WordStatus = WordStatus.Level1()
) : BaseWordPair, DismissableItem {
    var toLearn: Boolean = false
        get() = checkIfShow()

    var levelOfKnowledge: WordStatus = WordStatus.Level1()
        private set

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
            this.parentStackId ?: -1,
            this.word1,
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