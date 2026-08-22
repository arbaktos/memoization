package ru.vasilisasycheva.translation.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SerbianScriptTest {

    @Test
    fun `latin becomes cyrillic letter for letter`() {
        assertEquals("новинар", SerbianScript.latinToCyrillic("novinar"))
        assertEquals("чекић", SerbianScript.latinToCyrillic("čekić"))
        assertEquals("ђак", SerbianScript.latinToCyrillic("đak"))
        assertEquals("шума", SerbianScript.latinToCyrillic("šuma"))
    }

    @Test
    fun `latin digraphs become one cyrillic letter`() {
        assertEquals("љубав", SerbianScript.latinToCyrillic("ljubav"))
        assertEquals("њен", SerbianScript.latinToCyrillic("njen"))
        assertEquals("џеп", SerbianScript.latinToCyrillic("džep"))
    }

    @Test
    fun `case is kept, including digraphs`() {
        assertEquals("Новинар", SerbianScript.latinToCyrillic("Novinar"))
        assertEquals("НОВИНАР", SerbianScript.latinToCyrillic("NOVINAR"))
        assertEquals("Љубав", SerbianScript.latinToCyrillic("Ljubav"))
        assertEquals("ЉУБАВ", SerbianScript.latinToCyrillic("LJUBAV"))
        assertEquals("Ljubav", SerbianScript.cyrillicToLatin("Љубав"))
        assertEquals("LJUBAV", SerbianScript.cyrillicToLatin("ЉУБАВ"))
    }

    @Test
    fun `anything outside the alphabet passes through`() {
        assertEquals("може боље?", SerbianScript.latinToCyrillic("može bolje?"))
        assertEquals("ја сам / зовем се + име", SerbianScript.latinToCyrillic("ja sam / zovem se + ime"))
        assertEquals("123 новинар", SerbianScript.latinToCyrillic("123 новинар"))
        assertEquals("", SerbianScript.latinToCyrillic(""))
    }

    @Test
    fun `cyrillic becomes latin and round-trips`() {
        assertEquals("novinar", SerbianScript.cyrillicToLatin("новинар"))
        assertEquals("ljubav", SerbianScript.cyrillicToLatin("љубав"))
        assertEquals("džep", SerbianScript.cyrillicToLatin("џеп"))
        for (word in listOf("novinar", "ljubav", "čekić", "Đorđe", "Nježno", "pitanja")) {
            assertEquals(word, SerbianScript.cyrillicToLatin(SerbianScript.latinToCyrillic(word)))
        }
    }
}
