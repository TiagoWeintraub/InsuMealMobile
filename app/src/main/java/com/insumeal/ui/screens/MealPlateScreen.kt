package com.insumeal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.zIndex
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
    val hasAttemptedLoad by mealPlateViewModel.hasAttemptedLoad.collectAsState()    // Estados para la edición de ingredientes
    var editingIngredientId by remember { mutableStateOf<Int?>(null) }
    var editGrams by remember { mutableStateOf("") }
    var editGramsInput by remember { mutableStateOf("") } // Para el modal de edición
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    
    // Estados para la eliminación de ingredientes
    var deletingIngredientId by remember { mutableStateOf<Int?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
      // Estados para el cálculo de dosis
    val lastGlycemia by mealPlateViewModel.lastGlycemia.collectAsState()
    var glycemiaInput by remember { mutableStateOf("") }
    var isCalculatingDosis by remember { mutableStateOf(false) }
    var dosisCalculationError by remember { mutableStateOf<String?>(null) }
    
    // Efecto para restaurar la glucemia guardada
    LaunchedEffect(lastGlycemia) {
        if (lastGlycemia.isNotEmpty() && glycemiaInput.isEmpty()) {
            glycemiaInput = lastGlycemia
        }
    }// Efecto para sincronizar los estados y hacer logs
    LaunchedEffect(mealPlate) {
        android.util.Log.d("MealPlateScreen", "ViewModel mealPlate cambió: ${mealPlate?.name}")
        mealPlate?.let { plate ->
            android.util.Log.d("MealPlateScreen", "Número de ingredientes: ${plate.ingredients.size}")
            plate.ingredients.forEachIndexed { index, ingredient ->
                android.util.Log.d("MealPlateScreen", "Ingrediente $index: ${ingredient.name} (ID: ${ingredient.id})")
            }
        }
    }// Log inicial para verificar el estado al entrar a la pantalla
    LaunchedEffect(Unit) {
        android.util.Log.d("MealPlateScreen", "Entrando a MealPlateScreen - Estado inicial:")
        android.util.Log.d("MealPlateScreen", "mealPlate=${mealPlate?.name}, isLoading=${isLoading}, hasAttemptedLoad=${hasAttemptedLoad}")
        android.util.Log.d("MealPlateScreen", "ViewModel instance: $mealPlateViewModel")
        
        // Inicializar el servicio de traducción
        mealPlateViewModel.initializeTranslationService()
    }
    
    LaunchedEffect(isLoading) {
        android.util.Log.d("MealPlateScreen", "isLoading cambió: ${isLoading}")
    }
    LaunchedEffect(hasAttemptedLoad) {
        android.util.Log.d("MealPlateScreen", "hasAttemptedLoad cambió: ${hasAttemptedLoad}")
    }
    
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
                    android.util.Log.e("MealPlateScreen", "Error al eliminar plato: $error")
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
        topBar = {
            TopAppBar(
                title = { Text("Detalles del plato") },
                navigationIcon = {
                    IconButton(onClick = { 
                        // Si hay un plato cargado, eliminarlo antes de volver atrás
                        mealPlate?.let { plate ->
                            mealPlateViewModel.deleteMealPlate(
                                context = context,
                                mealPlateId = plate.id,
                                onSuccess = {
                                    navController.navigateUp()
                                },
                                onError = { error ->
                                    android.util.Log.e("MealPlateScreen", "Error al eliminar plato: $error")
                                    // Incluso si hay error, volver atrás
                                    navController.navigateUp()
                                }
                            )
                        } ?: run {
                            // Si no hay plato, simplemente volver atrás
                            navController.navigateUp()
                        }
                    }) {
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
            )        }) { paddingValues ->
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
                        )                    }
                    else if (errorMessage != null) {
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
                            onClick = { 
                                // Si hay un plato cargado, eliminarlo antes de volver atrás
                                mealPlate?.let { plate ->
                                    mealPlateViewModel.deleteMealPlate(
                                        context = context,
                                        mealPlateId = plate.id,
                                        onSuccess = {
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            android.util.Log.e("MealPlateScreen", "Error al eliminar plato: $error")
                                            // Incluso si hay error, volver atrás
                                            navController.popBackStack()
                                        }
                                    )
                                } ?: run {
                                    // Si no hay plato, simplemente volver atrás
                                    navController.popBackStack()
                                }
                            },
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
                            onClick = { 
                                // Si hay un plato cargado, eliminarlo antes de volver atrás
                                mealPlate?.let { plate ->
                                    mealPlateViewModel.deleteMealPlate(
                                        context = context,
                                        mealPlateId = plate.id,
                                        onSuccess = {
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            android.util.Log.e("MealPlateScreen", "Error al eliminar plato: $error")
                                            // Incluso si hay error, volver atrás
                                            navController.popBackStack()
                                        }
                                    )
                                } ?: run {
                                    // Si no hay plato, simplemente volver atrás
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Volver e intentar de nuevo")
                        }
                    }
                }            }        } else {
            // Mostrar detalles del plato cuando tenemos datos
            // Capturar el valor del plato para evitar problemas de recomposición
            val currentMealPlate = mealPlate
            if (currentMealPlate != null) {
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
                        text = currentMealPlate.name.uppercase(),
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
                }                // Lista de ingredientes con capacidad de edición
                items(currentMealPlate.ingredients) { ingredient ->                    IngredientEditableCard(
                        ingredient = ingredient,
                        isEditing = false, // Ya no necesitamos el modo de edición inline
                        editGrams = "", // Ya no se usa
                        isUpdating = isUpdating && editingIngredientId == ingredient.id,
                        isLastIngredient = currentMealPlate.ingredients.size == 1,
                        onEditStart = { 
                            // Ya no se usa, la edición se abre desde el modal
                        },
                        onEditCancel = { 
                            // Ya no se usa
                        },
                        onGramsChange = { 
                            // Ya no se usa
                        },                        onEditConfirm = { newGramsValue ->
                            // La lógica de actualización usando el valor pasado desde el modal
                            if (newGramsValue != null && newGramsValue > 0) {
                                isUpdating = true
                                editingIngredientId = ingredient.id
                                updateError = null
                                  mealPlateViewModel.updateIngredientGrams(
                                    context = context,
                                    mealPlateId = currentMealPlate.id,
                                    ingredientId = ingredient.id,
                                    newGrams = newGramsValue,
                                    onSuccess = {
                                        isUpdating = false
                                        editingIngredientId = null
                                        editGramsInput = ""
                                    },
                                    onError = { error ->
                                        isUpdating = false
                                        editingIngredientId = null
                                        updateError = error
                                    }
                                )
                            }
                        },onDeleteIngredient = {
                            // Usar el estado actual del ViewModel en lugar de currentMealPlate capturado
                            val currentPlate = mealPlate
                            if (currentPlate != null) {
                                deletingIngredientId = ingredient.id
                                deleteError = null
                                
                                // Verificar si es el último ingrediente
                                if (currentPlate.ingredients.size == 1) {
                                    // Si es el último ingrediente, eliminar todo el meal plate
                                    mealPlateViewModel.deleteMealPlate(
                                        context = context,
                                        mealPlateId = currentPlate.id,
                                        onSuccess = {
                                            deletingIngredientId = null
                                            // Navegar a la pantalla anterior (uploadPhoto)
                                            navController.navigateUp()
                                        },
                                        onError = { error ->
                                            deletingIngredientId = null
                                            deleteError = error
                                        }
                                    )
                                } else {
                                    // Si hay más ingredientes, solo eliminar el ingrediente
                                    mealPlateViewModel.deleteIngredientFromMealPlate(
                                        context = context,
                                        mealPlateId = currentPlate.id,
                                        ingredientId = ingredient.id,
                                        onSuccess = {
                                            deletingIngredientId = null
                                        },
                                        onError = { error ->
                                            deletingIngredientId = null
                                            deleteError = error
                                        }
                                    )
                                }
                            }
                        },
                        isDeleting = deletingIngredientId == ingredient.id
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
                                                mealPlateId = currentMealPlate.id,
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
                }                // Espacio al final
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            } // fin del if (currentMealPlate != null)
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
    
    // Diálogo de error para eliminaciones
    if (deleteError != null) {
        AlertDialog(
            onDismissRequest = { deleteError = null },
            title = { Text("Error al eliminar ingrediente") },
            text = { Text(deleteError!!) },
            confirmButton = {
                TextButton(onClick = { deleteError = null }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditableCard(
    ingredient: Ingredient,
    isEditing: Boolean,
    editGrams: String,
    isUpdating: Boolean,
    isLastIngredient: Boolean,
    onEditStart: () -> Unit,
    onEditCancel: () -> Unit,
    onGramsChange: (String) -> Unit,
    onEditConfirm: (Double?) -> Unit,
    onDeleteIngredient: () -> Unit,
    isDeleting: Boolean = false
) {
    // Estados para el modal de edición
    var showEditModal by remember { mutableStateOf(false) }
    var editGramsInput by remember { mutableStateOf("") }
    
    // Estado para el modal de confirmación
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe de izquierda a derecha -> Eliminar
                    showDeleteConfirmation = true
                    false // No dismissamos automáticamente, mostramos el modal
                }                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe de derecha a izquierda -> Editar
                    editGramsInput = ingredient.grams.toInt().toString()
                    showEditModal = true
                    false // No dismissamos automáticamente, mostramos el modal
                }
                else -> false
            }
        },
        // Configurar el threshold para requerir más distancia de deslizamiento
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )

    // Row que contiene la lengüeta del basurin y la card del ingrediente
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lengüeta del basurin que sobresale del lado izquierdo
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // SwipeToDismissBox con la card del ingrediente
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.weight(1f),
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                // Fondo dinámico dependiendo de la dirección del swipe
                val direction = dismissState.dismissDirection
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        // Fondo rojo para eliminar (swipe hacia la derecha)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                
                                Text(
                                    text = "Eliminar",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {                        // Fondo azul suave para editar (swipe hacia la izquierda)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF6B9DC3)),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier.padding(end = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Editar",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                
                            }
                        }
                    }
                    else -> {
                        // Fondo por defecto
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        ) {            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(0.dp) // Bordes rectos sin redondear
            ) {            // Solo modo visualización estática, la edición se hace únicamente mediante swipe
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
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${String.format("%.0f", ingredient.grams)} g",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${String.format("%.1f", ingredient.carbs)} g de carbohidratos",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Lengüeta de editar que sobresale del lado derecho
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
                .background(
                    Color(0xFF6B9DC3), // Color azul suave para editar
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 8.dp,
                        bottomEnd = 8.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Modal de confirmación para eliminar - diseño mejorado
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                coroutineScope.launch {
                    dismissState.reset()
                }
            },
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
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Eliminar ingrediente",
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
                    // Verificar si es el último ingrediente para mostrar mensaje apropiado
                    if (isLastIngredient) {
                        Text(
                            text = "Este es el último ingrediente del plato.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Al eliminarlo se borrará todo el plato y volverás a la pantalla anterior.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "¿Realmente quiere borrar el ingrediente:",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "\"${ingredient.name}\"?",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones mejorados con mejor diseño
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón Cancelar
                        OutlinedButton(
                            onClick = {
                                showDeleteConfirmation = false
                                coroutineScope.launch {
                                    dismissState.reset()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Cancelar",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        
                        // Botón Eliminar
                        Button(
                            onClick = {
                                showDeleteConfirmation = false
                                onDeleteIngredient()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Sí, eliminar",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = { },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
      // Modal de edición estético
    if (showEditModal) {
        // Actualizar el valor cada vez que se abre el modal
        LaunchedEffect(showEditModal) {
            if (showEditModal) {
                editGramsInput = ingredient.grams.toInt().toString()
            }
        }
        
        AlertDialog(
            onDismissRequest = { 
                showEditModal = false
                editGramsInput = ""
                coroutineScope.launch {
                    dismissState.reset()
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color(0xFF6B9DC3).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF6B9DC3),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Editar cantidad",
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
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = editGramsInput,
                        onValueChange = { newValue ->
                            // Solo permitir números y un punto decimal
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                editGramsInput = newValue
                            }
                        },
                        label = { Text("Cantidad en gramos") },
                        placeholder = { Text("Ej: 150") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isUpdating,
                        modifier = Modifier.fillMaxWidth(),                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6B9DC3),
                            focusedLabelColor = Color(0xFF6B9DC3)
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Vista previa de carbohidratos
                    Card(
                        modifier = Modifier.fillMaxWidth(),                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6B9DC3).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val previewCarbs = editGramsInput.toDoubleOrNull()?.let { grams ->
                                (grams / 100.0) * ingredient.carbsPerHundredGrams
                            } ?: ingredient.carbs
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFF6B9DC3),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Vista previa:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${String.format("%.1f", previewCarbs)} g de carbohidratos",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF6B9DC3),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón Cancelar
                        OutlinedButton(
                            onClick = {
                                showEditModal = false
                                editGramsInput = ""
                                coroutineScope.launch {
                                    dismissState.reset()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            ),
                            enabled = !isUpdating
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Cancelar",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        // Botón Guardar
                        Button(
                            onClick = {
                                val newGrams = editGramsInput.toDoubleOrNull()
                                if (newGrams != null && newGrams > 0) {
                                    showEditModal = false
                                    coroutineScope.launch {
                                        dismissState.reset()
                                    }
                                    onEditConfirm(newGrams)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B9DC3),
                                contentColor = Color.White
                            ),
                            enabled = !isUpdating && editGramsInput.toDoubleOrNull()?.let { it > 0 } == true
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    if (isUpdating) "Guardando..." else "Guardar",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = { },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
