package com.insumeal.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.insumeal.ui.viewmodel.MealPlateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPhotoScreen(navController: NavController) {
    // Eliminamos la variable 'context' que no se usa
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val mealPlateViewModel: MealPlateViewModel = viewModel()
    
    // Función simplificada para calcular la opacidad de los íconos
    fun calculateIconAlpha(currentIndex: Float, targetIndex: Int): Float {
        // Convertimos el índice actual a entero para comparaciones simples
        val currentIndexInt = currentIndex.toInt()
        
        // Caso exacto: ícono completamente visible
        if (currentIndexInt == targetIndex) {
            // Calculamos qué tan cerca estamos del índice exacto
            val fraction = 1f - (currentIndex - currentIndexInt)
            return if (fraction > 0.7f) 1f else fraction
        }
        
        // Caso especial: transición del último ícono (3) al primero (0)
        if (currentIndexInt == 3 && targetIndex == 0) {
            // Calculamos qué tan cerca estamos del siguiente ciclo
            val fraction = currentIndex - currentIndexInt
            return if (fraction > 0.3f) fraction else 0f
        }
        
        // Caso de transición normal: ícono anterior desvaneciéndose
        if (currentIndexInt + 1 == targetIndex) {
            val fraction = currentIndex - currentIndexInt
            return if (fraction < 0.3f) fraction else 0f
        }
        
        // Por defecto: invisible
        return 0f
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            bitmap = bmp
            imageUri = null
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            bitmap = null
        }
    }
    Scaffold(
            topBar = {
            TopAppBar(
                title = { Text("Captura tu comida") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)        ) {            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Toma una foto o selecciona una imagen de tu plato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // Mostrar la imagen si está disponible
            if (bitmap != null || imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Foto tomada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {                // Placeholder cuando no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {                    // Animación entre varios íconos
                    val infiniteTransition = rememberInfiniteTransition(label = "iconTransition")
                    val iconIndex = infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 4f,  // 4 íconos en total
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 6000,  // Duración total reducida a 6 segundos
                                easing = FastOutSlowInEasing  // Aceleración al inicio, desaceleración al final
                            ),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "iconIndex"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {                        Box(contentAlignment = Alignment.Center) {                            // Calcular las opacidades para cada ícono
                            val cameraAlpha = calculateIconAlpha(iconIndex.value, 0)
                            val plateAlpha = calculateIconAlpha(iconIndex.value, 1)
                            val fastFoodAlpha = calculateIconAlpha(iconIndex.value, 2)
                            val foodBankAlpha = calculateIconAlpha(iconIndex.value, 3)
                              // Función para calcular el tamaño basado en la opacidad
                            fun calculateIconSize(alpha: Float): Modifier {
                                val baseSize = 120.dp
                                // Escala más sutil: entre 90% y 100% basado en alpha
                                val scale = 0.9f + (0.1f * alpha) 
                                return Modifier.size(baseSize).scale(scale)
                            }
                            
                            // Ícono de cámara
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Ícono de cámara",
                                modifier = calculateIconSize(cameraAlpha),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = cameraAlpha)
                            )
                            
                            // Ícono de plato
                            Icon(
                                imageVector = Icons.Default.DinnerDining,
                                contentDescription = "Ícono de plato de comida",
                                modifier = calculateIconSize(plateAlpha),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = plateAlpha)
                            )                              // Ícono de comida rápida (hamburguesa)
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = "Ícono de comida rápida",
                                modifier = calculateIconSize(fastFoodAlpha),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = fastFoodAlpha)
                            )
                            
                            // Ícono de restaurante (comida)
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Ícono de restaurante",
                                modifier = calculateIconSize(foodBankAlpha),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = foodBankAlpha)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Listo para capturar tu comida",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
              // Botones de cámara y galería con iconos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        "Tomar Foto",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        "Galería",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
              Spacer(modifier = Modifier.weight(1f))
            
            // Mensaje informativo sobre el tiempo de procesamiento
            if (bitmap != null || imageUri != null) {
                Text(
                    text = "El procesamiento de la imagen puede tardar varios segundos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
              
            // Botón de continuar
            Button(
                onClick = {
                    mealPlateViewModel.setImage(imageUri, bitmap)
                    navController.navigate("mealPlate")
                },
                enabled = (bitmap != null || imageUri != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(
                    "Continuar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

