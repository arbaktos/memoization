package com.example.android.memoization.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeSidesTest {

    private fun side(shown: Shown, stability: Double? = null) = Side(
        sideId = if (shown == Shown.WORD) 1 else 2,
        wordPairId = 1,
        shown = shown,
        state = if (stability == null) SideState.New else SideState.Review,
        stability = stability,
    )

    private fun shownOf(practice: PracticeSides, vararg sides: Side) =
        practice.practised(sides.toList()).map { it.shown }

    @Test
    fun `an unknown or missing setting falls back to smart switch`() {
        assertEquals(PracticeSides.SMART_SWITCH, PracticeSides.fromName(null))
        assertEquals(PracticeSides.SMART_SWITCH, PracticeSides.fromName(""))
        assertEquals(PracticeSides.SMART_SWITCH, PracticeSides.fromName("SOMETHING_ELSE"))
        assertEquals(PracticeSides.DEFAULT, PracticeSides.SMART_SWITCH)
    }

    @Test
    fun `a stored setting round-trips through its name`() {
        for (setting in PracticeSides.entries) {
            assertEquals(setting, PracticeSides.fromName(setting.name))
        }
    }

    @Test
    fun `the fixed settings practise just their own sides`() {
        val word = side(Shown.WORD, 5.0)
        val meaning = side(Shown.MEANING)

        assertEquals(listOf(Shown.WORD), shownOf(PracticeSides.WORD_TO_MEANING, word, meaning))
        assertEquals(listOf(Shown.MEANING), shownOf(PracticeSides.MEANING_TO_WORD, word, meaning))
        assertEquals(listOf(Shown.WORD, Shown.MEANING), shownOf(PracticeSides.BOTH, word, meaning))
    }

    @Test
    fun `smart switch starts with the word side alone`() {
        assertEquals(listOf(Shown.WORD), shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD), side(Shown.MEANING)))
        assertEquals(listOf(Shown.WORD), shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD, 6.9), side(Shown.MEANING)))
    }

    @Test
    fun `smart switch adds the meaning side once the word side reaches level three`() {
        assertEquals(
            listOf(Shown.WORD, Shown.MEANING),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD, 7.0), side(Shown.MEANING)),
        )
    }

    @Test
    fun `smart switch keeps a meaning side that has been practised even after the word side lapses`() {
        assertEquals(
            listOf(Shown.WORD, Shown.MEANING),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD, 0.5), side(Shown.MEANING, 1.0)),
        )
    }

    @Test
    fun `smart switch does not mind the order of the sides`() {
        assertEquals(
            listOf(Shown.MEANING, Shown.WORD),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.MEANING), side(Shown.WORD, 10.0)),
        )
    }
}
