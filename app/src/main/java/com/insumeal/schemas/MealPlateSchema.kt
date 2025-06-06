package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.Ingredient
import com.insumeal.models.MealPlate

// Este schema representa la estructura de la respuesta del endpoint /gemini/analyze-image
data class MealPlateSchema(
    @SerializedName("meal_plate_id") val id: Int,
    @SerializedName("meal_plate_name") val name: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("totalCarbs") val totalCarbs: Double,
    @SerializedName("dosis") val dosis: Double,
    @SerializedName("glycemia") val glycemia: Double,
    @SerializedName("ingredients") val ingredients: List<IngredientSchema>? = null
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
    try {
        android.util.Log.d("MealPlateSchema", "Convirtiendo MealPlateSchema a MealPlate: id=${this.id}, name=${this.name}")
        
        // Manejar ingredientes nulos con una lista vacía por defecto
        val ingredientsList = this.ingredients ?: emptyList()
        android.util.Log.d("MealPlateSchema", "Procesando ${ingredientsList.size} ingredientes")
        
        val ingredientModels = ingredientsList.mapIndexed { index, ingredientSchema ->
            try {
                val ingredient = ingredientSchema.toModel()
                android.util.Log.d("MealPlateSchema", "Ingrediente $index convertido: ${ingredient.name}")
                ingredient
            } catch (e: Exception) {
                android.util.Log.e("MealPlateSchema", "Error convirtiendo ingrediente $index: ${e.message}", e)
                throw e
            }
        }
        
        val mealPlate = MealPlate(
            id = this.id,
            name = this.name ?: "Plato sin nombre", // Valor por defecto si name es null
            date = this.date ?: "", // Valor por defecto si date es null
            totalCarbs = this.totalCarbs,
            dosis = this.dosis,
            glycemia = this.glycemia,
            ingredients = ingredientModels
        )
        
        android.util.Log.d("MealPlateSchema", "MealPlate creado con éxito: ${mealPlate.name}, con ${mealPlate.ingredients.size} ingredientes")
        return mealPlate
    } catch (e: Exception) {
        android.util.Log.e("MealPlateSchema", "Error al convertir MealPlateSchema a MealPlate: ${e.message}", e)
        throw e
    }
}

// Función de extensión para convertir el schema de ingrediente a modelo
fun IngredientSchema.toModel(): Ingredient {
    try {
        android.util.Log.d("IngredientSchema", "Convirtiendo IngredientSchema a Ingredient: id=${this.id}, name=${this.name}")
        
        val ingredient = Ingredient(
            id = this.id,
            name = this.name,
            carbsPerHundredGrams = this.carbsPerHundredGrams,
            grams = this.grams,
            carbs = this.carbs
        )
        
        android.util.Log.d("IngredientSchema", "Ingrediente creado con éxito: ${ingredient.name}")
        return ingredient
    } catch (e: Exception) {
        android.util.Log.e("IngredientSchema", "Error al convertir IngredientSchema a Ingredient: ${e.message}", e)
        throw e
    }
}
