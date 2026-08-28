package com.example.android.memoization.ui.features.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedWordTest {

    @Test
    fun `a plain word comes through as it is`() {
        assertEquals("хлеб", SharedWord.from("хлеб"))
    }

    @Test
    fun `a selection keeps neither the spaces nor the line breaks around it`() {
        assertEquals("hleb", SharedWord.from("  hleb\n"))
        // What a web page selection tends to bring with it: a non-breaking and a zero-width space.
        assertEquals("hleb", SharedWord.from("\u00a0hleb\u200b"))
    }

    @Test
    fun `a selection of several words is one line`() {
        assertEquals("dobar dan", SharedWord.from("dobar\n   dan"))
    }

    @Test
    fun `quotation marks around a selection are not part of the word`() {
        assertEquals("hleb", SharedWord.from("\"hleb\""))
        assertEquals("hleb", SharedWord.from("«hleb»"))
        assertEquals("hleb", SharedWord.from("“ hleb ”"))
    }

    @Test
    fun `an apostrophe inside a word is left alone`() {
        assertEquals("l'eau", SharedWord.from("l'eau"))
    }

    @Test
    fun `nothing to learn in an empty share`() {
        assertNull(SharedWord.from(null))
        assertNull(SharedWord.from(""))
        assertNull(SharedWord.from("   \n "))
        assertNull(SharedWord.from("\"\""))
    }

    @Test
    fun `a shared article is cut to a length a word field can hold`() {
        val long = "a".repeat(SharedWord.MAX_LENGTH + 50)

        assertEquals(SharedWord.MAX_LENGTH, SharedWord.from(long)?.length)
    }
}
