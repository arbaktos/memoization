package com.example.android.memoization.data.api

import ru.vasilisasycheva.translation.api.LanguageItem

data class ApiLanguage(
    val err: String?,
    val result: List<LanguageItem>
)


