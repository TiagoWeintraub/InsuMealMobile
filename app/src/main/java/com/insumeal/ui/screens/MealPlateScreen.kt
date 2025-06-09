package com.insumeal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val context = LocalContext.current
    
    // Usar collectAsState para observar los StateFlow del ViewModel
    val mealPlate by mealPlateViewModel.mealPlate.collectAsState()
    val isLoading by mealPlateViewModel.isLoading.collectAsState()
    val errorMessage by mealPlateViewModel.errorMessage.collectAsState()
    val hasAttemptedLoad by mealPlateViewModel.hasAttemptedLoad.collectAsState()
      // Estados para la edición de ingredientes
    var editingIngredientId by remember { mutableStateOf<Int?>(null) }
    var editGrams by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    
    // Estados para el cálculo de dosis
    var glycemiaInput by remember { mutableStateOf("") }
    var isCalculatingDosis by remember { mutableStateOf(false) }
    var dosisCalculationError by remember { mutableStateOf<String?>(null) }
    
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
                        modifier = Modifier.padding(vertical = 8.dp)                    )
                }
                
                // Lista de ingredientes con capacidad de edición
                items(mealPlate!!.ingredients) { ingredient ->
                    IngredientEditableCard(
                        ingredient = ingredient,
                        isEditing = editingIngredientId == ingredient.id,
                        editGrams = editGrams,
                        isUpdating = isUpdating,
                        onEditStart = { 
                            editingIngredientId = ingredient.id
                            editGrams = ingredient.grams.toInt().toString()
                        },
                        onEditCancel = { 
                            editingIngredientId = null
                            editGrams = ""
                            updateError = null
                        },
                        onGramsChange = { editGrams = it },
                        onEditConfirm = {
                            val newGrams = editGrams.toDoubleOrNull()
                            if (newGrams != null && newGrams > 0) {
                                isUpdating = true
                                updateError = null
                                
                                mealPlateViewModel.updateIngredientGrams(
                                    context = context,
                                    mealPlateId = mealPlate!!.id,
                                    ingredientId = ingredient.id,
                                    newGrams = newGrams,
                                    onSuccess = {
                                        isUpdating = false
                                        editingIngredientId = null
                                        editGrams = ""
                                    },
                                    onError = { error ->
                                        isUpdating = false
                                        updateError = error
                                    }
                                )
                            } else {
                                updateError = "Por favor, ingresa un valor válido mayor a 0"                            }
                        }
                    )
                }
                
                // Campo para ingresar glucemia y botón para calcular dosis
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tarjeta para el cálculo de dosis
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
                            Text(
                                text = "Cálculo de Dosis",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "Ingresa tu nivel actual de glucemia para calcular la dosis de insulina recomendada:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                              // Campo de entrada para glucemia
                            OutlinedTextField(
                                value = glycemiaInput,
                                onValueChange = { glycemiaInput = it },
                                label = { Text("Glucemia actual (mg/dL)") },
                                placeholder = { Text("Entre 25 y 250 mg/dL") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !isCalculatingDosis,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Bloodtype,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                supportingText = {
                                    Text("Valores permitidos: entre 25 y 250 mg/dL")
                                }
                            )
                            
                            // Mostrar error si lo hay
                            if (dosisCalculationError != null) {
                                Text(
                                    text = dosisCalculationError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                              // Botón para calcular dosis
                            Button(
                                onClick = { 
                                    val glycemiaValue = glycemiaInput.toDoubleOrNull()
                                    when {
                                        glycemiaValue == null -> {
                                            dosisCalculationError = "Por favor, ingresa un valor válido de glucemia"
                                        }
                                        glycemiaValue < 25 -> {
                                            dosisCalculationError = "No se permite calcular dosis con glucemia menor a 25 mg/dL"
                                        }
                                        glycemiaValue > 250 -> {
                                            dosisCalculationError = "No se recomienda consumir carbohidratos con glucemia superior a 250 mg/dL"
                                        }
                                        else -> {
                                            isCalculatingDosis = true
                                            dosisCalculationError = null
                                            
                                            mealPlateViewModel.calculateDosis(
                                                context = context,
                                                mealPlateId = mealPlate!!.id,
                                                glycemia = glycemiaValue,
                                                onSuccess = {
                                                    isCalculatingDosis = false
                                                    navController.navigate("dosis")
                                                },
                                                onError = { error ->
                                                    isCalculatingDosis = false
                                                    dosisCalculationError = error
                                                }
                                            )
                                        }
                                    }
                                },                                enabled = glycemiaInput.isNotBlank() && !isCalculatingDosis &&
                                          glycemiaInput.toDoubleOrNull()?.let { it >= 25 && it <= 250 } ?: false,
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                if (isCalculatingDosis) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Calculando...",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Calcular Dosis",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
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
    
    // Diálogo de error para actualizaciones
    if (updateError != null) {
        AlertDialog(
            onDismissRequest = { updateError = null },
            title = { Text("Error al actualizar") },
            text = { Text(updateError!!) },
            confirmButton = {
                TextButton(onClick = { updateError = null }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun IngredientEditableCard(
    ingredient: Ingredient,
    isEditing: Boolean,
    editGrams: String,
    isUpdating: Boolean,
    onEditStart: () -> Unit,
    onEditCancel: () -> Unit,
    onGramsChange: (String) -> Unit,
    onEditConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        if (isEditing) {
            // Modo edición
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = editGrams,
                            onValueChange = onGramsChange,
                            label = { Text("Gramos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isUpdating
                        )
                        
                        // Mostrar carbohidratos calculados en tiempo real
                        val previewCarbs = editGrams.toDoubleOrNull()?.let { grams ->
                            (grams / 100.0) * ingredient.carbsPerHundredGrams
                        } ?: ingredient.carbs
                        
                        Text(
                            text = "≈ ${String.format("%.1f", previewCarbs)} Carbs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEditCancel,
                            enabled = !isUpdating
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        IconButton(
                            onClick = onEditConfirm,
                            enabled = !isUpdating && editGrams.isNotBlank()
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirmar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Modo visualización
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditStart() }
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
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${String.format("%.0f", ingredient.grams)} Gramos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${String.format("%.1f", ingredient.carbs)} Carbs",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}