package com.insumeal.models

// Modelo de dominio para Ingredient
data class Ingredient (
    val id: Int,
    val name: String,
    val carbsPerHundredGrams: Double,
    val grams: Double,
    val carbs: Double
)