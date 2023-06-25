package com.example.android.memoization.data.model

import org.intellij.lang.annotations.Language
import ru.vasilisasycheva.translation.api.LanguageItem
import java.util.*

data class MemoStack(
    val name: String,
    var numRep: Int = 0, //to schedule check days
    var words: MutableList<WordPair> = mutableListOf(),
    var hasWords: Boolean = false,
    var stackId: Long = 0,
    override var isVisible: Boolean = true,
    var pinned: Boolean = false,
    val fromLanguage: LanguageItem? = null,
    val toLanguage: LanguageItem? = null,
) : DismissableItem {

    fun prepareStack(): MemoStack {
        val currentDate = Date()
        return this.apply {
            words.forEach { wordPair ->
                wordPair.checkIfShow(currentDate = currentDate)
            }
        }
    }
}
