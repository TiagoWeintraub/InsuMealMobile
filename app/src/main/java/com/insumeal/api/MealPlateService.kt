package com.insumeal.api

import com.insumeal.schemas.MealPlateSchema
import com.insumeal.schemas.DosisCalculationSchema
import com.insumeal.schemas.MealPlateHistorySchema
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MealPlateService {
    @Multipart
    @POST("/gemini/analyze-image")
    suspend fun analyzeImage(@Part file: MultipartBody.Part): Response<MealPlateSchema>    @PUT("/meal_plate_ingredient/{meal_plate_id}/{ingredient_id}")
    suspend fun updateIngredientGrams(
        @Path("meal_plate_id") mealPlateId: Int,
        @Path("ingredient_id") ingredientId: Int,
        @Body updateGramsRequest: UpdateGramsRequest
    ): Response<MealPlateSchema>    @POST("/dosis/calculate/{meal_plate_id}")
    suspend fun calculateDosis(
        @Path("meal_plate_id") mealPlateId: Int,
        @Body calculateDosisRequest: CalculateDosisRequest
    ): Response<DosisCalculationSchema>
      @GET("/meal_plate/")
    suspend fun getMealPlateHistory(): Response<List<MealPlateHistorySchema>>
    
    @DELETE("/meal_plate/{id_meal_plate}")
    suspend fun deleteMealPlate(@Path("id_meal_plate") mealPlateId: Int): Response<Unit>
    
    @GET("/meal_plate/image/{id_meal_plate}")
    suspend fun getMealPlateImage(@Path("id_meal_plate") mealPlateId: Int): Response<okhttp3.ResponseBody>
      @GET("/ingredient/meal_plate/{id_meal_plate}")
    suspend fun getMealPlateById(@Path("id_meal_plate") mealPlateId: Int): Response<MealPlateSchema>
      @DELETE("/meal_plate/all")
    suspend fun deleteAllMealPlates(): Response<Unit>
      @DELETE("/meal_plate_ingredient/{meal_plate_id}/{ingredient_id}")
    suspend fun deleteIngredientFromMealPlate(
        @Path("meal_plate_id") mealPlateId: Int,
        @Path("ingredient_id") ingredientId: Int
    ): Response<Unit>

    @POST("/nutrition/add/food/{meal_plate_id}")
    suspend fun addFoodToMealPlate(
        @Path("meal_plate_id") mealPlateId: Int,
        @Body foodRequest: Map<String, String>
    ): Response<AddFoodResponse>
}
