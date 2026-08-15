package com.example.android.memoization.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSidesTest {

    @Test
    fun `an unknown or missing setting falls back to word to meaning`() {
        assertEquals(PracticeSides.WORD_TO_MEANING, PracticeSides.fromName(null))
        assertEquals(PracticeSides.WORD_TO_MEANING, PracticeSides.fromName(""))
        assertEquals(PracticeSides.WORD_TO_MEANING, PracticeSides.fromName("SOMETHING_ELSE"))
        assertEquals(PracticeSides.DEFAULT, PracticeSides.WORD_TO_MEANING)
    }

    @Test
    fun `a stored setting round-trips through its name`() {
        for (setting in PracticeSides.entries) {
            assertEquals(setting, PracticeSides.fromName(setting.name))
        }
    }

    @Test
    fun `each setting includes just its own sides`() {
        assertTrue(PracticeSides.WORD_TO_MEANING.includes(Shown.WORD))
        assertFalse(PracticeSides.WORD_TO_MEANING.includes(Shown.MEANING))
        assertTrue(PracticeSides.MEANING_TO_WORD.includes(Shown.MEANING))
        assertFalse(PracticeSides.MEANING_TO_WORD.includes(Shown.WORD))
        assertTrue(PracticeSides.BOTH.includes(Shown.WORD))
        assertTrue(PracticeSides.BOTH.includes(Shown.MEANING))
    }
}
