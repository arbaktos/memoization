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
    /** Sessions with this stack ended with "Enough for today"; see domain.session.Stamina. */
    val tiredSessions: Int
    /** Answers given in those sessions, summed. */
    val tiredAnswersSum: Int
}