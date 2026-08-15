package com.example.android.memoization.data.model

interface BaseWordPair {
    val parentStackId: Long
    var word1: String
    var word2: String?
    val wordPairId: Long
    var isVisible: Boolean
}
