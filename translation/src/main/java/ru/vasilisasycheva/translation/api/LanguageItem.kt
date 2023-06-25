package ru.vasilisasycheva.translation.api


data class LanguageItem(
    val full_code: String,
    val code_alpha_1: String,
    val englishName: String,
    val codeName: String,
    val flagPath: String,
    val testWordForSyntezis: String,
    val rtl: String,
    val modes: List<LanguageMode>
)