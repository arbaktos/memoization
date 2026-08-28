package com.example.android.memoization.ui.features.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StackShortcutIdTest {

    @Test
    fun `a stack is found again by the id its share target was published under`() {
        assertEquals(7L, StackShortcutId.stackIdOf(StackShortcutId.of(7)))
    }

    @Test
    fun `an id from somewhere else names no stack`() {
        assertNull(StackShortcutId.stackIdOf(null))
        assertNull(StackShortcutId.stackIdOf(""))
        assertNull(StackShortcutId.stackIdOf("7"))
        assertNull(StackShortcutId.stackIdOf("contact-7"))
        assertNull(StackShortcutId.stackIdOf("stack-"))
        assertNull(StackShortcutId.stackIdOf("stack-seven"))
    }
}
