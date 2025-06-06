package com.insumeal.models

// Modelo de dominio para MealPlate
data class MealPlate(
    val id: Int,
    val name: String,
    val date: String,
    val totalCarbs: Double,
    val dosis: Double,
    val glycemia: Double,
    val ingredients: List<Ingredient> = emptyList()
) {
    override fun toString(): String {
        return "MealPlate(id=$id, name='$name', totalCarbs=$totalCarbs, dosis=$dosis, ingredients=${ingredients.size})"
    }
}