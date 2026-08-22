package ru.vasilisasycheva.translation.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.vasilisasycheva.translation.api.LanguageItem

class ScriptVariantTest {

    private fun item(code: String, name: String) = LanguageItem(
        full_code = code, code_alpha_1 = code.substringBefore('_'), englishName = name,
        codeName = name, flagPath = "", testWordForSyntezis = "", rtl = "false", modes = emptyList(),
    )

    @Test
    fun `the api code of a variant is the language it stands in for`() {
        assertEquals("sr-Cyrl_RS", ScriptVariant.apiCode("sr-Latn_RS"))
        assertEquals("sr-Cyrl_RS", ScriptVariant.apiCode("sr-Cyrl_RS"))
        assertEquals("en_GB", ScriptVariant.apiCode("en_GB"))
        assertNull(ScriptVariant.of("en_GB"))
        assertNull(ScriptVariant.of(null))
    }

    @Test
    fun `the variant is listed right after its api language`() {
        val list = ScriptVariant.withVariants(
            listOf(item("ru_RU", "Russian"), item("sr-Cyrl_RS", "Serbian"), item("sv_SE", "Swedish"))
        )

        assertEquals(listOf("ru_RU", "sr-Cyrl_RS", "sr-Latn_RS", "sv_SE"), list.map { it.full_code })
        val latin = list.first { it.full_code == "sr-Latn_RS" }
        assertEquals("Serbian (Latin)", latin.englishName)
        assertEquals("sr-Latn", latin.code_alpha_1)
    }

    @Test
    fun `a variant the api already lists is not added twice`() {
        val list = ScriptVariant.withVariants(
            listOf(item("sr-Cyrl_RS", "Serbian"), item("sr-Latn_RS", "Serbian Latin"))
        )
        assertEquals(listOf("sr-Cyrl_RS", "sr-Latn_RS"), list.map { it.full_code })
    }

    @Test
    fun `a list without the language gains nothing`() {
        assertEquals(1, ScriptVariant.withVariants(listOf(item("en_GB", "English"))).size)
    }
}
