package com.example.android.memoization.domain.search

import com.example.android.memoization.data.model.WordPair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PairSearchTest {

    private fun pair(id: Long, word: String, meaning: String?) =
        WordPair(parentStackId = 1, word1 = word, word2 = meaning, wordPairId = id)

    private val stack = listOf(
        pair(1, "često", "часто"),
        pair(2, "đak", "ученик"),
        pair(3, "šetnju", "прогулки"),
        pair(4, "u redu je", "все нормально (ответ на извините)"),
        pair(5, "orphan", null),
    )

    private fun found(query: String) = PairSearch.filter(stack, query).map { it.wordPairId }

    @Test
    fun `a blank query keeps every pair in its order`() {
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), found(""))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), found("   "))
    }

    @Test
    fun `the query is looked for in the word and in the meaning`() {
        assertEquals(listOf(1L), found("čest"))
        assertEquals(listOf(1L), found("част"))
        assertEquals(listOf(4L), found("извини"))
    }

    @Test
    fun `case and diacritics do not matter`() {
        assertEquals(listOf(1L), found("CESTO"))
        assertEquals(listOf(3L), found("setnju"))
        assertEquals(listOf(2L), found("dak"))
        assertEquals(listOf(1L), found("Часто"))
    }

    @Test
    fun `a pair without a meaning is searched by its word alone`() {
        assertEquals(listOf(5L), found("orph"))
        assertTrue(found("zzz").isEmpty())
    }

    @Test
    fun `matches agrees with filter`() {
        assertTrue(PairSearch.matches(stack[0], "cesto"))
        assertFalse(PairSearch.matches(stack[0], "setnju"))
        assertTrue(PairSearch.matches(stack[0], ""))
    }

    @Test
    fun `folding strips marks but keeps letters and spaces`() {
        assertEquals("cesto", PairSearch.fold("Često"))
        assertEquals("u redu je", PairSearch.fold("  U redu je "))
        assertEquals("ежик", PairSearch.fold("ёжик"))
    }
}
