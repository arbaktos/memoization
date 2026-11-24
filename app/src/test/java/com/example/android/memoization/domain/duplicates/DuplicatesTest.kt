package com.example.android.memoization.domain.duplicates

import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState
import com.example.android.memoization.data.model.WordPair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicatesTest {

    private fun newSides(pairId: Long) = listOf(
        Side(sideId = pairId * 10, wordPairId = pairId, shown = Shown.WORD),
        Side(sideId = pairId * 10 + 1, wordPairId = pairId, shown = Shown.MEANING),
    )

    private fun pair(id: Long, word: String, meaning: String?, sides: List<Side> = newSides(id)) =
        WordPair(parentStackId = 1, word1 = word, word2 = meaning, wordPairId = id, sides = sides)

    private fun learned(pairId: Long, word: Double, meaning: Double) = listOf(
        Side(pairId * 10, pairId, Shown.WORD, SideState.Review, stability = word, difficulty = 5.0, due = 1L, lastReview = 1L, reps = 4),
        Side(pairId * 10 + 1, pairId, Shown.MEANING, SideState.Review, stability = meaning, difficulty = 3.0, due = 2L, lastReview = 2L, reps = 2),
    )

    private fun ids(proposal: MergeProposal) = proposal.pairs.map { it.wordPairId }

    // --- the key ---

    @Test
    fun `case, outer and doubled spaces, diacritics and look-alike letters fold to one key`() {
        assertEquals(Duplicates.key("sada"), Duplicates.key("  Sada "))
        assertEquals(Duplicates.key("u redu"), Duplicates.key("u  redu"))
        assertEquals(Duplicates.key("tražiti"), Duplicates.key("traziti"))
        assertEquals(Duplicates.key("đak"), Duplicates.key("dak"))
        // the second "o" is Cyrillic
        assertEquals(Duplicates.key("ako"), Duplicates.key("akо"))
    }

    @Test
    fun `a near-miss is a different word`() {
        assertNotEquals(Duplicates.key("trazit"), Duplicates.key("tražiti"))
    }

    @Test
    fun `russian и and й stay different letters`() {
        assertNotEquals(Duplicates.key("мой"), Duplicates.key("мои"))
    }

    // --- exact copies ---

    @Test
    fun `identical pairs ignoring case and spaces are exact copies, the oldest kept`() {
        val report = Duplicates.find(
            listOf(pair(60, "Sada ", "сейчас"), pair(49, "sada", "сейчас"), pair(70, "kuća", "дом")),
            dismissed = emptySet(),
        )
        assertEquals(1, report.copies.size)
        assertEquals(49L, report.copies[0].keep.wordPairId)
        assertEquals(listOf(60L), report.copies[0].copies.map { it.wordPairId })
        assertTrue(report.proposals.isEmpty())
    }

    @Test
    fun `three copies make one group`() {
        val report = Duplicates.find(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "сейчас"), pair(3, "SADA", "сейчас")),
            dismissed = emptySet(),
        )
        assertEquals(listOf(2L, 3L), report.copies.single().copies.map { it.wordPairId })
    }

    @Test
    fun `nothing alike, nothing found`() {
        val report = Duplicates.find(listOf(pair(1, "sada", "сейчас"), pair(2, "kuća", "дом")), emptySet())
        assertTrue(report.isEmpty)
        assertEquals(0, report.toReview)
    }

    // --- merge proposals ---

    @Test
    fun `a shared word with different meanings is proposed for merging`() {
        val report = Duplicates.find(listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "теперь")), emptySet())
        assertTrue(report.copies.isEmpty())
        assertEquals(listOf(1L, 2L), ids(report.proposals.single()))
    }

    @Test
    fun `a shared meaning with different words is proposed for merging`() {
        val report = Duplicates.find(listOf(pair(1, "sada", "сейчас"), pair(2, "sad", "сейчас")), emptySet())
        assertEquals(listOf(1L, 2L), ids(report.proposals.single()))
    }

    @Test
    fun `pairs that differ only in spelling are a proposal, not a copy`() {
        val report = Duplicates.find(listOf(pair(1, "ako", "если"), pair(2, "akо", "если")), emptySet())
        assertTrue(report.copies.isEmpty())
        assertEquals(listOf(1L, 2L), ids(report.proposals.single()))
    }

    @Test
    fun `two pairs without a meaning do not match on it`() {
        val report = Duplicates.find(listOf(pair(1, "sada", null), pair(2, "kuća", "")), emptySet())
        assertTrue(report.isEmpty)
    }

    @Test
    fun `exact copies take part in proposals through their kept pair only`() {
        val report = Duplicates.find(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "сейчас"), pair(3, "sada", "теперь")),
            emptySet(),
        )
        assertEquals(listOf(2L), report.copies.single().copies.map { it.wordPairId })
        assertEquals(listOf(1L, 3L), ids(report.proposals.single()))
        assertEquals(2, report.toReview)
    }

    @Test
    fun `three pairs sharing a word are one proposal`() {
        val report = Duplicates.find(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "теперь"), pair(3, "sada", "сразу")),
            emptySet(),
        )
        assertEquals(listOf(1L, 2L, 3L), ids(report.proposals.single()))
    }

    @Test
    fun `a cancelled match is not proposed again`() {
        val report = Duplicates.find(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "теперь")),
            dismissed = setOf(DismissedMatch.of(2, 1)),
        )
        assertTrue(report.isEmpty)
    }

    @Test
    fun `a new pair is offered against each existing one, not the cancelled match again`() {
        val report = Duplicates.find(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "теперь"), pair(3, "sada", "сразу")),
            dismissed = setOf(DismissedMatch.of(1, 2)),
        )
        assertEquals(listOf(listOf(1L, 3L), listOf(2L, 3L)), report.proposals.map(::ids))
    }

    @Test
    fun `cancelling a proposal dismisses every match inside it`() {
        val proposal = Duplicates.find(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "теперь"), pair(3, "sada", "сразу")),
            emptySet(),
        ).proposals.single()
        assertEquals(
            setOf(DismissedMatch.of(1, 2), DismissedMatch.of(1, 3), DismissedMatch.of(2, 3)),
            proposal.matches(),
        )
    }

    // --- what the merge dialog offers ---

    @Test
    fun `the shared side is fixed and the other is the comma-joined draft`() {
        val proposal = Duplicates.find(listOf(pair(1, "sada", "сейчас"), pair(2, "Sada", "теперь")), emptySet())
            .proposals.single()
        assertEquals(SideChoice.Fixed("sada"), proposal.word)
        assertEquals(SideChoice.Edit("сейчас, теперь"), proposal.meaning)
    }

    @Test
    fun `differing spellings of the shared side are offered as a choice, oldest first`() {
        val proposal = Duplicates.find(listOf(pair(1, "ako", "если"), pair(2, "akо", "если")), emptySet())
            .proposals.single()
        assertEquals(SideChoice.Spelling(listOf("ako", "akо")), proposal.word)
        assertEquals(SideChoice.Fixed("если"), proposal.meaning)
    }

    @Test
    fun `the draft skips a missing meaning and repeats nothing`() {
        val proposal = Duplicates.find(
            listOf(pair(1, "sada", null), pair(2, "sada", "сейчас"), pair(3, "sada", "теперь")),
            emptySet(),
        ).proposals.single()
        assertEquals(SideChoice.Edit("сейчас, теперь"), proposal.meaning)
    }

    // --- the merge itself ---

    @Test
    fun `the oldest pair is kept with the chosen words and the others hidden`() {
        val plan = Duplicates.plan(listOf(pair(2, "sada", "теперь"), pair(1, "sada", "сейчас")), "sada", "сейчас, теперь")
        assertEquals(1L, plan.keepPairId)
        assertEquals("sada", plan.word1)
        assertEquals("сейчас, теперь", plan.word2)
        assertEquals(listOf(2L), plan.hidePairIds)
    }

    @Test
    fun `each side takes the weaker schedule, kept under the kept pair's side ids`() {
        val plan = Duplicates.plan(
            listOf(pair(1, "sada", "сейчас", learned(1, word = 20.0, meaning = 3.0)), pair(2, "sada", "теперь", learned(2, word = 5.0, meaning = 8.0))),
            "sada", "сейчас, теперь",
        )
        val word = plan.sides.single { it.shown == Shown.WORD }
        val meaning = plan.sides.single { it.shown == Shown.MEANING }
        assertEquals(10L, word.sideId)
        assertEquals(1L, word.wordPairId)
        assertEquals(5.0, word.stability!!, 1e-9)
        assertEquals(11L, meaning.sideId)
        assertEquals(3.0, meaning.stability!!, 1e-9)
    }

    @Test
    fun `a side never practised is the weakest of all`() {
        val plan = Duplicates.plan(
            listOf(pair(1, "sada", "сейчас", learned(1, word = 20.0, meaning = 30.0)), pair(2, "sada", "сейчас")),
            "sada", "сейчас",
        )
        assertTrue(plan.sides.all { it.isNew && it.stability == null && it.reps == 0 })
        assertEquals(listOf(10L, 11L), plan.sides.map { it.sideId })
    }

    @Test
    fun `the hidden pairs' answers move onto the kept sides of the same direction`() {
        val plan = Duplicates.plan(
            listOf(pair(1, "sada", "сейчас"), pair(2, "sada", "теперь"), pair(3, "sada", "сразу")),
            "sada", "x",
        )
        assertEquals(mapOf(20L to 10L, 21L to 11L, 30L to 10L, 31L to 11L), plan.moveReviews)
    }

    @Test
    fun `removing copies keeps the oldest pair's words`() {
        val group = Duplicates.find(listOf(pair(1, "Sada", "сейчас"), pair(2, "sada", "Сейчас")), emptySet()).copies.single()
        val plan = group.plan()
        assertEquals("Sada", plan.word1)
        assertEquals("сейчас", plan.word2)
        assertFalse(plan.hidePairIds.contains(1L))
    }
}
