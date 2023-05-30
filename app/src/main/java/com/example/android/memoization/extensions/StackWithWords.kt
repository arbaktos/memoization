package com.example.android.memoization.extensions

import com.example.android.memoization.data.database.StackWithWords
import com.example.android.memoization.domain.model.Stack

fun StackWithWords.toStack(): Stack {
    return Stack(
        name = this.stack.name,
        numRep = this.stack.numRep,
        stackId = this.stack.stackId,
        words = this.words.map { it.toWordPair() }.toMutableList(),
        hasWords = this.stack.hasWords
    )
}