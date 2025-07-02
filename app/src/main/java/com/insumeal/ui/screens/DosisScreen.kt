package com.insumeal.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.insumeal.api.RetrofitClient
import com.insumeal.ui.viewmodel.MealPlateViewModel
import com.insumeal.utils.ImageUtils
import com.insumeal.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosisScreen(
    navController: NavController,
    mealPlateViewModel: MealPlateViewModel
) {
    // Observar los StateFlow del ViewModel
    val mealPlate by mealPlateViewModel.mealPlate.collectAsState()
    val dosisCalculation by mealPlateViewModel.dosisCalculation.collectAsState()
    val context = LocalContext.current

    // Estado para controlar el modal de advertencia
    var showDisclaimerDialog by remember { mutableStateOf(true) }

    // Estados para controlar los desplegables
    var isDosisExpanded by remember { mutableStateOf(false) }
    var isCarbsDetailExpanded by remember { mutableStateOf(false) }

    // Manejar el botón de volver atrás del sistema (hardware o gesto)
    BackHandler {
        // Si hay un plato cargado, eliminarlo antes de volver atrás
        mealPlate?.let { plate ->
            mealPlateViewModel.deleteMealPlate(
                context = context,
                mealPlateId = plate.id,
                onSuccess = {
                    navController.navigateUp()
                },
                onError = { error ->
                    android.util.Log.e("DosisScreen", "Error al eliminar plato: $error")
                    // Incluso si hay error, volver atrás
                    navController.navigateUp()
                }
            )
        } ?: run {
            // Si no hay plato, simplemente volver atrás
            navController.navigateUp()
        }
    }

    Scaffold(
        // Eliminar el topBar para que la imagen llegue hasta arriba
    ) { paddingValues ->

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
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Cargando información del plato...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Mostrar detalles del plato cuando tenemos datos
            val currentMealPlate = mealPlate!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp)
                    )
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
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Información nutricional detallada
                        if (dosisCalculation != null) {
                            item {
                                // Título de la sección con icono verde
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start // Alineado a la izquierda
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50), // Verde elegante (mantiene el verde)
                                        modifier = Modifier.size(20.dp) // Icono más pequeño
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Información",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp // Tamaño más pequeño
                                        ),
                                        color = Color.Black // Texto negro
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
                                        value = "${String.format("%.1f", dosisCalculation!!.totalCarbs)} g",
                                        backgroundColor = Color(0xFFFF9800), // Naranja elegante
                                        icon = Icons.Default.Restaurant,
                                        modifier = Modifier.weight(1f)
                                    )

                                    CircularInfoBubble(
                                        label = "Glucemia",
                                        value = "${String.format("%.0f", dosisCalculation!!.glycemia)} mg/dL",
                                        backgroundColor = Color(0xFFE91E63), // Rosa elegante
                                        icon = Icons.Default.Bloodtype,
                                        modifier = Modifier.weight(1f)
                                    )

                                    CircularInfoBubble(
                                        label = "Dosis Total",
                                        value = "${String.format("%.1f", dosisCalculation!!.totalDose)} U",
                                        backgroundColor = Color(0xFF2196F3), // Azul elegante
                                        icon = Icons.Default.MedicalServices,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Mensaje de advertencia si glucemia > 170
                            if (dosisCalculation!!.glycemia > 170) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFEF5350),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = "No se recomienda comer alimentos con carbohidratos cuando la glucemia está elevada",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color(0xFFEF5350)
                                            )
                                        }
                                    }
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
                                        containerColor = Color.White
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
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
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
                                                        text = "${String.format("%.1f", dosisCalculation!!.correctionInsulin)} U",
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
                                                        text = "${String.format("%.1f", dosisCalculation!!.carbInsulin)} U",
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
                                        .clickable { isCarbsDetailExpanded = !isCarbsDetailExpanded },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
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
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.Black
                                                )
                                            }

                                            Icon(
                                                imageVector = if (isCarbsDetailExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = if (isCarbsDetailExpanded) "Colapsar" else "Expandir",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isCarbsDetailExpanded,
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
                                                        // Información del ingrediente con el mismo estilo que el desglose del cálculo
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

                            // Botones horizontales: Volver al inicio y Editar el plato
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Botón para volver al home
                                    Card(
                                        modifier = Modifier.weight(1f),
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

                                    // Botón para editar el plato
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    navController.navigateUp()
                                                }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Editar",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Espaciado adicional al final
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        } else {
                            // Estado cuando no hay datos del cálculo
                            item {
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
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(12.dp)
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
                                                    text = "Volver al Inicio",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de advertencia que aparece al cargar la pantalla
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar tocando fuera */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Información Importante",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Esta información es orientativa. InsuMeal no asume responsabilidad por el uso de esta estimación.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDisclaimerDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Aceptar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun CircularInfoBubble(
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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
