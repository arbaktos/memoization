package ru.vasilisasycheva.translation.utils

import ru.vasilisasycheva.translation.BuildConfig

object Constants {
    /** Supplied at build time from LINGVANEX_API_KEY in local.properties; never committed. */
    const val API_KEY = BuildConfig.LINGVANEX_API_KEY
    const val HEADER_AUTH = "Authorization"
    const val HEADER_ACCEPT = "accept"
    const val HEADER_ACCEPT_VALUE = "application/json"
    const val BASE_URL = "https://api-b2b.backenster.com/"
}
