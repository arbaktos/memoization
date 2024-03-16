package ru.vasilisasycheva.translation.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.api.Retrofit
import ru.vasilisasycheva.translation.api.WordTranslationRequest
import ru.vasilisasycheva.translation.data.TranslationState
import javax.inject.Inject

class TranslationRepo @Inject constructor(private val retrofit: Retrofit) {

    private val scope = CoroutineScope(Job())

    fun getTranslation(fromLanguage: String, toLang: String, word: String): TranslationState {
        val request = WordTranslationRequest(fromLanguage, toLang, word)
        var result: TranslationState = TranslationState.Loading
        try {
            scope.launch(Dispatchers.IO) {
                val response = retrofit.linganexApi.getTranslation(request)
                result = when (response.code()) {
                    in 100..300 -> {
                        TranslationState.Success(response.body()!!.translation)
                    }

                    in 300..599 -> {
                        TranslationState.Error(response.errorBody().toString())
                    }

                    else -> TranslationState.Error(response.body()?.err)
                }
            }

        } catch (e: Exception) {
            result = TranslationState.Error("Error accessing the Internet")
        }
        return result
    }

    fun getLanguages(): TranslationState {
        var result: TranslationState = TranslationState.Loading
        scope.launch {
            val response = retrofit.linganexApi.getLanguages()
            val code = response.code()
            result = when (code) {
                in 100..299 -> {
                    TranslationState.Success(response.body()!!.result)
                }

                in 300..599 -> {
                    TranslationState.Error(response.errorBody().toString())
                }

                else -> TranslationState.Error(response.body()?.err)
            }
        }
        return result
    }
}


