package com.example.android.memoization.ui.features.share

import android.content.Intent

/**
 * A word sent to the app from another one - a selection in a browser, a reader or a chat, handed
 * over through the share sheet or straight from the selection menu. It arrives as plain text and
 * lands in the word field of a new pair, so it is tidied first: a selection usually carries the
 * spaces and quotation marks around the word.
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
 * The word this intent hands to the app, or null if it hands over no text - a plain launch, or
 * a notification tap. Both ways in are the same word: the share sheet sends the selection as
 * ACTION_SEND, the selection menu sends it as ACTION_PROCESS_TEXT.
 *
 * A selection travels as a CharSequence, so it is read as one; getStringExtra returns null for
 * a styled selection, which is most of what a web page gives.
 */
fun Intent.sharedWord(): String? = when (action) {
    Intent.ACTION_SEND ->
        if (type?.startsWith("text/") == true) SharedWord.from(getCharSequenceExtra(Intent.EXTRA_TEXT))
        else null
    // The read-only flag is not asked for: the word is copied into a pair, never edited back.
    Intent.ACTION_PROCESS_TEXT -> SharedWord.from(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT))
    else -> null
}
