package com.example.android.memoization.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StaminaTest {

    @Test
    fun `a stack never stopped with offers at the default`() {
        val fresh = Stamina()
        assertNull(fresh.average)
        assertEquals(Stamina.DEFAULT_OFFER, fresh.offerFrom)
    }

    @Test
    fun `the offer comes at three quarters of the mean stop`() {
        val s = Stamina().stoppedAt(40)
        assertEquals(40.0, s.average!!, 0.0)
        assertEquals(30, s.offerFrom)
    }

    @Test
    fun `the mean is exact whatever the history, from two counters`() {
        val s = Stamina().stoppedAt(30).stoppedAt(35).stoppedAt(46)
        assertEquals(3, s.tiredSessions)
        assertEquals(111, s.tiredAnswersSum)
        assertEquals(37.0, s.average!!, 0.0)
        // 27.75 rounds to 28
        assertEquals(28, s.offerFrom)
    }

    @Test
    fun `stopping as soon as offered pulls the mean down, so it drifts both ways`() {
        var s = Stamina().stoppedAt(40)
        val before = s.average!!
        s = s.stoppedAt(s.offerFrom)
        assertTrue(s.average!! < before)
        assertEquals(35.0, s.average!!, 0.0)
    }

    @Test
    fun `the offer never comes earlier than the floor`() {
        val s = Stamina().stoppedAt(4)
        assertEquals(Stamina.MIN_OFFER, s.offerFrom)
    }
}
