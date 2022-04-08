package com.example.android.memoization.model

data class Stack(
    val name: String,
    var numRep: Int = 0, //to schedule check days
    var words: MutableList<WordPair> = mutableListOf(),
    var stackId: Long = 0,
    var hasWords: Boolean = false,
    override var isVisible: Boolean = true
//    val language1: Language,
//    val language2: Language,
) : ListItem