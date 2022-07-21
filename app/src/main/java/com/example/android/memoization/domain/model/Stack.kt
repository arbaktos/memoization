package com.example.android.memoization.domain.model

import java.util.*

data class Stack(
    val name: String,
    var numRep: Int = 0, //to schedule check days
    var words: MutableList<WordPair> = mutableListOf(),
    var stackId: Long = 0,
    var hasWords: Boolean = false,
    override var isVisible: Boolean = true,
    var pinned: Boolean = false
//    val language1: Language,
//    val language2: Language,
) : ListItem {

    fun prepareStack(): Stack {
        val currentDate = Date()
        return this.apply {
            words.forEach { wordPair ->
                wordPair.checkIfShow(currentDate = currentDate)
            }
        }
    }
}
