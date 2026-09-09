package com.example.android.memoization.domain.search

import com.example.android.memoization.data.model.WordPair
import java.text.Normalizer

/**
 * Finding a pair in a stack by a few typed letters. A pair matches when the query occurs in
 * its word or in its meaning, and the comparison forgives what a keyboard makes hard: case,
 * and the marks on letters - "cesto" finds "često", "dj" is not needed to find "đ".
 */
object PairSearch {

    /** The pairs of [pairs] that [query] finds, in the order given; all of them for a blank query. */
    fun filter(pairs: List<WordPair>, query: String): List<WordPair> {
        val needle = fold(query)
        if (needle.isEmpty()) return pairs
        return pairs.filter { matchesFolded(it, needle) }
    }

    fun matches(pair: WordPair, query: String): Boolean {
        val needle = fold(query)
        return needle.isEmpty() || matchesFolded(pair, needle)
    }

    private fun matchesFolded(pair: WordPair, needle: String): Boolean =
        fold(pair.word1).contains(needle) || pair.word2?.let { fold(it).contains(needle) } == true

    /**
     * Lower case, marks stripped (č -> c, ё -> е, é -> e), the stroked letters that no mark
     * decomposes mapped by hand (đ -> d), and whitespace trimmed.
     */
    fun fold(text: String): String {
        val decomposed = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
        val sb = StringBuilder(decomposed.length)
        for (ch in decomposed) {
            when {
                Character.getType(ch) == Character.NON_SPACING_MARK.toInt() -> Unit
                ch in STROKED -> sb.append(STROKED.getValue(ch))
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private val STROKED = mapOf('đ' to 'd', 'ł' to 'l', 'ø' to 'o')
}
