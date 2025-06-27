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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.insumeal.ui.viewmodel.MealPlateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            try {
                // Guardamos el bitmap y limpiamos la URI para evitar conflictos
                bitmap = bmp
                imageUri = null
                // También establecemos la imagen en el ViewModel directamente
                mealPlateViewModel.setImage(null, bmp)
            } catch (e: Exception) {
                // Mostrar un error si algo sale mal
                // No podemos asignar directamente, el ViewModel maneja los errores internamente
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
            title = { Text("Procesando imagen") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "El procesamiento de la imagen puede tardar varios segundos. Por favor, espera...",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = { /* Sin botones durante la carga */ },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }    // Mostrar mensaje de error si ocurrió alguno
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { mealPlateViewModel.clearError() },            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Necesitamos agregar un método clearError al ViewModel
                        mealPlateViewModel.clearError()
                        // Si es un error de autenticación, redirigir a login
                        if (errorMessage!!.contains("autenticación") || 
                            errorMessage!!.contains("sesión")) {
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
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
                            MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "• Asegúrate de que la comida esté bien iluminada\n• Intenta tomar la foto desde un ángulo diferente\n• Verifica que los alimentos sean claramente visibles",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNoFoodDetectedDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Intentar de nuevo")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
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
        Column( // Main Column
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
            // verticalArrangement = Arrangement.spacedBy(24.dp) // REMOVED
        ) {
            // NEW Top Content Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column( // Existing "Toma una foto..." text block
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Toma una foto de tu plato o selecciona una imagen desde la galería",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp)) // ADDED for spacing

                // Existing if/else for image or placeholder
                if (bitmap != null || imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
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
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        // Lista de íconos disponibles actualizada
                        val icons = listOf(
                            Icons.Default.DinnerDining,
                            Icons.Default.Fastfood
                        )
                        
                        // Descripción de los íconos actualizada
                        val iconDescriptions = listOf(
                            "Ícono de plato de comida",
                            "Ícono de comida rápida"
                        )
                        // Se reemplaza la animación basada en infiniteTransition por LaunchedEffect y delay
                        val numIcons = icons.size
                        var currentIconIndex by remember { mutableStateOf(0) }

                        LaunchedEffect(Unit) { // El Unit como key asegura que se lance una vez
                            while (isActive) { // isActive proviene del CoroutineScope de LaunchedEffect y asegura la cancelación
                                delay(2000L) // Espera 2 segundos
                                currentIconIndex = (currentIconIndex + 1) % numIcons
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Reintroducir Crossfade para una transición suave
                                androidx.compose.animation.Crossfade(
                                    targetState = currentIconIndex,
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 500), // Duración del fundido para suavidad
                                    label = "iconCrossfade"
                                ) { displayedIndex ->
                                    Icon(
                                        imageVector = icons[displayedIndex],
                                        contentDescription = iconDescriptions[displayedIndex],
                                        modifier = Modifier.size(120.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }                        
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "Listo para capturar tu comida",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } // Cierre del Box del placeholder
                } // Cierre del else
            } // END OF: Top Content Column

            Spacer(Modifier.weight(1f)) // Spacer to push bottom content down

            // NEW Bottom Content Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botones de cámara y galería con iconos
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                        // .padding(vertical = 16.dp), // Padding handled by parent Column's top padding and spacedBy
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
            
            // Mensaje informativo sobre el tiempo de procesamiento
            if (bitmap != null || imageUri != null) {
                Text(
                    text = "El procesamiento de la imagen puede tardar varios segundos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }                // Botón de continuar
            Button(                onClick = {
                    // Usamos el contexto que ya fue capturado anteriormente
                    android.util.Log.d("UploadPhotoScreen", "Iniciando análisis de imagen")
                      // El ViewModel ahora maneja el estado internamente
                    mealPlateViewModel.analyzeImage(
                        context = context,
                        onSuccess = {
                            // Callback de éxito - navegar a la siguiente pantalla
                            android.util.Log.d("UploadPhotoScreen", "Análisis completado con éxito, navegando a MealPlateScreen")
                            android.util.Log.d("UploadPhotoScreen", "ViewModel instance antes de navegar: $mealPlateViewModel")
                            navController.navigate("mealPlate")
                        },
                        onNoFoodDetected = {
                            // Callback cuando no se detectan comidas
                            android.util.Log.d("UploadPhotoScreen", "No se detectaron comidas en la imagen")
                            showNoFoodDetectedDialog = true
                        }
                    )
                },
                enabled = (bitmap != null || imageUri != null) && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    disabledElevation = 0.dp
                )            ){
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Procesando...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    Text(
                        "Continuar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        } // END OF: Bottom Content Column
        } // END OF: Main Column
    } // END OF: Scaffold


