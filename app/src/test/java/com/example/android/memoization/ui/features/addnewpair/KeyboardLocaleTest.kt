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

    @Test
    fun `a field's label takes the language alone, in capitals`() {
        assertEquals("SR", languageLabel("sr-Cyrl_RS"))
        assertEquals("ES", languageLabel("es_ES"))
        assertEquals("RU", languageLabel("ru"))
        assertNull(languageLabel(null))
        assertNull(languageLabel("???"))
    }

    @Test
    fun `a language is named in the learner's own language`() {
        val ru = java.util.Locale.forLanguageTag("ru")
        assertEquals("Испанский (Испания)", languageDisplayName("es_ES", "Spanish", ru))
        assertEquals("Сербский (латиница, Сербия)", languageDisplayName("sr-Latn_RS", "Serbian (Latin)", ru))
        assertEquals("Spanish (Spain)", languageDisplayName("es_ES", "Spanish", java.util.Locale.ENGLISH))
    }

    @Test
    fun `a code the system cannot name keeps the api's name`() {
        assertEquals("Klingon", languageDisplayName("???", "Klingon", java.util.Locale.ENGLISH))
        assertEquals("Esperanto?", languageDisplayName(null, "Esperanto?", java.util.Locale.ENGLISH))
    }
}
