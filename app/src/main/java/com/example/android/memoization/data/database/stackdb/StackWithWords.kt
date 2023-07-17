package com.example.android.memoization.data.database.stackdb

import androidx.room.Embedded
import androidx.room.Relation
import com.example.android.memoization.data.database.WordPairEntity
import com.example.android.memoization.data.model.MemoStack

data class StackWithWords(
    @Embedded val stack: StackEntity,
    @Relation(
        parentColumn = "stackId",
        entityColumn = "parentStackId"
    )
    val words: List<WordPairEntity>
)

fun StackWithWords.toStack(): MemoStack {
    return MemoStack(
        name = this.stack.name,
        numRep = this.stack.numRep,
        stackId = this.stack.stackId,
        words = this.words.map { it.toWordPair() }.toMutableList(),
        hasWords = this.words.isNotEmpty(),
        isVisible = this.stack.isVisible,
        pinned = this.stack.pinned
    )
}