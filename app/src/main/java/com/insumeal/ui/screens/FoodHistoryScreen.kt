package com.insumeal.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.insumeal.ui.theme.Turquoise300
import com.insumeal.ui.theme.Turquoise500
import com.insumeal.ui.theme.Turquoise600
import com.insumeal.ui.viewmodel.MealPlateHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodHistoryScreen(
    navController: NavController,
    viewModel: MealPlateHistoryViewModel? = null
) {
    val context = LocalContext.current
    val historyViewModel = remember { MealPlateHistoryViewModel() }

    val historyList by historyViewModel.historyList.collectAsState()
    val isLoading by historyViewModel.isLoading.collectAsState()
    val errorMessage by historyViewModel.errorMessage.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var isDeletingAll by remember { mutableStateOf(false) }
    var deleteAllError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        historyViewModel.loadHistory(context)
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header moderno con gradiente (igual que HomeScreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                // Forma geométrica ondulada de fondo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(
                            androidx.compose.foundation.shape.GenericShape { size, _ ->
                                val width = size.width
                                val height = size.height

                                // Crear una forma con ondas suaves en la parte inferior
                                moveTo(0f, 0f)
                                lineTo(width, 0f)
                                lineTo(width, height * 0.75f)

                                // Crear ondas más pronunciadas y elegantes
                                cubicTo(
                                    width * 0.85f, height * 0.95f,
                                    width * 0.65f, height * 0.95f,
                                    width * 0.5f, height * 0.85f
                                )
                                cubicTo(
                                    width * 0.35f, height * 0.75f,
                                    width * 0.15f, height * 0.75f,
                                    0f, height * 0.85f
                                )
                                close()
                            }
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Turquoise500,
                                    Turquoise600
                                )
                            )
                        )
                )

                Column {
                    // Top bar integrado en el header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de volver atrás con el mismo estilo que HomeScreen
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Título centrado a la misma altura que el botón
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .offset(x = (-22).dp), // Compensar el ancho del botón para centrar realmente
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Historial de Comidas",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Contenido principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                when {
                    isLoading -> {
                        // Estado de carga moderno
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                // Círculo de carga con colores del tema
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp),
                                    color = Turquoise500,
                                    strokeWidth = 6.dp
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = "Cargando historial",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = Color(0xFF2D3748),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Obteniendo tus análisis anteriores...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF4A5568),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        // Estado de error moderno
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    // Icono de error
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Turquoise600.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Error,
                                            contentDescription = null,
                                            tint = Turquoise600,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "Oops! Algo salió mal",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF2D3748),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = errorMessage!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Turquoise600,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            historyViewModel.clearError()
                                            historyViewModel.loadHistory(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Turquoise500
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Reintentar",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    !isLoading && historyList.isEmpty() -> {
                        // Estado vacío moderno
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    // Icono de historial vacío
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(Color.White,Color.White.copy(alpha = 0.1f) )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = Turquoise300,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "¡Aún no tienes análisis!",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = Color(0xFF2D3748),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Comienza analizando tu primera comida para ver los resultados aquí",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp
                                        ),
                                        color = Color(0xFF4A5568),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = { navController.navigate("uploadPhoto") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Turquoise500
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PhotoCamera,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Analizar Primera Comida",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Lista del historial con diseño moderno
                        Column {
                            // Estadísticas rápidas
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCard(
                                    label = "Total",
                                    value = "${historyList.size}",
                                    icon = Icons.Filled.RestaurantMenu,
                                    backgroundColor = Color(0xFFF7FAFF),
                                    iconColor = Color(0xFF4299E1),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(historyList) { historyItem ->
                                    ModernMealPlateHistoryCard(
                                        historyItem = historyItem,
                                        onViewDetails = { mealPlateId ->
                                            navController.navigate("foodHistoryMealPlate/$mealPlateId")
                                        },
                                        onDelete = { mealPlateId ->
                                            historyViewModel.deleteMealPlate(
                                                context = context,
                                                mealPlateId = mealPlateId,
                                                onSuccess = {},
                                                onError = { error -> }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar todo el historial
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text("Eliminar todo el historial")
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar todo el historial de comidas? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        isDeletingAll = true
                        deleteAllError = null
                        
                        historyViewModel.deleteAllMealPlates(
                            context = context,
                            onSuccess = {
                                isDeletingAll = false
                            },
                            onError = { error ->
                                isDeletingAll = false
                                deleteAllError = error
                            }
                        )
                    }
                ) {
                    Text("Eliminar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de error para eliminar todo
    deleteAllError?.let { error ->
        AlertDialog(
            onDismissRequest = { deleteAllError = null },
            title = {
                Text("Error al eliminar historial")
            },
            text = {
                Text(error)
            },
            confirmButton = {
                TextButton(
                    onClick = { deleteAllError = null }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4A5568)
                )
            }
        }
    }
}

@Composable
fun ModernMealPlateHistoryCard(
    historyItem: com.insumeal.models.MealPlateHistory,
    onViewDetails: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con título y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = historyItem.type.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF2D3748)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Fecha destacada con fondo y icono
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7FAFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF4299E1),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = historyItem.date,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF4299E1)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón ver detalles
                    IconButton(
                        onClick = { onViewDetails(historyItem.id) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF7FAFF))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "Ver detalles",
                            tint = Color(0xFF4299E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Botón eliminar
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFF5F5))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Tres globos circulares con información nutricional (igual que DosisScreen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularInfoCard(
                    label = "Carbohidratos",
                    value = "${String.format("%.1f", historyItem.totalCarbs)} g",
                    backgroundColor = Color(0xFFFF9800), // Naranja elegante
                    icon = Icons.Default.Restaurant,
                    modifier = Modifier.weight(1f)
                )

                CircularInfoCard(
                    label = "Glucemia",
                    value = "${String.format("%.0f", historyItem.glycemia)} mg/dL",
                    backgroundColor = Color(0xFFE91E63), // Rosa elegante
                    icon = Icons.Default.Bloodtype,
                    modifier = Modifier.weight(1f)
                )

                CircularInfoCard(
                    label = "Dosis Total",
                    value = "${String.format("%.1f", historyItem.dosis)} U",
                    backgroundColor = Color(0xFF2196F3), // Azul elegante
                    icon = Icons.Default.MedicalServices,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Confirmar eliminación")
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar esta comida? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(historyItem.id)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CircularInfoCard(
    label: String,
    value: String,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Globo circular con color de fondo sutil del icono
        Box(
            modifier = Modifier
                .size(55.dp) // Reducido de 75.dp a 65.dp
                .background(
                    color = backgroundColor.copy(alpha = 0.1f), // Color de fondo sutil del icono
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .clip(androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(30.dp) // Reducido de 36.dp a 30.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Información debajo del globo con mejor tipografía
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
