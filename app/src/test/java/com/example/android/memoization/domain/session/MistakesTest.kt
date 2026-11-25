package com.example.android.memoization.domain.session

import com.example.android.memoization.domain.session.Mistakes.Verdict
import org.junit.Assert.assertEquals
import org.junit.Test

class MistakesTest {

    private fun verdict(count: Int, expected: Double) = Mistakes(count, reviewed = 30, expected = expected).verdict

    @Test
    fun `a large session is judged within fifteen percent of the expected count`() {
        // 20 expected: the window is 17..23.
        assertEquals(Verdict.FEW, verdict(16, 20.0))
        assertEquals(Verdict.AS_EXPECTED, verdict(17, 20.0))
        assertEquals(Verdict.AS_EXPECTED, verdict(23, 20.0))
        assertEquals(Verdict.MANY, verdict(24, 20.0))
    }

    @Test
    fun `the window is never narrower than one mistake either way`() {
        // 3 expected: fifteen percent is under one, so 2..4.
        assertEquals(Verdict.FEW, verdict(1, 3.0))
        assertEquals(Verdict.AS_EXPECTED, verdict(2, 3.0))
        assertEquals(Verdict.AS_EXPECTED, verdict(4, 3.0))
        assertEquals(Verdict.MANY, verdict(5, 3.0))
    }

    @Test
    fun `a small session always has a whole number inside the window`() {
        // 1.4 expected: 0.4..2.4, so one or two mistakes are as expected.
        assertEquals(Verdict.FEW, verdict(0, 1.4))
        assertEquals(Verdict.AS_EXPECTED, verdict(1, 1.4))
        assertEquals(Verdict.AS_EXPECTED, verdict(2, 1.4))
        assertEquals(Verdict.MANY, verdict(3, 1.4))
        // 0.6 expected: none or one.
        assertEquals(Verdict.AS_EXPECTED, verdict(0, 0.6))
        assertEquals(Verdict.AS_EXPECTED, verdict(1, 0.6))
        assertEquals(Verdict.MANY, verdict(2, 0.6))
    }
}
