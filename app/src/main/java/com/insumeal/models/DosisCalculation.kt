package com.insumeal.models

data class DosisCalculation(
    val mealPlateId: Int,
    val glycemia: Double,
    val totalCarbs: Double,
    val correctionInsulin: Double,
    val carbInsulin: Double,
    val totalDose: Double
)
