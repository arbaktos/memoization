package com.example.android.memoization.utils

sealed class LoadingState<out T> {
    object Loading : LoadingState<Nothing>()
    object Error : LoadingState<Nothing>()
    class Collected<T>(val content: T) : LoadingState<T>()
}