package com.example.android.memoization.data.model

interface BaseWordPair {
    val parentStackId: Long
    var word1: String
    var word2: String?
    val wordPairId: Long
    var isVisible: Boolean
    /**
     * When the learner deleted the pair; null otherwise. A pair can be hidden without being
     * deleted: a copy folded into another by a duplicates merge is hidden, never shown as deleted.
     */
    val deletedAt: Long?
}
