package com.example.android.memoization.data.database.stackdb

import androidx.room.Embedded
import androidx.room.Relation
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.database.wordpairdb.WordPairWithSides
import com.example.android.memoization.data.model.MemoStack

data class StackWithWords(
    @Embedded val stack: StackEntity,
    @Relation(
        entity = WordPairEntity::class,
        parentColumn = "stackId",
        entityColumn = "parentStackId"
    )
    val words: List<WordPairWithSides>
)

fun StackWithWords.toMemoStack(): MemoStack {
    // Hidden pairs are filtered here, once: nothing downstream - session, counts, the Learn
    // button, the reminders - should see a pair the learner has deleted.
    val visibleWords = this.words.filter { it.pair.isVisible }
    return MemoStack(
        name = this.stack.name,
        numRep = this.stack.numRep,
        stackId = this.stack.stackId,
        hasWords = visibleWords.isNotEmpty(),
        isVisible = this.stack.isVisible,
        pinnedTime = this.stack.pinnedTime,
        fromLanguage = this.stack.fromLanguage,
        toLanguage = this.stack.toLanguage,
        tiredSessions = this.stack.tiredSessions,
        tiredAnswersSum = this.stack.tiredAnswersSum,
    ).apply {
        words = visibleWords.map { it.toWordPair() }.toMutableList()
    }
}
