package com.insumeal.api

import com.insumeal.schemas.MealPlateSchema
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MealPlateService {
    @Multipart
    @POST("/gemini/analyze-image")
    suspend fun analyzeImage(@Part file: MultipartBody.Part): Response<MealPlateSchema>
      @PUT("/meal_plate_ingredient/{meal_plate_id}/{ingredient_id}")
    suspend fun updateIngredientGrams(
        @Path("meal_plate_id") mealPlateId: Int,
        @Path("ingredient_id") ingredientId: Int,
        @Body updateGramsRequest: UpdateGramsRequest
    ): Response<MealPlateSchema>
}
