package com.example.android.memoization.ui.features.addnewpair

import java.util.Locale

/**
 * The language tag to hint the keyboard with for a field, from the stack's language code.
 *
 * The stack stores the translation api's code ("es_ES", "sr-Cyrl_RS"); a keyboard wants a
 * BCP 47 tag ("es-ES", "sr-Cyrl-RS"). Android cannot force a keyboard language, but most
 * keyboards switch to a hinted one on their own if the learner has it enabled, which saves
 * flipping the keyboard by hand between the word and its meaning on every pair.
 *
 * Null when there is no code, or it does not name a language.
 */
fun keyboardLocaleTag(apiCode: String?): String? {
    val tag = apiCode?.trim()?.replace('_', '-')?.takeIf { it.isNotEmpty() } ?: return null
    val locale = Locale.forLanguageTag(tag)
    return locale.toLanguageTag().takeIf { locale.language.isNotEmpty() && it != "und" }
}
