package com.example.android.memoization.domain.scheduler

import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState
import java.util.TimeZone
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.random.Random

/**
 * FSRS-6, the scheduler Anki adopted: it models a memory by its stability (how many days it
 * holds), its difficulty for this learner, and its retrievability (the chance of recalling it
 * right now, which decays with time). After each rating it recomputes stability and difficulty
 * and asks for the next review on the day retrievability would fall to [desiredRetention].
 *
 * Transcribed from the reference implementation (open-spaced-repetition/py-fsrs, MIT). The
 * expected values in FsrsTest were generated independently with ts-fsrs 5.4.1, which implements
 * the same FSRS-6.0 - excluding its cross-button ordering rule (good >= hard + 1), which is a
 * UI constraint of that library rather than part of the algorithm.
 *
 * Pure Kotlin: no Android, no clock of its own, so it can be tested with fixed instants.
 * Days elapsed between reviews are calendar days in [zone], the same days due-ness is judged by.
 */
class Fsrs(
    private val w: DoubleArray = DEFAULT_PARAMETERS,
    private val desiredRetention: Double = DEFAULT_RETENTION,
    private val maximumInterval: Int = MAXIMUM_INTERVAL,
    private val fuzz: Boolean = true,
    private val random: Random = Random.Default,
    private val zone: TimeZone = TimeZone.getDefault(),
) {
    init {
        require(w.size == DEFAULT_PARAMETERS.size) { "FSRS-6 takes ${DEFAULT_PARAMETERS.size} parameters" }
        require(desiredRetention > 0 && desiredRetention <= 1) { "retention must be in (0, 1]" }
    }

    private val decay: Double = -w[20]
    private val factor: Double = 0.9.pow(1 / decay) - 1

    /** Stability of a side rated for the very first time. */
    fun initialStability(rating: Rating): Double = clampStability(w[rating.grade - 1])

    /** Difficulty of a side rated for the very first time. */
    fun initialDifficulty(rating: Rating): Double = clampDifficulty(rawInitialDifficulty(rating))

    private fun rawInitialDifficulty(rating: Rating): Double =
        w[4] - exp(w[5] * (rating.grade - 1)) + 1

    /** The chance of recalling a memory of [stability] after [elapsedDays] days. */
    fun retrievability(stability: Double, elapsedDays: Long): Double =
        (1 + factor * elapsedDays / stability).pow(decay)

    /** Days until retrievability would fall to the desired retention; at least one, whole days. */
    fun nextInterval(stability: Double): Int {
        val days = stability / factor * (desiredRetention.pow(1 / decay) - 1)
        return min(max(round(days).toInt(), 1), maximumInterval)
    }

    /** A second look on the same calendar day moves stability only a little. */
    fun shortTermStability(stability: Double, rating: Rating): Double {
        var increase = exp(w[17] * (rating.grade - 3 + w[18])) * stability.pow(-w[19])
        if (rating != Rating.Again) increase = max(increase, 1.0)
        return clampStability(stability * increase)
    }

    fun nextDifficulty(difficulty: Double, rating: Rating): Double {
        val deltaDifficulty = -(w[6] * (rating.grade - 3))
        val damped = difficulty + (10.0 - difficulty) * deltaDifficulty / 9.0
        // Mean reversion towards the difficulty an "easy" first answer would imply, so a run of
        // bad answers cannot pin a side at 10 forever.
        val reverted = w[7] * rawInitialDifficulty(Rating.Easy) + (1 - w[7]) * damped
        return clampDifficulty(reverted)
    }

    /**
     * Recalling grows stability the most when it was nearly forgotten (low retrievability),
     * and the least when the side is already difficult or already long-lived.
     */
    fun nextRecallStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double,
        rating: Rating,
    ): Double {
        val hardPenalty = if (rating == Rating.Hard) w[15] else 1.0
        val easyBonus = if (rating == Rating.Easy) w[16] else 1.0
        val growth = exp(w[8]) * (11 - difficulty) * stability.pow(-w[9]) *
            (exp((1 - retrievability) * w[10]) - 1) * hardPenalty * easyBonus
        return clampStability(stability * (1 + growth))
    }

    /** Forgetting does not erase what was learnt: stability drops, it does not reset. */
    fun nextForgetStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double,
    ): Double {
        val longTerm = w[11] * difficulty.pow(-w[12]) *
            ((stability + 1).pow(w[13]) - 1) * exp((1 - retrievability) * w[14])
        val shortTerm = stability / exp(w[17] * w[18])
        return clampStability(min(longTerm, shortTerm))
    }

    /**
     * Spreads intervals a little so that a batch of pairs added on the same day does not keep
     * coming back as one lump. Short intervals are left alone.
     */
    fun fuzzedInterval(days: Int): Int {
        if (!fuzz || days < FUZZ_FLOOR) return days
        var delta = 1.0
        for ((start, end, weight) in FUZZ_RANGES) {
            delta += weight * max(min(days.toDouble(), end) - start, 0.0)
        }
        var minIvl = max(2, round(days - delta).toInt())
        val maxIvl = min(round(days + delta).toInt(), maximumInterval)
        minIvl = min(minIvl, maxIvl)
        // Floor, not round: rounding a value drawn from [min, max + 1) can land on max + 1
        // and push the interval outside the range it was just given.
        val fuzzed = random.nextDouble() * (maxIvl - minIvl + 1) + minIvl
        return min(floor(fuzzed).toInt(), maximumInterval)
    }

    /** The side as it must be stored after [rating] given at [now] (epoch millis). */
    fun review(side: Side, rating: Rating, now: Long): Side {
        val stability = side.stability
        val difficulty = side.difficulty
        val elapsed = side.lastReview?.let { elapsedDays(it, now, zone) }

        val newStability: Double
        val newDifficulty: Double
        if (stability == null || difficulty == null) {
            newStability = initialStability(rating)
            newDifficulty = initialDifficulty(rating)
        } else if (elapsed != null && elapsed < 1) {
            newStability = shortTermStability(stability, rating)
            newDifficulty = nextDifficulty(difficulty, rating)
        } else {
            val retrievability = retrievability(stability, elapsed ?: 0)
            newStability = if (rating == Rating.Again) {
                nextForgetStability(difficulty, stability, retrievability)
            } else {
                nextRecallStability(difficulty, stability, retrievability, rating)
            }
            newDifficulty = nextDifficulty(difficulty, rating)
        }

        val interval = fuzzedInterval(nextInterval(newStability))

        return side.copy(
            state = SideState.Review,
            stability = newStability,
            difficulty = newDifficulty,
            due = now + interval * DAY_MILLIS,
            lastReview = now,
            reps = side.reps + 1,
            lapses = side.lapses + if (rating == Rating.Again && side.state == SideState.Review) 1 else 0,
        )
    }

    private fun clampStability(stability: Double) = max(stability, STABILITY_MIN)

    private fun clampDifficulty(difficulty: Double) =
        min(max(difficulty, MIN_DIFFICULTY), MAX_DIFFICULTY)

    companion object {
        /** FSRS-6 defaults, fitted to some 700 million real reviews. */
        val DEFAULT_PARAMETERS = doubleArrayOf(
            0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666,
            0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658,
            0.1542,
        )
        const val DEFAULT_RETENTION = 0.9
        const val MAXIMUM_INTERVAL = 36_500
        const val STABILITY_MIN = 0.001
        const val MIN_DIFFICULTY = 1.0
        const val MAX_DIFFICULTY = 10.0

        private const val FUZZ_FLOOR = 2.5
        /** start, end, weight - the wider the interval, the more it may be nudged. */
        private val FUZZ_RANGES = listOf(
            Triple(2.5, 7.0, 0.15),
            Triple(7.0, 20.0, 0.1),
            Triple(20.0, Double.MAX_VALUE, 0.05),
        )
    }
}
