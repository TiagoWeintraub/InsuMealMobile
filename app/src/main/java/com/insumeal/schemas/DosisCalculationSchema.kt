package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.DosisCalculation

data class DosisCalculationSchema(
    @SerializedName("meal_plate_id") val mealPlateId: Int,
    @SerializedName("glycemia") val glycemia: Double,
    @SerializedName("total_carbs") val totalCarbs: Double,
    @SerializedName("correction_insulin") val correctionInsulin: Double,
    @SerializedName("carb_insulin") val carbInsulin: Double,
    @SerializedName("total_dose") val totalDose: Double
)

// Función de extensión para convertir el schema a modelo
fun DosisCalculationSchema.toModel(): DosisCalculation {
    return DosisCalculation(
        mealPlateId = this.mealPlateId,
        glycemia = this.glycemia,
        totalCarbs = this.totalCarbs,
        correctionInsulin = this.correctionInsulin,
        carbInsulin = this.carbInsulin,
        totalDose = this.totalDose
    )
}
