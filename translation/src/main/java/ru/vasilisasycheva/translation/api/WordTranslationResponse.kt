package ru.vasilisasycheva.translation.api

import com.google.gson.annotations.SerializedName

data class WordTranslationResponse(
    val err: String?,
    @SerializedName("result")
    val translation: String,
    val sourceTransliteration: String = "",
    val targetTransliteration: String = ""
)