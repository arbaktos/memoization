package ru.vasilisasycheva.translation.domain

import ru.vasilisasycheva.translation.api.LanguageItem

/**
 * A language the api serves in one script only, offered to the learner in another as well.
 *
 * Serbian is the case: Lingvanex knows sr-Cyrl_RS alone, while most learners type Serbian in
 * Latin - and a keyboard hinted "sr-Cyrl" brings up the Cyrillic layout. The variant keeps a
 * code of its own, so the stack and the keyboard see the script the learner uses; only the api
 * call is mapped, with the text transliterated on the way there and back.
 */
enum class ScriptVariant(
    val code: String,
    val apiCode: String,
    val englishName: String,
    val codeName: String,
    /** Text typed in the variant's script, as the api wants it. */
    val toApi: (String) -> String,
    /** What the api answered, in the variant's script. */
    val fromApi: (String) -> String,
) {
    SERBIAN_LATIN(
        code = "sr-Latn_RS",
        apiCode = "sr-Cyrl_RS",
        englishName = "Serbian (Latin)",
        codeName = "Serbian Latin",
        toApi = SerbianScript::latinToCyrillic,
        fromApi = SerbianScript::cyrillicToLatin,
    );

    companion object {
        fun of(code: String?): ScriptVariant? = entries.firstOrNull { it.code == code }

        /** The code the api understands for [code]; a code that is no variant is its own. */
        fun apiCode(code: String): String = of(code)?.apiCode ?: code

        /** The api's list with every variant inserted right after the language it stands in for. */
        fun withVariants(languages: List<LanguageItem>): List<LanguageItem> {
            val known = languages.map { it.full_code }.toSet()
            return languages.flatMap { item ->
                listOf(item) + entries
                    .filter { it.apiCode == item.full_code && it.code !in known }
                    .map { variant ->
                        item.copy(
                            full_code = variant.code,
                            code_alpha_1 = variant.code.substringBefore('_'),
                            englishName = variant.englishName,
                            codeName = variant.codeName,
                        )
                    }
            }
        }
    }
}
