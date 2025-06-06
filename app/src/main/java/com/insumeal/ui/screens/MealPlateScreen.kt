package com.insumeal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.insumeal.models.Ingredient
import com.insumeal.ui.viewmodel.MealPlateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlateScreen(
    navController: NavController,
    mealPlateViewModel: MealPlateViewModel
) {
    // Usar collectAsState para observar los StateFlow del ViewModel
    val mealPlate by mealPlateViewModel.mealPlate.collectAsState()
    val isLoading by mealPlateViewModel.isLoading.collectAsState()
    val errorMessage by mealPlateViewModel.errorMessage.collectAsState()
    val hasAttemptedLoad by mealPlateViewModel.hasAttemptedLoad.collectAsState()
    
    // Efecto para sincronizar los estados y hacer logs
    LaunchedEffect(mealPlate) {
        android.util.Log.d("MealPlateScreen", "ViewModel mealPlate cambió: ${mealPlate?.name}")
    }
      // Log inicial para verificar el estado al entrar a la pantalla
    LaunchedEffect(Unit) {
        android.util.Log.d("MealPlateScreen", "Entrando a MealPlateScreen - Estado inicial:")
        android.util.Log.d("MealPlateScreen", "mealPlate=${mealPlate?.name}, isLoading=${isLoading}, hasAttemptedLoad=${hasAttemptedLoad}")
        android.util.Log.d("MealPlateScreen", "ViewModel instance: $mealPlateViewModel")
    }
    // Logs para debugging - observar cambios en el estado
    LaunchedEffect(mealPlate) {
        android.util.Log.d("MealPlateScreen", "mealPlate cambió: ${mealPlate?.name}")
    }
    
    LaunchedEffect(isLoading) {
        android.util.Log.d("MealPlateScreen", "isLoading cambió: ${isLoading}")
    }
      LaunchedEffect(hasAttemptedLoad) {
        android.util.Log.d("MealPlateScreen", "hasAttemptedLoad cambió: ${hasAttemptedLoad}")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del plato") },
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
            )        }    ) { paddingValues ->
        // Las variables ya están definidas usando collectAsState arriba
        
        if (mealPlate == null) {
            // Mostrar pantalla de carga o mensaje si no hay datos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Cargando información del plato...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else if (!hasAttemptedLoad) {
                        // No se ha intentado cargar aún
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Preparando análisis...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )                    } else if (errorMessage != null) {
                        // Hay un error específico
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Volver e intentar de nuevo")
                        }
                    } else {
                        // Estado desconocido - no hay datos pero tampoco hay error
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se pudieron cargar los datos del plato",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Volver e intentar de nuevo")
                        }
                    }
                }
            }        } else {
            // Mostrar detalles del plato cuando tenemos datos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {                // Título del plato
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = mealPlate!!.name.uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Ingredientes detectados:",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Lista de ingredientes simplificada
                items(mealPlate!!.ingredients) { ingredient ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = "${String.format("%.0f", ingredient.grams)}g",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Información nutricional resumida
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Información nutricional:",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoItem(
                                    icon = Icons.Default.Restaurant,
                                    label = "Carbohidratos",
                                    value = "${String.format("%.1f", mealPlate!!.totalCarbs)} g"
                                )
                                
                                InfoItem(
                                    icon = Icons.Default.Favorite,
                                    label = "Dosis",
                                    value = "${String.format("%.1f", mealPlate!!.dosis)} U"
                                )
                                
                                InfoItem(
                                    icon = Icons.Default.Timeline,
                                    label = "Glucemia",
                                    value = "${String.format("%.0f", mealPlate!!.glycemia)} mg/dl"
                                )
                            }
                        }
                    }
                }
                
                // Espacio al final
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}