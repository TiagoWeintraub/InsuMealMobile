package com.insumeal.schemas

import com.google.gson.annotations.SerializedName
import com.insumeal.models.MealPlateHistory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Esquema para la respuesta del endpoint /meal_plate/
data class MealPlateHistorySchema(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("type") val type: String,
    @SerializedName("totalCarbs") val totalCarbs: Double,
    @SerializedName("glycemia") val glycemia: Double,
    @SerializedName("dosis") val dosis: Double,
    @SerializedName("image_url") val imageUrl: String
)

// Función de extensión para convertir el schema a modelo
fun MealPlateHistorySchema.toModel(): MealPlateHistory {
    return MealPlateHistory(
        id = this.id,
        date = convertToUtcMinus3(this.date),
        type = this.type,
        totalCarbs = this.totalCarbs,
        glycemia = this.glycemia,
        dosis = this.dosis,
        imageUrl = this.imageUrl
    )
}

private fun convertToUtcMinus3(originalDate: String): String {
    try {
        // Formatos posibles que podrían venir del backend
        val inputPatterns = listOf(
            "dd/MM/yyyy - HH:mm:ss",
            "dd/MM/yyyy - HH:mm"
        )
        
        for (pattern in inputPatterns) {
            try {
                val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Asumir que viene en UTC
                val date = inputFormat.parse(originalDate)
                
                if (date != null) {
                    // Formatear a UTC-3 manteniendo el patrón original
                    val outputFormat = SimpleDateFormat(pattern, Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getTimeZone("GMT-03:00")
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // Probar siguiente formato
            }
        }
    } catch (e: Exception) {
        // Si falla todo, devolver original
    }
    return originalDate
}
