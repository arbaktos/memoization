package com.example.android.memoization.data.model

import ru.vasilisasycheva.translation.api.LanguageItem


interface BaseStack {
    val name: String
    var numRep: Int //to schedule check days
    val stackId: Long
    var hasWords: Boolean
    var isVisible: Boolean
    var pinned: Boolean
    val fromLanguage: String?
    val toLanguage:String?
}