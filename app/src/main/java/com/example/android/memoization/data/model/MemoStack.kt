package com.example.android.memoization.data.model

import java.util.TimeZone

data class MemoStack(
    override val name: String,
    override var numRep: Int = 0, //to schedule check days
    override var hasWords: Boolean = false,
    override var stackId: Long = 0,
    override var isVisible: Boolean = true,
    override val fromLanguage: String? = null,
    override val toLanguage: String? = null,
    override var pinnedTime: Long? = null,
) : BaseStack, DismissableItem {

    var words: MutableList<WordPair> = mutableListOf()
        set(value) {
            hasWords = value.isNotEmpty()
            field = value
        }

    /** Pairs with at least one practised side to review; counted as pairs, not sides. */
    fun duePairs(
        practice: PracticeSides,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault(),
    ): List<WordPair> = words.filter { it.isDue(practice, now, zone) }

    fun hasDue(
        practice: PracticeSides,
        now: Long = System.currentTimeMillis(),
        zone: TimeZone = TimeZone.getDefault(),
    ): Boolean = words.any { it.isDue(practice, now, zone) }

    val needsTranslation = this.fromLanguage != null && this.toLanguage != null
}
