package com.example.android.memoization.data.model

import com.example.android.memoization.data.database.MIGRATION_D0_GOOD_FOR_TEST
import com.example.android.memoization.domain.scheduler.Fsrs
import com.example.android.memoization.domain.scheduler.Rating
import org.junit.Assert.assertEquals
import org.junit.Test

class WordStatusTest {

    @Test
    fun `a side that has never been rated reads as level one`() {
        assertEquals(WordStatus.Level1, WordStatus.fromStability(null))
    }

    @Test
    fun `the level changes on the day the old schedule would have`() {
        assertEquals(WordStatus.Level1, WordStatus.fromStability(1.99))
        assertEquals(WordStatus.Level2, WordStatus.fromStability(2.0))
        assertEquals(WordStatus.Level2, WordStatus.fromStability(6.99))
        assertEquals(WordStatus.Level3, WordStatus.fromStability(7.0))
        assertEquals(WordStatus.Level3, WordStatus.fromStability(13.99))
        assertEquals(WordStatus.Level4, WordStatus.fromStability(14.0))
        assertEquals(WordStatus.Level4, WordStatus.fromStability(29.99))
        assertEquals(WordStatus.Learned, WordStatus.fromStability(30.0))
        assertEquals(WordStatus.Learned, WordStatus.fromStability(1000.0))
    }

    @Test
    fun `the migration seeds sides with the difficulty of a good answer`() {
        // The 2 -> 3 migration writes this constant into every migrated side; if the FSRS
        // parameters ever change, this catches the two drifting apart.
        assertEquals(
            Fsrs().initialDifficulty(Rating.Good),
            MIGRATION_D0_GOOD_FOR_TEST,
            1e-9
        )
    }
}
