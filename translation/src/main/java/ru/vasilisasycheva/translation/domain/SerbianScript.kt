package ru.vasilisasycheva.translation.domain

/**
 * Serbian is written in two scripts with a one-to-one mapping between them, so text in one
 * can be turned into the other without knowing the language: Latin digraphs lj, nj, dž stand
 * for single Cyrillic letters, everything else is letter for letter. Case is kept; anything
 * outside the alphabet (digits, punctuation, the other script) passes through unchanged.
 */
object SerbianScript {

    private val latinToCyrillic: Map<String, String> = linkedMapOf(
        "lj" to "љ", "nj" to "њ", "dž" to "џ",
        "a" to "а", "b" to "б", "v" to "в", "g" to "г", "d" to "д", "đ" to "ђ", "e" to "е",
        "ž" to "ж", "z" to "з", "i" to "и", "j" to "ј", "k" to "к", "l" to "л", "m" to "м",
        "n" to "н", "o" to "о", "p" to "п", "r" to "р", "s" to "с", "t" to "т", "ć" to "ћ",
        "u" to "у", "f" to "ф", "h" to "х", "c" to "ц", "č" to "ч", "š" to "ш",
    )

    private val cyrillicToLatin: Map<String, String> =
        latinToCyrillic.entries.associate { (latin, cyrillic) -> cyrillic to latin }

    fun latinToCyrillic(text: String): String = convert(text, latinToCyrillic, longest = 2)

    fun cyrillicToLatin(text: String): String = convert(text, cyrillicToLatin, longest = 1)

    private fun convert(text: String, table: Map<String, String>, longest: Int): String {
        val out = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            var matched = false
            for (len in longest downTo 1) {
                if (i + len > text.length) continue
                val piece = text.substring(i, i + len)
                val mapped = table[piece.lowercase()] ?: continue
                val prev = text.getOrNull(i - 1)
                val next = text.getOrNull(i + len)
                out.append(recase(mapped, piece, neighbourUpper = prev?.isUpperCase() == true || next?.isUpperCase() == true))
                i += len
                matched = true
                break
            }
            if (!matched) {
                out.append(text[i])
                i++
            }
        }
        return out.toString()
    }

    /**
     * "Lj" and "LJ" both become "Љ"; a lower-case source stays lower-case. A single "Љ" becomes
     * "LJ" inside a word in capitals and "Lj" at the head of a capitalised one, judged by the
     * letters around it.
     */
    private fun recase(mapped: String, source: String, neighbourUpper: Boolean): String {
        if (!source[0].isUpperCase()) return mapped
        val allUpper = if (source.length > 1) source.all { it.isUpperCase() } else neighbourUpper
        return if (allUpper || mapped.length == 1) mapped.uppercase()
        else mapped.replaceFirstChar { it.uppercase() }
    }
}
