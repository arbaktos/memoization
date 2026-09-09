package com.example.android.memoization.domain.scheduler

import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState
import com.example.android.memoization.data.model.Shown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone
import kotlin.random.Random

/**
 * Expected values were generated with ts-fsrs 5.4.1 (FSRS-6.0), an independent port, through its
 * low-level FSRSAlgorithm API - so they are not a copy of this implementation's own arithmetic.
 * Intervals follow the reference rule round(stability) at retention 0.9; ts-fsrs additionally
 * forces good >= hard + 1 across its four buttons, which is a UI rule of that library, not FSRS.
 */
class FsrsTest {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    // Instants below step by whole days; UTC keeps each step exactly one calendar day.
    private val fsrs = Fsrs(fuzz = false, zone = utc)
    private val eps = 1e-6

    private fun newSide() = Side(sideId = 1, wordPairId = 1, shown = Shown.WORD)

    /** Walks a chain of (rating, days since the previous review) and returns the side each time. */
    private fun walk(steps: List<Pair<Rating, Long>>): List<Side> {
        var side = newSide()
        var now = 1_800_000_000_000L
        return steps.map { (rating, daysLater) ->
            now += daysLater * DAY_MILLIS
            side = fsrs.review(side, rating, now)
            side
        }
    }

    private fun assertChain(
        steps: List<Pair<Rating, Long>>,
        expected: List<Triple<Int, Double, Double>>, // interval days, stability, difficulty
    ) {
        val sides = walk(steps)
        sides.forEachIndexed { i, side ->
            val (interval, stability, difficulty) = expected[i]
            // The reference rounds its intermediate values to eight decimals, so a chain drifts
            // by a fraction of that; the interval, which is what the learner sees, must match.
            assertEquals("stability at step $i", stability, side.stability!!, 1e-4)
            assertEquals("difficulty at step $i", difficulty, side.difficulty!!, 1e-4)
            assertEquals(
                "interval at step $i",
                interval.toLong(),
                (side.due!! - side.lastReview!!) / DAY_MILLIS
            )
        }
    }

    // --- constants ----------------------------------------------------------------------

    @Test
    fun `initial stability is the parameter for the grade`() {
        assertEquals(0.212, fsrs.initialStability(Rating.Again), eps)
        assertEquals(1.2931, fsrs.initialStability(Rating.Hard), eps)
        assertEquals(2.3065, fsrs.initialStability(Rating.Good), eps)
        assertEquals(8.2956, fsrs.initialStability(Rating.Easy), eps)
    }

    @Test
    fun `initial difficulty falls with the grade and clamps at one`() {
        assertEquals(6.4133, fsrs.initialDifficulty(Rating.Again), eps)
        assertEquals(5.11217071, fsrs.initialDifficulty(Rating.Hard), 1e-8)
        assertEquals(2.11810397, fsrs.initialDifficulty(Rating.Good), 1e-8)
        // The raw value for Easy is negative; difficulty is clamped to [1, 10].
        assertEquals(1.0, fsrs.initialDifficulty(Rating.Easy), eps)
    }

    @Test
    fun `first interval is the rounded initial stability`() {
        assertEquals(1, fsrs.nextInterval(fsrs.initialStability(Rating.Again)))
        assertEquals(1, fsrs.nextInterval(fsrs.initialStability(Rating.Hard)))
        assertEquals(2, fsrs.nextInterval(fsrs.initialStability(Rating.Good)))
        assertEquals(8, fsrs.nextInterval(fsrs.initialStability(Rating.Easy)))
    }

    @Test
    fun `interval is at least a day and at most the maximum`() {
        assertEquals(1, fsrs.nextInterval(0.001))
        assertEquals(36_500, fsrs.nextInterval(1_000_000.0))
    }

    @Test
    fun `a side's retrievability counts calendar days since its last rating`() {
        val now = 1_800_000_000_000L
        val side = newSide().copy(state = SideState.Review, stability = 10.0, difficulty = 5.0, lastReview = now)

        assertEquals(null, fsrs.retrievability(newSide(), now))
        assertEquals(1.0, fsrs.retrievability(side, now)!!, eps)
        assertEquals(fsrs.retrievability(10.0, 10), fsrs.retrievability(side, now + 10 * DAY_MILLIS)!!, eps)
        // Under 24 hours, but the next calendar day: one day elapsed, not none.
        assertEquals(fsrs.retrievability(10.0, 1), fsrs.retrievability(side, now + 20 * 3_600_000L)!!, eps)
    }

    @Test
    fun `retrievability starts at one and decays`() {
        assertEquals(1.0, fsrs.retrievability(10.0, 0), eps)
        val afterTen = fsrs.retrievability(10.0, 10)
        assertEquals(0.9, afterTen, 1e-3)
        assertTrue(fsrs.retrievability(10.0, 100) < afterTen)
    }

    // --- individual formulas ------------------------------------------------------------

    @Test
    fun `same-day stability never shrinks unless the answer was again`() {
        assertEquals(0.08335672, fsrs.shortTermStability(0.212, Rating.Again), 1e-8)
        assertEquals(0.212, fsrs.shortTermStability(0.212, Rating.Hard), 1e-8)
        assertEquals(0.24668919, fsrs.shortTermStability(0.212, Rating.Good), 1e-8)
        assertEquals(0.42437996, fsrs.shortTermStability(0.212, Rating.Easy), 1e-8)
        // The raw multiplier is below one here, so Hard and Good are held at the old value.
        assertEquals(2.3065, fsrs.shortTermStability(2.3065, Rating.Hard), 1e-8)
        assertEquals(2.3065, fsrs.shortTermStability(2.3065, Rating.Good), 1e-8)
        assertEquals(3.94605407, fsrs.shortTermStability(2.3065, Rating.Easy), 1e-8)
        assertEquals(3.32527684, fsrs.shortTermStability(10.9643, Rating.Again), 1e-8)
    }

    @Test
    fun `recall and forget stability match the reference`() {
        assertEquals(
            46.19830346,
            fsrs.nextRecallStability(2.1112, 10.9643, 0.9, Rating.Good),
            1e-6
        )
        assertEquals(1.45649445, fsrs.nextForgetStability(5.1122, 10.9643, 0.9), 1e-6)
    }

    @Test
    fun `difficulty rises on again and falls on good`() {
        assertEquals(4.99022837, fsrs.nextDifficulty(5.0, Rating.Good), 1e-8)
        assertEquals(6.66599536, fsrs.nextDifficulty(5.0, Rating.Hard), 1e-8)
        assertEquals(8.34176237, fsrs.nextDifficulty(5.0, Rating.Again), 1e-8)
        // Mean reversion keeps difficulty a hair off the ends, so a run of bad answers
        // cannot pin a side at 10 for good.
        assertEquals(9.98522837, fsrs.nextDifficulty(10.0, Rating.Again), 1e-8)
        assertEquals(1.0, fsrs.nextDifficulty(1.0, Rating.Easy), eps)
    }

    // --- reference chains ---------------------------------------------------------------

    @Test
    fun `good every time grows the interval fast`() {
        assertChain(
            listOf(
                Rating.Good to 0L, Rating.Good to 2L, Rating.Good to 11L,
                Rating.Good to 46L, Rating.Good to 163L
            ),
            listOf(
                Triple(2, 2.3065, 2.11810397),
                Triple(11, 10.96433194, 2.11121424),
                Triple(46, 46.28021494, 2.1043314),
                Triple(163, 162.86219887, 2.09745544),
                Triple(497, 497.44720499, 2.09058635),
            )
        )
    }

    @Test
    fun `forgetting drops stability but keeps some of what was learnt`() {
        assertChain(
            listOf(
                Rating.Good to 0L, Rating.Good to 2L, Rating.Again to 11L,
                Rating.Good to 1L, Rating.Good to 2L
            ),
            listOf(
                Triple(2, 2.3065, 2.11810397),
                Triple(11, 10.96433194, 2.11121424),
                // An 11-day memory falls back to a day and a half, not to zero.
                Triple(2, 1.5383372, 7.39223814),
                Triple(4, 3.5539099, 7.38007427),
                Triple(7, 7.17881897, 7.36792257),
            )
        )
    }

    @Test
    fun `hard answers grow slowly and push difficulty up`() {
        assertChain(
            listOf(Rating.Hard to 0L, Rating.Hard to 1L, Rating.Hard to 2L, Rating.Good to 3L),
            listOf(
                Triple(1, 1.2931, 5.11217071),
                Triple(3, 3.24941173, 6.74045952),
                Triple(6, 5.80614858, 7.8213935),
                Triple(10, 10.27823612, 7.80880048),
            )
        )
    }

    @Test
    fun `easy answers jump ahead`() {
        assertChain(
            listOf(Rating.Easy to 0L, Rating.Easy to 8L, Rating.Good to 30L),
            listOf(
                Triple(8, 8.2956, 1.0),
                Triple(66, 65.62422648, 1.0),
                Triple(162, 161.62234675, 1.0),
            )
        )
    }

    @Test
    fun `a first lapse then recovery`() {
        assertChain(
            listOf(Rating.Again to 0L, Rating.Good to 1L, Rating.Good to 2L),
            listOf(
                Triple(1, 0.212, 6.4133),
                Triple(2, 1.88678762, 6.40211507),
                Triple(6, 6.26916146, 6.39094132),
            )
        )
    }

    @Test
    fun `a mixed history stays in step with the reference`() {
        assertChain(
            listOf(
                Rating.Good to 0L, Rating.Hard to 2L, Rating.Good to 3L, Rating.Again to 6L,
                Rating.Hard to 1L, Rating.Good to 2L, Rating.Good to 5L
            ),
            listOf(
                Triple(2, 2.3065, 2.11810397),
                Triple(8, 7.51332013, 4.75285849),
                Triple(16, 16.3085, 4.743334),
                Triple(2, 1.62436951, 8.257398),
                Triple(3, 2.54822441, 8.82840796),
                Triple(5, 4.68358559, 8.81480792),
                Triple(9, 9.14925236, 8.80122148),
            )
        )
    }

    // --- review bookkeeping and fuzz -----------------------------------------------------

    @Test
    fun `review stamps the side and counts reps and lapses`() {
        val now = 1_800_000_000_000L
        val first = fsrs.review(newSide(), Rating.Good, now)

        assertEquals(SideState.Review, first.state)
        assertEquals(now, first.lastReview)
        assertEquals(1, first.reps)
        assertEquals(0, first.lapses)
        assertEquals(now + 2 * DAY_MILLIS, first.due)

        // A lapse only counts once the side had reached Review.
        val lapsed = fsrs.review(first, Rating.Again, now + 2 * DAY_MILLIS)
        assertEquals(1, lapsed.lapses)
        assertEquals(2, lapsed.reps)
        assertEquals(0, fsrs.review(newSide(), Rating.Again, now).lapses)
    }

    @Test
    fun `fuzz leaves short intervals alone and keeps long ones in range`() {
        val fuzzing = Fsrs(random = Random(42), zone = utc)

        assertEquals(1, fuzzing.fuzzedInterval(1))
        assertEquals(2, fuzzing.fuzzedInterval(2))
        // A month may move by a few days either way, a year by a few weeks.
        repeat(200) {
            val fuzzed = fuzzing.fuzzedInterval(30)
            assertTrue("30 fuzzed to $fuzzed", fuzzed in 27..33)
        }
        repeat(200) {
            val fuzzed = fuzzing.fuzzedInterval(365)
            assertTrue("365 fuzzed to $fuzzed", fuzzed in 345..385)
        }
    }

    @Test
    fun `fuzz off is the identity`() {
        for (days in listOf(1, 3, 30, 365, 36_500)) {
            assertEquals(days, fsrs.fuzzedInterval(days))
        }
    }
}
