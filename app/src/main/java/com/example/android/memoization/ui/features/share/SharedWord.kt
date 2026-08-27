package com.example.android.memoization.ui.features.share

import android.content.Intent

/**
 * A word sent to the app from another one - a selection shared out of a browser, a reader or a
 * chat. The share arrives as plain text and lands in the word field of a new pair, so it is
 * tidied first: a selection usually carries the spaces and quotation marks around the word.
 */
object SharedWord {

    /**
     * Long enough for a phrase worth learning, short enough that a whole shared article does
     * not become a word pair; what is over is cut, and the learner edits the field anyway.
     */
    const val MAX_LENGTH = 200

    private val WHITESPACE = Regex("[\\s\\u00a0\\u200b\\ufeff]+")

    /** Quotation marks a selection is likely to be wrapped in, whatever the language. */
    private const val QUOTES = "\"'«»„“”‘’"

    /** The word to start a new pair with, or null if the share holds no text. */
    fun from(raw: CharSequence?): String? {
        val text = raw?.toString() ?: return null
        val word = text.replace(WHITESPACE, " ").trim().trim { it in QUOTES }.trim()
        return if (word.isEmpty()) null else word.take(MAX_LENGTH)
    }
}

/**
 * The word this intent shares into the app, or null if it is not a text share - a plain launch,
 * or a notification tap.
 */
fun Intent.sharedWord(): String? {
    if (action != Intent.ACTION_SEND) return null
    if (type?.startsWith("text/") != true) return null
    // A selection is shared as a CharSequence; getStringExtra would drop a styled one.
    return SharedWord.from(getCharSequenceExtra(Intent.EXTRA_TEXT))
}
