package com.example.android.memoization.data.model

import android.util.Log
import java.util.*

data class MemoStack(
    override val name: String,
    override var numRep: Int = 0, //to schedule check days
    override var hasWords: Boolean = false,
    override var stackId: Long = 0,
    override var isVisible: Boolean = true,
    override var pinned: Boolean = false,
    override val fromLanguage: String? = null,
    override val toLanguage: String? = null,
) : BaseStack, DismissableItem {

    var words: MutableList<BaseWordPair> = mutableListOf()
        set(value) {
            hasWords = value.isNotEmpty()
            field = value
        }

    fun prepareStack(): MemoStack {
        val currentDate = Date()
        return this.apply {
            words.forEach {it as WordPair
                it.checkIfShow(currentDate = currentDate)
            }
        }
    }

    fun hasWordsToLearn(): Boolean {
        return this.words.any { (it as WordPair).toLearn }
    }

    fun getStackUnrepeatedPercent(): Int {
        val all = this.words.size
        val unlearned = this.prepareStack().words.filter { (it as WordPair).toLearn }.size
        val result = ((unlearned.toFloat() / all.toFloat())  * 100).toInt()
        Log.d(TAG, "getStackUnrepeatedPercent: $result")
        return result
    }

    companion object {
        private val TAG = "MemoStack"
    }
}
