package com.example.android.memoization.data.model

import java.util.Date

interface BaseWordPair {
    val parentStackId: Long
    var word1: String
    var word2: String?
    var lastRep: Date
    var toShow: Boolean
    var level: WordStatus
    val wordPairId: Long
    var isVisible: Boolean
}