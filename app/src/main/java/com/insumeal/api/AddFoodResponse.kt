package com.insumeal.api

import com.insumeal.schemas.MealPlateSchema

data class AddFoodResponse(
    val message: String,
    val original_food: String,
    val translated_food: String,
    val estimated_weight: Int,
    val meal_plate_details: MealPlateSchema
)
