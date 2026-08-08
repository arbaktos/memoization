package com.example.android.memoization.data.model


interface BaseStack {
    val name: String
    var numRep: Int //to schedule check days
    val stackId: Long
    var hasWords: Boolean
    var isVisible: Boolean
    var pinnedTime: Long?
    val fromLanguage: String?
    val toLanguage:String?
}