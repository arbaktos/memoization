package com.example.android.memoization.data.api

import com.google.gson.annotations.SerializedName

data class WordTranslationRequest (
    val from: String,
    val to: String,
    @SerializedName("data")
    val toTranslate: String,
    val platform: String = "api",
    val enableTransliteration: Boolean = false
)

data class WordTranslationResponse(
    val err: String?,
    @SerializedName("result")
    val translation: String,
    val sourceTransliteration: String = "",
    val targetTransliteration: String = ""
)

