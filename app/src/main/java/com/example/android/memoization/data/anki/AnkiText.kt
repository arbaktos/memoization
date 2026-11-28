package com.example.android.memoization.data.anki

/**
 * Anki keeps a note's fields as HTML: formatting, line breaks, entities, and media as
 * `<img src="…">` and `[sound:…]`. A word pair holds plain text, so what the learner reads
 * is taken out of it; the HTML itself is kept with the imported note for later.
 */
object AnkiText {

    private val CLOZE = Regex("""\{\{c\d+::""")
    private val SOUND = Regex("""\[sound:([^\]]+)]""")
    private val IMG = Regex("""<img\b[^>]*?\bsrc\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))[^>]*>""", RegexOption.IGNORE_CASE)
    private val HIDDEN_BLOCK = Regex("""<(style|script)\b[^>]*>.*?</\1\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val LINE_BREAK = Regex("""<br\s*/?>|</(div|p|li|tr|h\d)\s*>""", RegexOption.IGNORE_CASE)
    private val TAG = Regex("""<[^>]*>""")
    private val ENTITY = Regex("""&(#x[0-9a-fA-F]+|#\d+|[a-zA-Z]+);""")
    private val SPACES = Regex("""[ \t ]+""")

    private val NAMED = mapOf(
        "nbsp" to " ", "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
        "mdash" to "—", "ndash" to "–", "hellip" to "…", "laquo" to "«", "raquo" to "»",
        "shy" to "", "zwnj" to "", "zwj" to "",
    )

    /** A cloze deletion, `{{c1::…}}`: the note is a sentence with gaps, not a word and its meaning. */
    fun hasCloze(field: String): Boolean = CLOZE.containsMatchIn(field)

    /** The text of a field as the learner would read it, without formatting or media. */
    fun toPlain(html: String): String {
        val text = html
            .replace(HIDDEN_BLOCK, "")
            .replace(SOUND, "")
            .replace(IMG, "")
            .replace(LINE_BREAK, "\n")
            .replace(TAG, "")
        return decodeEntities(text)
            .lines()
            .map { it.replace(SPACES, " ").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    /** The media files a field refers to, by their names in the collection. */
    fun mediaNames(html: String): List<String> =
        SOUND.findAll(html).map { it.groupValues[1] }.toList() +
            IMG.findAll(html).map { m -> m.groupValues.drop(1).first { it.isNotEmpty() } }.toList()

    private fun decodeEntities(text: String): String = ENTITY.replace(text) { match ->
        val body = match.groupValues[1]
        when {
            body.startsWith("#x") || body.startsWith("#X") -> body.drop(2).toIntOrNull(16)?.let(::codePoint)
            body.startsWith("#") -> body.drop(1).toIntOrNull()?.let(::codePoint)
            else -> NAMED[body]
        } ?: match.value
    }

    private fun codePoint(code: Int): String? =
        if (Character.isValidCodePoint(code)) String(Character.toChars(code)) else null
}
