package com.example.android.memoization.domain.duplicates

import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.WordPair
import java.text.Normalizer

/**
 * Two pairs of a stack the learner said are not the same word: a merge they cancelled. Kept as
 * ids, not words, so a pair added later that looks the same is still offered.
 */
data class DismissedMatch(val lowId: Long, val highId: Long) {
    companion object {
        fun of(a: Long, b: Long) = DismissedMatch(minOf(a, b), maxOf(a, b))
    }
}

/** What the merge dialog offers for one side of the merged pair. */
sealed interface SideChoice {
    /** Every pair already says the same; nothing to choose. */
    data class Fixed(val text: String) : SideChoice

    /** The same word spelled differently - marks, look-alike letters; the learner picks one. */
    data class Spelling(val options: List<String>) : SideChoice

    /** The pairs say different things; a comma-joined draft the learner can edit. */
    data class Edit(val draft: String) : SideChoice
}

/**
 * Pairs that are the same word in all but case and spacing. The oldest stays, the rest are
 * hidden without asking anything but "remove?".
 */
data class CopyGroup(val keep: WordPair, val copies: List<WordPair>) {
    fun plan(): MergePlan = Duplicates.plan(listOf(keep) + copies, keep.word1, keep.word2)
}

/** Pairs sharing a word or a meaning (up to marks and look-alikes), to be merged into one. */
data class MergeProposal(val pairs: List<WordPair>, val word: SideChoice, val meaning: SideChoice) {
    /** Every match inside the proposal: what a Cancel puts on record. */
    fun matches(): Set<DismissedMatch> = buildSet {
        for (i in pairs.indices) for (j in i + 1 until pairs.size) {
            add(DismissedMatch.of(pairs[i].wordPairId, pairs[j].wordPairId))
        }
    }
}

data class DuplicateReport(val copies: List<CopyGroup>, val proposals: List<MergeProposal>) {
    val isEmpty: Boolean get() = copies.isEmpty() && proposals.isEmpty()

    /** All exact copies are one thing to review; each proposal is another. */
    val toReview: Int get() = (if (copies.isEmpty()) 0 else 1) + proposals.size

    companion object {
        val NONE = DuplicateReport(emptyList(), emptyList())
    }
}

/**
 * The database writes that turn several pairs into one: the oldest pair stays with the chosen
 * words, each of its sides takes the weakest schedule among the merged sides of that direction,
 * the others are hidden, and their answers are re-pointed at the kept sides.
 */
data class MergePlan(
    val keepPairId: Long,
    val word1: String,
    val word2: String?,
    val sides: List<Side>,
    val hidePairIds: List<Long>,
    /** Side id of a hidden pair -> side id of the kept pair it is merged into. */
    val moveReviews: Map<Long, Long>,
)

/** Finding the same word added twice to a stack, and what merging such pairs means. ADR 0009. */
object Duplicates {

    fun find(pairs: List<WordPair>, dismissed: Set<DismissedMatch>): DuplicateReport {
        val byAge = pairs.sortedBy { it.wordPairId }

        val copies = byAge
            .groupBy { exactKey(it.word1) + '\u0000' + exactKey(it.word2.orEmpty()) }
            .values
            .filter { it.size > 1 }
            .map { CopyGroup(keep = it.first(), copies = it.drop(1)) }
        val copyIds = copies.flatMap { group -> group.copies.map { it.wordPairId } }.toSet()
        val distinct = byAge.filterNot { it.wordPairId in copyIds }

        val sharedWord = distinct.groupBy { key(it.word1) }.values
        val sharedMeaning = distinct.filter { !it.word2.isNullOrBlank() }.groupBy { key(it.word2!!) }.values

        val groups = (sharedWord + sharedMeaning)
            .filter { it.size > 1 }
            .flatMap { group -> splitByDismissed(group, dismissed) }
            .distinctBy { group -> group.map { it.wordPairId }.toSet() }
        val proposals = groups
            .filterNot { group -> isInsideAnother(group, groups) }
            .sortedBy { group -> group.first().wordPairId }
            .map(::proposal)

        return DuplicateReport(copies, proposals)
    }

    /**
     * A group with no cancelled match inside is proposed whole; once the learner has said two
     * of its pairs differ, each remaining match is offered on its own.
     */
    private fun splitByDismissed(group: List<WordPair>, dismissed: Set<DismissedMatch>): List<List<WordPair>> {
        val pairs = buildList {
            for (i in group.indices) for (j in i + 1 until group.size) add(group[i] to group[j])
        }
        val open = pairs.filterNot { (a, b) -> DismissedMatch.of(a.wordPairId, b.wordPairId) in dismissed }
        return if (open.size == pairs.size) listOf(group) else open.map { (a, b) -> listOf(a, b) }
    }

    private fun isInsideAnother(group: List<WordPair>, all: List<List<WordPair>>): Boolean {
        val ids = group.map { it.wordPairId }.toSet()
        return all.any { other -> other.size > group.size && other.map { it.wordPairId }.containsAll(ids) }
    }

    private fun proposal(pairs: List<WordPair>) = MergeProposal(
        pairs = pairs,
        word = choice(pairs.map { it.word1 }),
        meaning = choice(pairs.map { it.word2.orEmpty() }),
    )

    private fun choice(values: List<String>): SideChoice {
        val texts = values.map { it.trim() }
        if (texts.map(::key).distinct().size == 1) {
            val spellings = texts.distinctBy(::exactKey)
            return if (spellings.size == 1) SideChoice.Fixed(spellings.single()) else SideChoice.Spelling(spellings)
        }
        return SideChoice.Edit(texts.filter { it.isNotEmpty() }.distinctBy(::key).joinToString(", "))
    }

    fun plan(pairs: List<WordPair>, word1: String, word2: String?): MergePlan {
        val byAge = pairs.sortedBy { it.wordPairId }
        val keep = byAge.first()
        val others = byAge.drop(1)

        val sides = Shown.entries.mapNotNull { shown ->
            val kept = keep.sides.firstOrNull { it.shown == shown } ?: return@mapNotNull null
            val weakest = byAge.flatMap { it.sides }.filter { it.shown == shown }.minWith(WEAKER_FIRST)
            weakest.copy(sideId = kept.sideId, wordPairId = keep.wordPairId)
        }
        val moveReviews = others.flatMap { it.sides }.mapNotNull { side ->
            val target = sides.firstOrNull { it.shown == side.shown } ?: return@mapNotNull null
            side.sideId to target.sideId
        }.toMap()

        return MergePlan(
            keepPairId = keep.wordPairId,
            word1 = word1.trim(),
            word2 = word2?.trim()?.ifEmpty { null },
            sides = sides,
            hidePairIds = others.map { it.wordPairId },
            moveReviews = moveReviews,
        )
    }

    /**
     * Adding a word again says it was not remembered, so the merged side gets the weakest
     * schedule: never practised before anything, then the shortest stability. A stable sort
     * keeps the oldest pair's side among equals.
     */
    private val WEAKER_FIRST = compareBy<Side>({ !(it.isNew || it.stability == null) }, { it.stability ?: 0.0 })

    /** Case and spacing only: what makes two pairs plain copies of each other. */
    fun exactKey(text: String): String = text.trim().lowercase().replace(WHITESPACE, " ")

    /**
     * What makes two words the same word: [exactKey], and also the marks on letters (č = c,
     * đ = d) and Cyrillic letters that look Latin (о = o), typed on the wrong keyboard. Russian
     * й keeps its breve - и and й are different letters, not a mark.
     */
    fun key(text: String): String {
        val sb = StringBuilder()
        for (ch in exactKey(text)) {
            when (ch) {
                'й' -> sb.append(ch)
                in STROKED -> sb.append(STROKED.getValue(ch))
                in LOOK_ALIKE -> sb.append(LOOK_ALIKE.getValue(ch))
                else -> Normalizer.normalize(ch.toString(), Normalizer.Form.NFD)
                    .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
                    .forEach { sb.append(LOOK_ALIKE[it] ?: it) }
            }
        }
        return sb.toString()
    }

    private val WHITESPACE = Regex("\\s+")
    private val STROKED = mapOf('đ' to 'd', 'ł' to 'l', 'ø' to 'o')

    /** Lower-case Cyrillic letters drawn like a Latin one. */
    private val LOOK_ALIKE = mapOf(
        'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c', 'у' to 'y', 'х' to 'x',
        'к' to 'k', 'і' to 'i', 'ј' to 'j', 'ѕ' to 's',
    )
}
