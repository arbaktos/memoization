package com.example.android.memoization.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StackLevelsTest {

    private fun side(stability: Double?) = Side(sideId = 1, wordPairId = 1, shown = Shown.WORD, stability = stability)

    @Test
    fun `a side steps from new through learning and known to learned`() {
        assertEquals(0, side(null).step)
        assertEquals(1, side(1.0).step)
        assertEquals(1, side(6.0).step)
        assertEquals(2, side(7.0).step)
        assertEquals(2, side(29.0).step)
        assertEquals(3, side(30.0).step)
    }

    @Test
    fun `a one-way setting never covers the other side, smart switch and both cover each`() {
        assertTrue(PracticeSides.WORD_TO_MEANING.covers(Shown.WORD))
        assertFalse(PracticeSides.WORD_TO_MEANING.covers(Shown.MEANING))
        assertFalse(PracticeSides.MEANING_TO_WORD.covers(Shown.WORD))
        assertTrue(PracticeSides.SMART_SWITCH.covers(Shown.MEANING))
        assertTrue(PracticeSides.BOTH.covers(Shown.WORD))
    }
}
