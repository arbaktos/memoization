package com.example.android.memoization.domain.session

import com.example.android.memoization.data.model.Rating
import com.example.android.memoization.data.model.WordPair
import kotlin.random.Random

/**
 * One sitting with a stack: the due and New word pairs, shuffled once on entry and worked
 * through one at a time. A pair rated Again goes to the back of the queue and keeps coming
 * round until it is rated Hard or Good, so nothing leaves the session unrecalled.
 *
 * Pure Kotlin - the ViewModel feeds it ratings and persists whatever [Outcome.toPersist] says.
 */
class MemorizationSession(words: List<WordPair>, random: Random = Random.Default) {

    data class State(
        /** The pair on screen; null once the queue is empty. */
        val current: WordPair?,
        /** Pairs still in the queue, the current one included. */
        val remaining: Int,
        /** Bumped on every rating so the UI can tell "same pair, next attempt" apart. */
        val serial: Int,
    ) {
        val isFinished: Boolean get() = current == null
    }

    data class Outcome(
        /** The pair as it should be written to the database, or null if nothing changed. */
        val toPersist: WordPair?,
        val state: State,
    )

    private val queue = ArrayDeque(words.shuffled(random))
    private val lapsedIds = mutableSetOf<Long>()
    private var serial = 0

    fun state(): State = State(queue.firstOrNull(), queue.size, serial)

    /**
     * Again: reset to Level1 with today's date - written once, on the first lapse - and back of
     * the queue. Hard/Good on a fresh pair: written as rated. Hard/Good after an Again in this
     * session: the pair is done for today but earns no level, so nothing is written.
     */
    fun rate(rating: Rating, now: Long): Outcome {
        val word = queue.removeFirst()
        serial++
        val lapsed = word.wordPairId in lapsedIds
        val toPersist = when (rating) {
            Rating.Again -> {
                val reset = word.rated(Rating.Again, now)
                queue.addLast(reset)
                if (lapsed) null else {
                    lapsedIds += word.wordPairId
                    reset
                }
            }
            Rating.Hard, Rating.Good -> if (lapsed) null else word.rated(rating, now)
        }
        return Outcome(toPersist, state())
    }
}
