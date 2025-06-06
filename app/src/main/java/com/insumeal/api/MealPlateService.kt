package com.insumeal.api

import com.insumeal.schemas.MealPlateSchema
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MealPlateService {
    @Multipart
    @POST("/gemini/analyze-image")
    suspend fun analyzeImage(@Part file: MultipartBody.Part): Response<MealPlateSchema>
}
