package com.insumeal.models

// Modelo para el historial de meal plates
data class MealPlateHistory(
    val id: Int,
    val date: String,
    val type: String,
    val totalCarbs: Double,
    val glycemia: Double,
    val dosis: Double,
    val imageUrl: String
)
