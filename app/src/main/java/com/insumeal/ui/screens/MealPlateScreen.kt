package com.insumeal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.insumeal.models.Ingredient
import com.insumeal.ui.theme.Gray400
import com.insumeal.ui.viewmodel.MealPlateViewModel

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
                }
                SwipeToDismissBoxValue.EndToStart -> {
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
            modifier = Modifier
                .weight(1f)
                .zIndex(1f),
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
                    SwipeToDismissBoxValue.EndToStart -> {
                        // Fondo azul suave para editar (swipe hacia la izquierda)
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
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp) // Bordes rectos sin redondear
            ) {
                // Solo modo visualización estática, la edición se hace únicamente mediante swipe
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
                            text = "${String.format("%.0f", ingredient.grams)}g",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                        Text(
                            text = "${String.format("%.1f", ingredient.carbs)}g de HC",
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
                                    modifier = Modifier.size(13.dp)
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
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Eliminar",
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

    // Modal de edición estético y moderno
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
                // Icono moderno con gradiente azul (color de lengüeta de editar)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF6B9DC3).copy(alpha = 0.15f),
                                    Color(0xFF6B9DC3).copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Color(0xFF6B9DC3).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF6B9DC3),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Editar cantidad",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center, // COlor Negro
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6B9DC3),
                        modifier = Modifier
                            .background(
                                Color(0xFF6B9DC3).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    .padding(horizontal = 16.dp, vertical = 4.dp))
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Destacar la cantidad actual sin card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF6B9DC3).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Cantidad actual",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${String.format("%.0f", ingredient.grams)} gramos",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF6B9DC3)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de entrada moderno con colores azules
                    OutlinedTextField(
                        value = editGramsInput,
                        onValueChange = { newValue ->
                            // Solo permitir números y un punto decimal
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                editGramsInput = newValue
                            }
                        },
                        label = {
                            Text(
                                "Nueva cantidad en gramos",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        placeholder = {
                            Text(
                                "Ej: 150",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6B9DC3),
                            focusedLabelColor = Color(0xFF6B9DC3),
                            unfocusedBorderColor = Color(0xFF6B9DC3).copy(alpha = 0.6f),
                            cursorColor = Color(0xFF6B9DC3),
                            focusedContainerColor = Color.White, // Fondo blanco
                            unfocusedContainerColor = Color.White, // Fondo blanco
                            focusedTextColor = Color.Black, // Texto negro
                            unfocusedTextColor = Color.Black, // Texto negro
                            focusedPlaceholderColor = Gray400,
                            unfocusedPlaceholderColor = Gray400
                        ),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = Color(0xFF6B9DC3),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Vista previa de carbohidratos sin card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF6B9DC3).copy(alpha = 0.05f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val previewCarbs = editGramsInput.toDoubleOrNull()?.let { grams ->
                            (grams / 100.0) * ingredient.carbsPerHundredGrams
                        } ?: ingredient.carbs

                        // Header de la preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF6B9DC3).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color(0xFF6B9DC3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Carbohidratos calculados",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Valor de carbohidratos destacado con gradiente azul
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6B9DC3).copy(alpha = 0.15f),
                                            Color(0xFF6B9DC3).copy(alpha = 0.25f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${String.format("%.1f", previewCarbs)} g de HC",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF6B9DC3),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones modernos - Cancelar en naranja, Guardar en azul
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón Cancelar moderno con colores naranjas
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
                                Color(0xFFFF6B35).copy(alpha = 0.8f)
                            ),
                            enabled = !isUpdating
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = Color(0xFFFF6B35)
                                )
                                Text(
                                    "Cancelar",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFFFF6B35),
                                    maxLines = 1
                                )
                            }
                        }

                        // Botón Guardar moderno con color azul claro uniforme
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
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isUpdating && editGramsInput.toDoubleOrNull()?.let { it > 0 } == true) {
                                    Color(0xFF6B9DC3)
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.outline,
                                disabledContentColor = Color.White.copy(alpha = 0.6f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 2.dp
                            ),
                            enabled = !isUpdating && editGramsInput.toDoubleOrNull()?.let { it > 0 } == true
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
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
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 8.dp
        )
    }
}

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

    // Estados para agregar alimentos
    var showAddFoodModal by remember { mutableStateOf(false) }
    var foodNameInput by remember { mutableStateOf("") }
    var isAddingFood by remember { mutableStateOf(false) }
    var addFoodError by remember { mutableStateOf<String?>(null) }

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
        // Eliminar el topBar para que la imagen llegue hasta arriba
    ) { paddingValues ->
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
                        if (mealPlateViewModel.bitmap != null) {
                            Image(
                                bitmap = mealPlateViewModel.bitmap!!.asImageBitmap(),
                                contentDescription = "Imagen del plato",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (mealPlateViewModel.imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = mealPlateViewModel.imageUri),
                                contentDescription = "Imagen del plato",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Imagen placeholder si no hay imagen
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Imagen del plato",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

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
                            containerColor = Color(0xFFFFFFFF) // Blanco puro
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Sección de ingredientes
                            item {
                                Text(
                                    text = "Ingredientes detectados",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp // Tamaño más pequeño
                                    ),
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            // Lista de ingredientes con capacidad de edición
                            items(currentMealPlate.ingredients) { ingredient ->
                                IngredientEditableCard(
                                    ingredient = ingredient,
                                    isEditing = false,
                                    editGrams = "",
                                    isUpdating = isUpdating && editingIngredientId == ingredient.id,
                                    isLastIngredient = currentMealPlate.ingredients.size == 1,
                                    onEditStart = { },
                                    onEditCancel = { },
                                    onGramsChange = { },
                                    onEditConfirm = { newGramsValue: Double? ->
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
                                    },
                                    onDeleteIngredient = {
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

                            // Botón para agregar alimento (sin Card)
                            item {
                                Button(
                                    onClick = {
                                        showAddFoodModal = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Agregar Alimento",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            // Campo para ingresar glucemia y botón para calcular dosis
                            item {
                                // Tarjeta para el cálculo de dosis
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFFFFF) // Blanco puro
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bloodtype,
                                                    contentDescription = null,
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = "Glucosa en Sangre",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp // Tamaño más pequeño
                                                    ),
                                                    color = Color.Black,
                                                )
                                                Text(
                                                    text = "Ingresa tu glucemia actual para un cálculo preciso en la dosis",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

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
                                            },
                                            enabled = glycemiaInput.isNotBlank() && !isCalculatingDosis &&
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
                            }

                            // Espacio al final
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }

                    // Botón de volver atrás flotando sobre la imagen con estética consistente
                    IconButton(
                        onClick = {
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
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
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

    // Diálogo para agregar alimento
    if (showAddFoodModal) {
        val currentMealPlate = mealPlate // Capturar el meal plate actual
        if (currentMealPlate != null) {
            AlertDialog(
                onDismissRequest = {
                    showAddFoodModal = false
                    foodNameInput = ""
                    addFoodError = null
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Agregar Alimento",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ingrese el nombre del alimento",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Las cantidades se van a poder editar una vez se haya agregado el alimento.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = foodNameInput,
                            onValueChange = {
                                foodNameInput = it
                                // Limpiar error cuando el usuario comience a escribir
                                if (addFoodError != null) {
                                    addFoodError = null
                                }
                            },
                            label = { Text("Nombre del alimento") },
                            placeholder = { Text("Ej: ketchup") },
                            singleLine = true,
                            enabled = !isAddingFood,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                                focusedContainerColor = Color.White, // Fondo blanco
                                unfocusedContainerColor = Color.White, // Fondo blanco
                                focusedTextColor = Color.Black, // Texto negro
                                unfocusedTextColor = Color.Black, // Texto negro
                                focusedPlaceholderColor = Gray400,
                                unfocusedPlaceholderColor = Gray400
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )

                        // Mostrar error al agregar alimento si lo hay
                        if (addFoodError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = addFoodError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
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
                                    showAddFoodModal = false
                                    foodNameInput = ""
                                    addFoodError = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline
                                ),
                                enabled = !isAddingFood
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Cancelar",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // Botón Agregar
                            Button(
                                onClick = {
                                    if (foodNameInput.isNotBlank()) {
                                        isAddingFood = true
                                        addFoodError = null

                                        mealPlateViewModel.addFoodToMealPlate(
                                            context = context,
                                            mealPlateId = currentMealPlate.id,
                                            foodName = foodNameInput,
                                            onSuccess = {
                                                isAddingFood = false
                                                showAddFoodModal = false
                                                foodNameInput = ""
                                                addFoodError = null
                                            },
                                            onError = { error ->
                                                isAddingFood = false
                                                addFoodError = error
                                            }
                                        )
                                    } else {
                                        addFoodError = "El nombre del alimento no puede estar vacío"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                enabled = foodNameInput.isNotBlank() && !isAddingFood
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isAddingFood) {
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
                                        if (isAddingFood) "Agregar" else "Agregar",
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
}
