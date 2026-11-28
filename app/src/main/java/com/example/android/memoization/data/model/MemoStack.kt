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
    override val tiredSessions: Int = 0,
    override val tiredAnswersSum: Int = 0,
    override val deletedAt: Long? = null,
    override val practiceSides: String? = null,
) : BaseStack, DismissableItem {

    /** The stack's own choice, if it has made one; a name no longer known counts as none. */
    val ownPractice: PracticeSides? get() = PracticeSides.entries.firstOrNull { it.name == practiceSides }

    /** The sides this stack practises: its own choice, else the library's [library]. */
    fun practice(library: PracticeSides): PracticeSides = ownPractice ?: library


    /** The pairs the learner deleted, the latest first: the stack screen's Deleted section. */
    var deletedWords: List<WordPair> = emptyList()

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
