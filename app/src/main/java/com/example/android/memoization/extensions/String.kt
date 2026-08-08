package com.example.android.memoization.extensions

fun String.checkLength(): String {
    return if (this.length > 35) {
        val newString = this.substring(0, 34) + "..."
        newString
    } else {
        this
    }
}