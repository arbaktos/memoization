package com.example.android.memoization.data.model

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

    /** The pairs a session should hold right now: due ones and New ones. */
    fun dueWords(now: Long = System.currentTimeMillis()): List<WordPair> =
        words.filter { it.isDue(now) }

    fun hasDueWords(now: Long = System.currentTimeMillis()): Boolean =
        words.any { it.isDue(now) }

    val needsTranslation = this.fromLanguage != null && this.toLanguage != null
}
