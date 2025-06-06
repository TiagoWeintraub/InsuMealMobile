package com.insumeal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.insumeal.ui.viewmodel.MealPlateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosisScreen(
    navController: NavController,
    mealPlateViewModel: MealPlateViewModel
) {
    // Observar los StateFlow del ViewModel
    val mealPlate by mealPlateViewModel.mealPlate.collectAsState()
    val dosisCalculation by mealPlateViewModel.dosisCalculation.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cálculo de Dosis") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Título de la pantalla
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                      Text(
                        text = "Resultado del Cálculo",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    if (mealPlate != null) {
                        Text(
                            text = mealPlate!!.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
              // Información nutricional detallada
            if (mealPlate != null && dosisCalculation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Glucemia actual
                        InfoRowItem(
                            icon = Icons.Default.Bloodtype,
                            label = "Glucemia actual",
                            value = "${String.format("%.0f", dosisCalculation!!.glycemia)} mg/dL",
                            iconColor = MaterialTheme.colorScheme.tertiary
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        
                        // Carbohidratos totales
                        InfoRowItem(
                            icon = Icons.Default.Restaurant,
                            label = "Carbohidratos totales",
                            value = "${String.format("%.1f", dosisCalculation!!.totalCarbs)} g",
                            iconColor = MaterialTheme.colorScheme.secondary
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        
                        // Insulina para corrección
                        InfoRowItem(
                            icon = Icons.Default.Healing,
                            label = "Insulina para corrección",
                            value = "${String.format("%.1f", dosisCalculation!!.correctionInsulin)} U",
                            iconColor = MaterialTheme.colorScheme.error
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        
                        // Insulina para carbohidratos
                        InfoRowItem(
                            icon = Icons.Default.LocalDining,
                            label = "Insulina para carbohidratos",
                            value = "${String.format("%.1f", dosisCalculation!!.carbInsulin)} U",
                            iconColor = MaterialTheme.colorScheme.secondary
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        
                        // Dosis total recomendada (destacada)
                        InfoRowItem(
                            icon = Icons.Default.Medication,
                            label = "DOSIS TOTAL RECOMENDADA",
                            value = "${String.format("%.1f", dosisCalculation!!.totalDose)} U",
                            iconColor = MaterialTheme.colorScheme.primary,
                            isHighlighted = true
                        )
                    }
                }
                
                // Tarjeta de advertencia/información
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Esta información es orientativa. Consulta siempre con tu médico antes de aplicar cualquier dosis de insulina.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Botón para volver
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Volver al Plato")
                }            } else {
                // Estado cuando no hay datos del cálculo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Sin datos",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay información del cálculo de dosis disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun InfoRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
