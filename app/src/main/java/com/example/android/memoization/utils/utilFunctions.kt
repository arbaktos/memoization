package com.example.android.memoization.utils

fun longToDays(timeSpace: Long): Int {
    val seconds = timeSpace / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = (hours / 24).toInt()
    return days
}