package com.example.android.memoization.data.database.stackdb

import androidx.room.Embedded
import androidx.room.Relation
import com.example.android.memoization.data.database.wordpairdb.WordPairEntity
import com.example.android.memoization.data.model.MemoStack

data class StackWithWords(
    @Embedded val stack: StackEntity,
    @Relation(
        parentColumn = "stackId",
        entityColumn = "parentStackId"
    )
    val words: List<WordPairEntity>
)

fun StackWithWords.toMemoStack(): MemoStack {
    return MemoStack(
        name = this.stack.name,
        numRep = this.stack.numRep,
        stackId = this.stack.stackId,
        hasWords = this.words.isNotEmpty(),
        isVisible = this.stack.isVisible,
        pinned = this.stack.pinned
    ).apply {
        words = this@toMemoStack.words.map { it.toWordPair() }.toMutableList()
    }
}