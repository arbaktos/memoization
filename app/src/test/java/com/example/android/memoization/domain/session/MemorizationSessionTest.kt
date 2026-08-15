package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.Rating
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.data.model.WordStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import kotlin.random.Random

class MemorizationSessionTest {

    private val now = 1_800_000_000_000L

    private fun pair(id: Long, level: WordStatus = WordStatus.Level2) = WordPair(
        parentStackId = 1, word1 = "w$id", word2 = "t$id",
        lastRep = Date(now - 10 * 86_400_000L), wordPairId = id, level = level
    )

    private fun session(vararg ids: Long, seed: Int = 1) =
        MemorizationSession(ids.map { pair(it) }, Random(seed))

    private fun MemorizationSession.drain(): List<Long> {
        val seen = mutableListOf<Long>()
        while (!state().isFinished) {
            seen += state().current!!.wordPairId
            rate(Rating.Good, now)
        }
        return seen
    }

    @Test
    fun `shuffles once on entry and then keeps the order`() {
        val ids = LongArray(20) { it + 1L }
        val s = session(*ids, seed = 7)

        val first = s.state().current
        assertEquals(first, s.state().current)
        assertEquals(first, s.state().current)

        val order = s.drain()
        assertEquals(ids.toSet(), order.toSet())
        assertNotEquals("a 20-item shuffle should not be the identity", ids.toList(), order)
    }

    @Test
    fun `an empty session is finished immediately`() {
        val s = MemorizationSession(emptyList())

        assertTrue(s.state().isFinished)
        assertNull(s.state().current)
        assertEquals(0, s.state().remaining)
        assertEquals(1f, s.state().progress, 0f)
    }

    @Test
    fun `progress is closed pairs over the starting total and holds still on again`() {
        val s = session(1, 2, 3, 4)
        assertEquals(4, s.state().total)
        assertEquals(0f, s.state().progress, 0f)

        assertEquals(0.25f, s.rate(Rating.Good, now).state.progress, 0.001f)
        assertEquals(0.25f, s.rate(Rating.Again, now).state.progress, 0.001f)
        assertEquals(0.5f, s.rate(Rating.Hard, now).state.progress, 0.001f)
        assertEquals(4, s.state().total)
    }

    @Test
    fun `good removes the pair, persists it one level up and counts down`() {
        val s = session(1, 2, 3)
        val first = s.state().current!!

        val outcome = s.rate(Rating.Good, now)

        assertEquals(WordStatus.Level3, outcome.toPersist?.level)
        assertEquals(Date(now), outcome.toPersist?.lastRep)
        assertEquals(first.wordPairId, outcome.toPersist?.wordPairId)
        assertEquals(2, outcome.state.remaining)
        assertNotEquals(first.wordPairId, outcome.state.current?.wordPairId)
    }

    @Test
    fun `hard removes the pair and persists it at the same level`() {
        val s = session(1, 2)

        val outcome = s.rate(Rating.Hard, now)

        assertEquals(WordStatus.Level2, outcome.toPersist?.level)
        assertEquals(Date(now), outcome.toPersist?.lastRep)
        assertEquals(1, outcome.state.remaining)
    }

    @Test
    fun `again sends the pair to the back and persists the reset once`() {
        val s = session(1, 2, 3)
        val lapsed = s.state().current!!

        val outcome = s.rate(Rating.Again, now)

        assertEquals(WordStatus.Level1, outcome.toPersist?.level)
        assertEquals(lapsed.wordPairId, outcome.toPersist?.wordPairId)
        assertEquals(3, outcome.state.remaining)
        assertNotEquals(lapsed.wordPairId, outcome.state.current?.wordPairId)

        // Work through the other two; the lapsed one comes round last.
        s.rate(Rating.Good, now)
        s.rate(Rating.Good, now)
        assertEquals(lapsed.wordPairId, s.state().current?.wordPairId)
        assertEquals(WordStatus.Level1, s.state().current?.level)
    }

    @Test
    fun `a second again on the same pair persists nothing`() {
        val s = session(1)

        assertNotNull(s.rate(Rating.Again, now).toPersist)
        assertNull(s.rate(Rating.Again, now).toPersist)
        assertEquals(1, s.state().remaining)
    }

    @Test
    fun `good after again closes the pair without raising its level`() {
        val s = session(1)

        s.rate(Rating.Again, now)
        val outcome = s.rate(Rating.Good, now)

        assertNull(outcome.toPersist)
        assertTrue(outcome.state.isFinished)
    }

    @Test
    fun `hard after again also closes the pair without a write`() {
        val s = session(1)

        s.rate(Rating.Again, now)
        val outcome = s.rate(Rating.Hard, now)

        assertNull(outcome.toPersist)
        assertTrue(outcome.state.isFinished)
    }

    @Test
    fun `the last good finishes the session`() {
        val s = session(1, 2)

        s.rate(Rating.Good, now)
        assertFalse(s.state().isFinished)
        val outcome = s.rate(Rating.Good, now)

        assertTrue(outcome.state.isFinished)
        assertEquals(0, outcome.state.remaining)
    }

    @Test
    fun `serial grows on every rating so a returning pair reads as a new attempt`() {
        val s = session(1)

        assertEquals(0, s.state().serial)
        val afterAgain = s.rate(Rating.Again, now).state
        assertEquals(1, afterAgain.serial)
        assertEquals(1L, afterAgain.current?.wordPairId)
        assertEquals(2, s.rate(Rating.Again, now).state.serial)
    }
}
