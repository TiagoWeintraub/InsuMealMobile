package com.insumeal.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.insumeal.api.RetrofitClient
import com.insumeal.ui.viewmodel.FoodHistoryMealPlateViewModel
import com.insumeal.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodHistoryMealPlateScreen(
    navController: NavController,
    mealPlateId: Int
) {
    val context = LocalContext.current
    val viewModel = remember { FoodHistoryMealPlateViewModel() }
    
    val mealPlate by viewModel.mealPlate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Estados para controlar la expansión de los desplegables
    var isDosisExpanded by remember { mutableStateOf(true) } // Abierto por defecto
    var isIngredientsExpanded by remember { mutableStateOf(true) } // Abierto por defecto

    // Cargar los detalles del meal plate cuando se inicia la pantalla
    LaunchedEffect(mealPlateId) {
        viewModel.initializeTranslationService()
        viewModel.loadMealPlateDetails(context, mealPlateId)
    }
    
    Scaffold(
        // Sin topBar para que la imagen llegue hasta arriba
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Mostrar indicador de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Cargando información del plato...",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    // Mostrar mensaje de error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.clearError()
                                    viewModel.loadMealPlateDetails(context, mealPlateId)
                                }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                mealPlate != null -> {
                    // Mostrar detalles del plato con el diseño de DosisScreen
                    val currentMealPlate = mealPlate!!
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Imagen del plato que ocupa todo el ancho y llega hasta arriba
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Altura fija para la imagen
                        ) {
                            // Mostrar la imagen del plato
                            val imageRequest = ImageUtils.createAuthenticatedImageRequest(
                                context = context,
                                imageUrl = RetrofitClient.getMealPlateImageUrl(currentMealPlate.id)
                            )

                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = imageRequest,
                                    error = rememberAsyncImagePainter("https://via.placeholder.com/400x300/4CAF50/FFFFFF?text=${currentMealPlate.name}")
                                ),
                                contentDescription = "Imagen de ${currentMealPlate.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Overlay con gradiente para el título
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )

                            // Título del plato sobre la imagen
                            Text(
                                text = currentMealPlate.name.uppercase(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    // 1. PARA HACER EL TEXTO MÁS GRANDE
                                    fontSize = 28.sp,

                                    // 2. PARA AÑADIR LA SOMBRA
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.75f), // Color de la sombra
                                        offset = Offset(x = 4f, y = 4f),     // Desplazamiento (dónde se dibuja)
                                        blurRadius = 8f                      // Qué tan difuminada es la sombra
                                    )
                                ),
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp)
                            )

                            // Botón de volver atrás con la misma estética que UploadPhotoScreen
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                                    .statusBarsPadding(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { navController.navigateUp() },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver atrás",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Card superpuesta que contiene todo el contenido
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 250.dp), // Posicionada para superponerse a la imagen
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            shape = RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = if (isDosisExpanded || isIngredientsExpanded) 0.dp else 24.dp,
                                bottomEnd = if (isDosisExpanded || isIngredientsExpanded) 0.dp else 24.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFFFFF) // Blanco puro
                            )
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Información nutricional detallada
                                item {
                                    // Título de la sección con icono verde
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50), // Verde elegante
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Información",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            ),
                                            color = Color.Black
                                        )
                                    }

                                    // Tres globos circulares con información nutricional
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        CircularInfoBubble(
                                            label = "Carbohidratos",
                                            value = "${String.format("%.1f", currentMealPlate.totalCarbs)} g",
                                            backgroundColor = Color(0xFFFF9800), // Naranja elegante
                                            icon = Icons.Default.Restaurant,
                                            modifier = Modifier.weight(1f)
                                        )

                                        CircularInfoBubble(
                                            label = "Glucemia",
                                            value = "${String.format("%.0f", currentMealPlate.glycemia)} mg/dL",
                                            backgroundColor = Color(0xFFE91E63), // Rosa elegante
                                            icon = Icons.Default.Bloodtype,
                                            modifier = Modifier.weight(1f)
                                        )

                                        CircularInfoBubble(
                                            label = "Dosis Total",
                                            value = "${String.format("%.1f", currentMealPlate.dosis)} U", // Usar dosis total disponible
                                            backgroundColor = Color(0xFF2196F3), // Azul elegante
                                            icon = Icons.Default.MedicalServices,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // Desglose del cálculo (desplegable)
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isDosisExpanded = !isDosisExpanded },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFFFFFF) // Blanco puro
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Calculate,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2196F3),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Desglose del cálculo",
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp // Tamaño más pequeño
                                                        ),
                                                        color = Color.Black                                                )
                                                }

                                                Icon(
                                                    imageVector = if (isDosisExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (isDosisExpanded) "Colapsar" else "Expandir",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }

                                            // Detalles del cálculo (desplegable)
                                            AnimatedVisibility(
                                                visible = isDosisExpanded,
                                                enter = expandVertically(),
                                                exit = shrinkVertically()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 20.dp),
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                        thickness = 1.dp
                                                    )

                                                    // Insulina para corrección
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
                                                                imageVector = Icons.Default.Healing,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Text(
                                                                text = "Insulina para corrección",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = Color.Black,
                                                                maxLines = 2
                                                            )
                                                        }
                                                        Text(
                                                            text = "${String.format("%.1f", 0.0)} U", // Valor por defecto ya que no está disponible
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }

                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                        thickness = 1.dp
                                                    )

                                                    // Insulina para carbohidratos
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
                                                                imageVector = Icons.Default.LocalDining,
                                                                contentDescription = null,
                                                                tint = Color(0xFF2196F3),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            Text(
                                                                text = "Insulina para carbohidratos",
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = Color.Black,
                                                                maxLines = 2
                                                            )
                                                        }
                                                        Text(
                                                            text = "${String.format("%.1f", currentMealPlate.dosis)} U", // Usar dosis total disponible
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = Color(0xFF2196F3)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Desglose de ingredientes (desplegable)
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isIngredientsExpanded = !isIngredientsExpanded },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFFFFFF) // Blanco puro
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocalDining,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF9800),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Ingredientes Detectados",
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp // Tamaño más pequeño
                                                        ),
                                                        color = Color.Black,
                                                    )
                                                }

                                                Icon(
                                                    imageVector = if (isIngredientsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (isIngredientsExpanded) "Colapsar" else "Expandir",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }

                                            AnimatedVisibility(
                                                visible = isIngredientsExpanded,
                                                enter = expandVertically(),
                                                exit = shrinkVertically()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 20.dp),
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    if (currentMealPlate.ingredients.isNotEmpty()) {
                                                        // Línea divisora inicial
                                                        HorizontalDivider(
                                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                            thickness = 1.dp
                                                        )

                                                        currentMealPlate.ingredients.forEachIndexed { index, ingredient ->
                                                            // Información del ingrediente
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
                                                                        imageVector = Icons.Default.Restaurant,
                                                                        contentDescription = null,
                                                                        tint = Color.Gray,
                                                                        modifier = Modifier.size(24.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(12.dp))
                                                                    Column {
                                                                        Text(
                                                                            text = ingredient.name,
                                                                            style = MaterialTheme.typography.titleMedium,
                                                                            color = Color.Black,
                                                                            maxLines = 2
                                                                        )
                                                                        Text(
                                                                            text = "${String.format("%.0f", ingredient.grams)}g",
                                                                            style = MaterialTheme.typography.bodyMedium,
                                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                                        )
                                                                    }
                                                                }
                                                                Text(
                                                                    text = "${String.format("%.1f", ingredient.carbs)}g de HC",
                                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                                        fontWeight = FontWeight.Bold
                                                                    ),
                                                                    color = MaterialTheme.colorScheme.secondary
                                                                )
                                                            }

                                                            // Línea divisora entre ingredientes (excepto después del último)
                                                            if (index < currentMealPlate.ingredients.size - 1) {
                                                                HorizontalDivider(
                                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                                    thickness = 1.dp
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        HorizontalDivider(
                                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                            thickness = 1.dp
                                                        )

                                                        Text(
                                                            text = "No hay información detallada de ingredientes disponible",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Solo botón de Volver al inicio (sin editar)
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    // Navegar al home y limpiar el stack
                                                    navController.navigate("home") {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            inclusive = false
                                                        }
                                                    }
                                                }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Inicio",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // Espaciado adicional al final
                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
