package com.example.android.memoization.domain.session

import com.example.android.memoization.data.database.MIGRATION_ENDING_DRAINED_FOR_TEST
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionEndingTest {

    @Test
    fun `the code the migration backfills is DRAINED`() {
        assertEquals(SessionEnding.DRAINED.code, MIGRATION_ENDING_DRAINED_FOR_TEST)
    }

    @Test
    fun `every ending round-trips through its code`() {
        SessionEnding.entries.forEach { assertEquals(it, SessionEnding.fromCode(it.code)) }
    }
}
