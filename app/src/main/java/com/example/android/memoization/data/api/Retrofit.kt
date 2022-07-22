package com.example.android.memoization.data.api

import android.content.Context
import android.content.SharedPreferences
import com.example.android.memoization.R
import com.example.android.memoization.utils.API_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class Retrofit @Inject constructor(){
    val httpLoginIntercepror = setLoginInterceptor()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    @Provides
    fun setLoginInterceptor(): HttpLoggingInterceptor {
        val httpLoginIntercepror = HttpLoggingInterceptor()
        httpLoginIntercepror.level = HttpLoggingInterceptor.Level.BODY
        return httpLoginIntercepror
    }

    @Provides
    fun okHttpClient() = OkHttpClient.Builder()
        .addInterceptor(httpLoginIntercepror)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", API_KEY)
                .addHeader("accept","application/json").build()
            return@addInterceptor chain.proceed(request)
        }
        .build()

    @Provides
    fun retrofit() = Retrofit.Builder()
        .baseUrl("https://api-b2b.backenster.com/")
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val linganexApi = retrofit().create(LinganexApi::class.java)
}



