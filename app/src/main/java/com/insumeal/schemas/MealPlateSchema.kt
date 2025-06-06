package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.Ingredient
import com.insumeal.models.MealPlate

// Este schema representa la estructura de la respuesta del endpoint /gemini/analyze-image
data class MealPlateSchema(
    @SerializedName("meal_plate_id") val id: Int,
    @SerializedName("meal_plate_name") val name: String,
    @SerializedName("date") val date: String,
    @SerializedName("totalCarbs") val totalCarbs: Double,
    @SerializedName("dosis") val dosis: Double,
    @SerializedName("glycemia") val glycemia: Double,
    @SerializedName("ingredients") val ingredients: List<IngredientSchema> = emptyList()
)

data class IngredientSchema(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("carbsPerHundredGrams") val carbsPerHundredGrams: Double,
    @SerializedName("grams") val grams: Double,
    @SerializedName("carbs") val carbs: Double
)

// Función de extensión para convertir el schema a modelo
fun MealPlateSchema.toModel(): MealPlate {
    return MealPlate(
        id = this.id,
        name = this.name,
        date = this.date,
        totalCarbs = this.totalCarbs,
        dosis = this.dosis,
        glycemia = this.glycemia,
        ingredients = this.ingredients.map { it.toModel() }
    )
}

// Función de extensión para convertir el schema de ingrediente a modelo
fun IngredientSchema.toModel(): Ingredient {
    return Ingredient(
        id = this.id,
        name = this.name,
        carbsPerHundredGrams = this.carbsPerHundredGrams,
        grams = this.grams,
        carbs = this.carbs
    )
}
