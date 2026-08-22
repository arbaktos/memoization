package com.example.android.memoization.ui.features.addnewpair

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardLocaleTest {

    @Test
    fun `an api code becomes a language tag`() {
        assertEquals("es-ES", keyboardLocaleTag("es_ES"))
        assertEquals("en-GB", keyboardLocaleTag("en_GB"))
        assertEquals("sr-Cyrl-RS", keyboardLocaleTag("sr-Cyrl_RS"))
        assertEquals("sr-Latn-RS", keyboardLocaleTag("sr-Latn_RS"))
    }

    @Test
    fun `a bare language or a ready tag passes through`() {
        assertEquals("sr", keyboardLocaleTag("sr"))
        assertEquals("de-DE", keyboardLocaleTag("de-DE"))
        assertEquals("es-ES", keyboardLocaleTag(" es_ES "))
    }

    @Test
    fun `no language means no hint`() {
        assertNull(keyboardLocaleTag(null))
        assertNull(keyboardLocaleTag(""))
        assertNull(keyboardLocaleTag("   "))
        assertNull(keyboardLocaleTag("_"))
        assertNull(keyboardLocaleTag("???"))
    }
}
