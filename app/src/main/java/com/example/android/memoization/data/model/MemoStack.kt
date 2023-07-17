package com.example.android.memoization.data.model

import ru.vasilisasycheva.translation.api.LanguageItem
import java.util.*

data class MemoStack(
    override val name: String,
    override var numRep: Int = 0, //to schedule check days
    var words: MutableList<WordPair> = mutableListOf(),
    override var hasWords: Boolean = false,
    override var stackId: Long = 0,
    override var isVisible: Boolean = true,
    override var pinned: Boolean = false,
    override val fromLanguage: String? = null,
    override val toLanguage: String? = null,
) : BaseStack, DismissableItem {

    fun prepareStack(): MemoStack {
        val currentDate = Date()
        return this.apply {
            words.forEach { wordPair ->
                wordPair.checkIfShow(currentDate = currentDate)
            }
        }
    }
}
