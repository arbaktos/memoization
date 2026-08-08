package ru.vasilisasycheva.translation.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response
import ru.vasilisasycheva.translation.api.LanguageItem
import ru.vasilisasycheva.translation.api.Retrofit
import ru.vasilisasycheva.translation.api.WordTranslationRequest
import ru.vasilisasycheva.translation.data.TranslationState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * These used to launch a coroutine and return the result variable straight away, so every
 * call handed back Loading and the real response was assigned into a value nobody read.
 * They are suspend functions now: the caller waits for the answer.
 */
@Singleton
class TranslationRepo @Inject constructor(private val retrofit: Retrofit) {

    private val languagesLock = Mutex()
    private var cachedLanguages: List<LanguageItem>? = null

    suspend fun getTranslation(
        fromLanguage: String,
        toLang: String,
        word: String
    ): TranslationState = withContext(Dispatchers.IO) {
        request {
            retrofit.linganexApi.getTranslation(
                WordTranslationRequest(fromLanguage, toLang, word)
            ).map { it.translation }
        }
    }

    /** The list is a 117 entry download that never changes mid-session, so fetch it once. */
    suspend fun getLanguages(): TranslationState = withContext(Dispatchers.IO) {
        cachedLanguages?.let { return@withContext TranslationState.Success(it) }
        languagesLock.withLock {
            cachedLanguages?.let { return@withLock TranslationState.Success(it) }
            val state = request { retrofit.linganexApi.getLanguages().map { it.result } }
            if (state is TranslationState.Success<*>) {
                @Suppress("UNCHECKED_CAST")
                cachedLanguages = state.content as List<LanguageItem>
            }
            state
        }
    }

    private inline fun <T> Response<T>.map(body: (T) -> Any): TranslationState {
        val payload = this.body()
        return when {
            !isSuccessful || payload == null ->
                TranslationState.Error(errorBody()?.string() ?: "HTTP ${code()}")

            else -> TranslationState.Success(body(payload))
        }
    }

    private inline fun request(call: () -> TranslationState): TranslationState = try {
        call()
    } catch (e: Exception) {
        TranslationState.Error(e.message ?: "Could not reach the translation service")
    }
}
