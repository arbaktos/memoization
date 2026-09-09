package com.example.android.memoization.data.model

import com.example.android.memoization.domain.scheduler.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class PracticeSidesTest {

    private val now = 1_800_000_000_000L
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    private fun side(shown: Shown, stability: Double? = null, lastReview: Long? = null) = Side(
        sideId = if (shown == Shown.WORD) 1 else 2,
        wordPairId = 1,
        shown = shown,
        state = if (stability == null) SideState.New else SideState.Review,
        stability = stability,
        lastReview = lastReview,
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
    fun `smart switch hands over to the meaning side once the word side reaches level three`() {
        assertEquals(
            listOf(Shown.MEANING),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD, 7.0), side(Shown.MEANING)),
        )
    }

    @Test
    fun `smart switch hands over the day after the word side got there, not the same day`() {
        val sides = listOf(side(Shown.WORD, 9.0, lastReview = now), side(Shown.MEANING))

        // Rated up to Level3 today: still the word side, so the meaning side is not due yet.
        assertEquals(listOf(Shown.WORD), PracticeSides.SMART_SWITCH.practised(sides, now, utc).map { it.shown })
        assertEquals(
            listOf(Shown.WORD),
            PracticeSides.SMART_SWITCH.practised(sides, now + 11 * 3_600_000L, utc).map { it.shown }
        )
        // From the next calendar day on, the meaning side.
        assertEquals(
            listOf(Shown.MEANING),
            PracticeSides.SMART_SWITCH.practised(sides, now + DAY_MILLIS, utc).map { it.shown }
        )
    }

    @Test
    fun `smart switch keeps the meaning side alone after the word side lapses`() {
        assertEquals(
            listOf(Shown.MEANING),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD, 0.5), side(Shown.MEANING, 1.0)),
        )
    }

    @Test
    fun `smart switch does not mind the order of the sides`() {
        assertEquals(
            listOf(Shown.MEANING),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.MEANING), side(Shown.WORD, 10.0)),
        )
        assertEquals(
            listOf(Shown.WORD),
            shownOf(PracticeSides.SMART_SWITCH, side(Shown.MEANING), side(Shown.WORD)),
        )
    }

    @Test
    fun `smart switch practises the only side a pair has`() {
        assertEquals(listOf(Shown.WORD), shownOf(PracticeSides.SMART_SWITCH, side(Shown.WORD)))
        assertEquals(listOf(Shown.MEANING), shownOf(PracticeSides.SMART_SWITCH, side(Shown.MEANING)))
        assertEquals(emptyList<Shown>(), shownOf(PracticeSides.SMART_SWITCH))
    }
}
