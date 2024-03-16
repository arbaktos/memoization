package ru.vasilisasycheva.translation.api

import com.google.gson.annotations.SerializedName

data class WordTranslationRequest (
    val from: String,
    val to: String,
    @SerializedName("data")
    val toTranslate: String,
    val platform: String = "api",
    val enableTransliteration: Boolean = false
)

