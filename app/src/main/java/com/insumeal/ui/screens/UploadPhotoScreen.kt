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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.insumeal.ui.theme.Turquoise500
import com.insumeal.ui.theme.Turquoise600
import com.insumeal.ui.viewmodel.MealPlateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPhotoScreen(
    navController: NavController,
    mealPlateViewModel: MealPlateViewModel
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Observar los StateFlow del ViewModel
    val isLoading by mealPlateViewModel.isLoading.collectAsState()
    val errorMessage by mealPlateViewModel.errorMessage.collectAsState()
    
    // Estado local para cuando no se detectan comidas
    var showNoFoodDetectedDialog by remember { mutableStateOf(false) }
    
    // Crear URI para la foto de alta resolución
    val photoUri = remember {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(null)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    // Cambiar de TakePicturePreview a TakePicture para captura en alta resolución
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            try {
                // Usar la URI de alta resolución en lugar de bitmap
                imageUri = photoUri
                bitmap = null
                mealPlateViewModel.setImage(photoUri, null)
            } catch (e: Exception) {
                // Manejar errores
                android.util.Log.e("UploadPhotoScreen", "Error al capturar foto: ${e.message}")
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Asegurarnos de que la URI sea persistente para que no desaparezca después
            try {
                // Guardamos la URI y limpiamos el bitmap para evitar conflictos
                imageUri = uri
                bitmap = null
                // También podemos establecer la imagen en el ViewModel directamente
                mealPlateViewModel.setImage(uri, null)
            } catch (e: Exception) {
                // Mostrar un error si algo sale mal
                // No podemos asignar directamente, el ViewModel maneja los errores internamente
            }
        }
    }
    
    // Mostrar diálogo de carga si está procesando la imagen
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar durante la carga */ },
            title = {
                Text(
                    "Procesando imagen",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2D3748)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = Color(0xFFFF6B35),
                        strokeWidth = 4.dp
                    )
                    Text(
                        "El procesamiento de la imagen puede tardar varios segundos. Por favor, espera...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4A5568)
                    )
                }
            },
            confirmButton = { },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Mostrar mensaje de error si ocurrió alguno
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { mealPlateViewModel.clearError() },
            title = {
                Text(
                    "Error",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2D3748)
                )
            },
            text = {
                Text(
                    errorMessage!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4A5568)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mealPlateViewModel.clearError()
                        if (errorMessage!!.contains("autenticación") ||
                            errorMessage!!.contains("sesión")) {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Aceptar", color = Color.White)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Diálogo cuando no se detectan comidas en la imagen
    if (showNoFoodDetectedDialog) {
        AlertDialog(
            onDismissRequest = { showNoFoodDetectedDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color(0xFFFF6B35).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "No se detectaron comidas",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hemos podido detectar ningún alimento en la imagen que seleccionaste.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4A5568),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "• Asegúrate de que la comida esté bien iluminada\n• Intenta tomar la foto desde un ángulo diferente\n• Verifica que los alimentos sean claramente visibles",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = Color(0xFF718096)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNoFoodDetectedDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Intentar de nuevo", color = Color.White)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header con forma ondulada elegante
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
                        // Botón de volver atrás
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
                                text = "Analizar Comida",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mensaje descriptivo movido aquí, fuera del gradiente naranja
                Text(
                    text = "Captura una foto de tu plato o selecciona una imagen desde la galería",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF4A5568),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Card principal de imagen siguiendo el estilo de HomeScreen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    if (bitmap != null || imageUri != null) {
                        Box(
                            modifier = Modifier.fillMaxSize()
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
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val icons = listOf(
                                Icons.Default.DinnerDining,
                                Icons.Default.Fastfood
                            )

                            val iconDescriptions = listOf(
                                "Ícono de plato de comida",
                                "Ícono de comida rápida"
                            )

                            val numIcons = icons.size
                            var currentIconIndex by remember { mutableStateOf(0) }

                            LaunchedEffect(Unit) {
                                while (isActive) {
                                    delay(2000L)
                                    currentIconIndex = (currentIconIndex + 1) % numIcons
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    androidx.compose.animation.Crossfade(
                                        targetState = currentIconIndex,
                                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
                                        label = "iconCrossfade"
                                    ) { displayedIndex ->
                                        Icon(
                                            imageVector = icons[displayedIndex],
                                            contentDescription = iconDescriptions[displayedIndex],
                                            modifier = Modifier.size(80.dp),
                                            tint = Turquoise500.copy(alpha = 0.3f) // Cambiado de naranja a turquesa
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Listo para capturar tu comida",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF718096),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de acción siguiendo el estilo de ModernActionCard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(photoUri) },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            elevation = null
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Turquoise600 // Cambiado de naranja a turquesa
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Cámara",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF2D3748)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            elevation = null
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Turquoise600 // Cambiado de naranja a turquesa
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Galería",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color(0xFF2D3748)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Mensaje informativo
                if (bitmap != null || imageUri != null) {
                    Text(
                        text = "El procesamiento de la imagen puede tardar varios segundos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF718096),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Botón de continuar siguiendo el estilo de la app
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if ((bitmap != null || imageUri != null) && !isLoading)
                            Color(0xFFFF6B35) else Color(0xFFE2E8F0)
                    )
                ) {
                    Button(
                        onClick = {
                            android.util.Log.d("UploadPhotoScreen", "Iniciando análisis de imagen")
                            mealPlateViewModel.analyzeImage(
                                context = context,
                                onSuccess = {
                                    android.util.Log.d("UploadPhotoScreen", "Análisis completado con éxito, navegando a MealPlateScreen")
                                    navController.navigate("mealPlate")
                                },
                                onNoFoodDetected = {
                                    android.util.Log.d("UploadPhotoScreen", "No se detectaron comidas en la imagen")
                                    showNoFoodDetectedDialog = true
                                }
                            )
                        },
                        enabled = (bitmap != null || imageUri != null) && !isLoading,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        elevation = null
                    ) {
                        if (isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Procesando...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                        } else {
                            Text(
                                "Continuar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = if ((bitmap != null || imageUri != null))
                                    Color.White else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
