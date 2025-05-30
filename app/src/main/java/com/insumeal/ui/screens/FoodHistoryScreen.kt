package com.insumeal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class FoodHistoryItem(
    val nombrePlato: String,
    val fecha: Date,
    val glucemia: Int,
    val dosisInsulina: Double
)

@Composable
fun FoodHistoryScreen() {
    // Datos de ejemplo
    val historial = listOf(
        FoodHistoryItem("Arroz con pollo", Date(), 110, 4.0),
        FoodHistoryItem("Ensalada César", Date(System.currentTimeMillis() - 86400000), 95, 2.5),
        FoodHistoryItem("Pizza", Date(System.currentTimeMillis() - 2 * 86400000), 130, 6.0)
    )
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(historial) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = item.nombrePlato, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Fecha: ${dateFormat.format(item.fecha)}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Glucemia: ${item.glucemia} mg/dL", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Dosis insulina: ${item.dosisInsulina} U", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

