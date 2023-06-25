package ru.vasilisasycheva.translation.data

sealed class TranslationState {
    class Success<T>(val content: T) : TranslationState()
    class Error(val errorMessage: String?) : TranslationState()
    object Loading : TranslationState()
}