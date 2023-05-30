package com.example.android.memoization.data.api

import retrofit2.Response
import retrofit2.http.*


interface LinganexApi {
    @Headers("Content-Type: application/json")
    @POST("/b1/api/v3/translate") // b1/api/v3/translate
    suspend fun getTranslation(@Body request: WordTranslationRequest): Response<WordTranslationResponse>

    @GET("/b1/api/v3/getLanguages")
    suspend fun getLanguages(
        @Query("platform") platform: String = "api",
        @Query("code") code: String = "en_GB"
    ): Response<ApiLanguage>
}