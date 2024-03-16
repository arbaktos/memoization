package ru.vasilisasycheva.translation.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.vasilisasycheva.translation.utils.Constants.API_KEY
import ru.vasilisasycheva.translation.utils.Constants.BASE_URL
import ru.vasilisasycheva.translation.utils.Constants.HEADER_ACCEPT
import ru.vasilisasycheva.translation.utils.Constants.HEADER_ACCEPT_VALUE
import ru.vasilisasycheva.translation.utils.Constants.HEADER_AUTH
import javax.inject.Inject


@Module
@InstallIn(SingletonComponent::class)
class Retrofit @Inject constructor(){
    private val httpLoggingInterceptor = setLoginInterceptor()

    @Provides
    fun setLoginInterceptor(): HttpLoggingInterceptor {
        val httpLoginIntercepror = HttpLoggingInterceptor()
        httpLoginIntercepror.level = HttpLoggingInterceptor.Level.BODY
        return httpLoginIntercepror
    }

    @Provides
    fun okHttpClient() = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(HEADER_AUTH, API_KEY)
                .addHeader(HEADER_ACCEPT, HEADER_ACCEPT_VALUE).build()
            return@addInterceptor chain.proceed(request)
        }
        .build()

    @Provides
    fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val linganexApi: LinganexApi = retrofit().create(LinganexApi::class.java)
}



