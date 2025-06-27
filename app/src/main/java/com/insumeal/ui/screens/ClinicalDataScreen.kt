package com.insumeal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.insumeal.ui.viewmodel.ClinicalDataViewModel
import com.insumeal.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalDataScreen(userId: Int = 1, navController: NavController) {
    val context = LocalContext.current
    val clinicalDataViewModel = remember { ClinicalDataViewModel() }
    val clinicalData by clinicalDataViewModel.clinicalData.collectAsState()

    // Estados para la edición
    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }

    // Si los datos clínicos son nulos, intentamos cargarlos
    LaunchedEffect(Unit) {
        if (clinicalData == null) {
            val tokenManager = TokenManager(context)
            val token = tokenManager.getToken()
            val savedUserId = tokenManager.getUserId() ?: userId.toString()

            if (token != null) {
                val authHeader = "Bearer $token"
                clinicalDataViewModel.loadClinicalData(authHeader, savedUserId)
            }
        }
    }

    // UI para mostrar los datos clínicos
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos Clínicos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (clinicalData != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {                    
                    EditableClinicalDataCard(
                        title = "Sensibilidad a la Insulina",
                        value = clinicalData!!.sensitivity.toInt().toString(),
                        unit = "mg/dl por unidad",
                        description = "La sensibilidad indica cuántos mg/dL disminuye la glucemia con una unidad de insulina rápida",
                        icon = Icons.Default.MonitorHeart,
                        fieldName = "sensitivity",
                        isEditing = editingField == "sensitivity",
                        editValue = editValue,
                        isUpdating = isUpdating,
                        onEditStart = { 
                            editingField = "sensitivity"
                            editValue = clinicalData!!.sensitivity.toInt().toString()
                        },
                        onEditCancel = { 
                            editingField = null
                            editValue = ""
                            updateError = null
                        },                        onValueChange = { editValue = it },
                        onEditConfirm = {
                            val newValue = editValue.toIntOrNull()
                            when {
                                newValue == null -> {
                                    updateError = "Por favor, ingresa un número válido"
                                }
                                newValue < 10 || newValue > 100 -> {
                                    updateError = "La sensibilidad debe estar entre 10 y 100"
                                }
                                else -> {
                                    isUpdating = true
                                    updateError = null
                                    
                                    val tokenManager = TokenManager(context)
                                    val token = tokenManager.getToken()
                                    val savedUserId = tokenManager.getUserId() ?: userId.toString()
                                    
                                    if (token != null) {
                                        val authHeader = "Bearer $token"
                                        clinicalDataViewModel.updateClinicalData(
                                            authHeader = authHeader,
                                            userId = savedUserId,
                                            ratio = clinicalData!!.ratio,
                                            sensitivity = newValue.toDouble(),
                                            glycemiaTarget = clinicalData!!.glycemiaTarget,
                                            onSuccess = {
                                                isUpdating = false
                                                editingField = null
                                                editValue = ""
                                            },
                                            onError = { error ->
                                                isUpdating = false
                                                updateError = error
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        onUpdateField = { fieldName, newValue ->
                            isUpdating = true
                            updateError = null
                            
                            val tokenManager = TokenManager(context)
                            val token = tokenManager.getToken()
                            val savedUserId = tokenManager.getUserId() ?: userId.toString()
                            
                            if (token != null) {
                                val authHeader = "Bearer $token"
                                when (fieldName) {
                                    "sensitivity" -> {
                                        clinicalDataViewModel.updateClinicalData(
                                            authHeader = authHeader,
                                            userId = savedUserId,
                                            ratio = clinicalData!!.ratio,
                                            sensitivity = newValue.toDouble(),
                                            glycemiaTarget = clinicalData!!.glycemiaTarget,
                                            onSuccess = {
                                                isUpdating = false
                                            },
                                            onError = { error ->
                                                isUpdating = false
                                                updateError = error
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                    EditableClinicalDataCard(
                        title = "Ratio Insulina/Carbohidratos",
                        value = clinicalData!!.ratio.toInt().toString(),
                        unit = "gramos por unidad",
                        description = "El ratio es la cantidad de gramos de carbohidratos que cubre una unidad de insulina rápida",
                        icon = Icons.Default.Scale,
                        fieldName = "ratio",
                        isEditing = editingField == "ratio",
                        editValue = editValue,
                        isUpdating = isUpdating,
                        onEditStart = { 
                            editingField = "ratio"
                            editValue = clinicalData!!.ratio.toInt().toString()
                        },
                        onEditCancel = { 
                            editingField = null
                            editValue = ""
                            updateError = null
                        },                        onValueChange = { editValue = it },
                        onEditConfirm = {
                            val newValue = editValue.toIntOrNull()
                            when {
                                newValue == null -> {
                                    updateError = "Por favor, ingresa un número válido"
                                }
                                newValue < 5 || newValue > 30 -> {
                                    updateError = "El ratio debe estar entre 5 y 30"
                                }
                                else -> {
                                    isUpdating = true
                                    updateError = null
                                    
                                    val tokenManager = TokenManager(context)
                                    val token = tokenManager.getToken()
                                    val savedUserId = tokenManager.getUserId() ?: userId.toString()
                                    
                                    if (token != null) {
                                        val authHeader = "Bearer $token"
                                        clinicalDataViewModel.updateClinicalData(
                                            authHeader = authHeader,
                                            userId = savedUserId,
                                            ratio = newValue.toDouble(),
                                            sensitivity = clinicalData!!.sensitivity,
                                            glycemiaTarget = clinicalData!!.glycemiaTarget,
                                            onSuccess = {
                                                isUpdating = false
                                                editingField = null
                                                editValue = ""
                                            },
                                            onError = { error ->
                                                isUpdating = false
                                                updateError = error
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        onUpdateField = { fieldName, newValue ->
                            isUpdating = true
                            updateError = null
                            
                            val tokenManager = TokenManager(context)
                            val token = tokenManager.getToken()
                            val savedUserId = tokenManager.getUserId() ?: userId.toString()
                            
                            if (token != null) {
                                val authHeader = "Bearer $token"
                                when (fieldName) {
                                    "ratio" -> {
                                        clinicalDataViewModel.updateClinicalData(
                                            authHeader = authHeader,
                                            userId = savedUserId,
                                            ratio = newValue.toDouble(),
                                            sensitivity = clinicalData!!.sensitivity,
                                            glycemiaTarget = clinicalData!!.glycemiaTarget,
                                            onSuccess = {
                                                isUpdating = false
                                            },
                                            onError = { error ->
                                                isUpdating = false
                                                updateError = error
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                    EditableClinicalDataCard(
                        title = "Objetivo de Glucemia",
                        value = clinicalData!!.glycemiaTarget.toInt().toString(),
                        unit = "mg/dl",
                        description = "El target de glucemia es el valor objetivo al cual deseas mantener tu nivel de azúcar en sangre",
                        icon = Icons.Filled.TrackChanges,
                        fieldName = "glycemiaTarget",
                        isEditing = editingField == "glycemiaTarget",
                        editValue = editValue,
                        isUpdating = isUpdating,                        
                        onEditStart = { 
                            editingField = "glycemiaTarget"
                            editValue = clinicalData!!.glycemiaTarget.toInt().toString()
                        },
                        onEditCancel = { 
                            editingField = null
                            editValue = ""
                            updateError = null
                        },                        onValueChange = { editValue = it },                        
                        onEditConfirm = {
                            val newValue = editValue.toIntOrNull()
                            when {
                                newValue == null -> {
                                    updateError = "Por favor, ingresa un número válido"
                                }
                                newValue < 90 || newValue > 120 -> {
                                    updateError = "El objetivo de glucemia debe estar entre 90 y 120"
                                }
                                else -> {
                                    isUpdating = true
                                    updateError = null
                                    
                                    val tokenManager = TokenManager(context)
                                    val token = tokenManager.getToken()
                                    val savedUserId = tokenManager.getUserId() ?: userId.toString()
                                    
                                    if (token != null) {
                                        val authHeader = "Bearer $token"
                                        clinicalDataViewModel.updateClinicalData(
                                            authHeader = authHeader,
                                            userId = savedUserId,
                                            ratio = clinicalData!!.ratio,
                                            sensitivity = clinicalData!!.sensitivity,
                                            glycemiaTarget = newValue.toDouble(),
                                            onSuccess = {
                                                isUpdating = false
                                                editingField = null
                                                editValue = ""
                                            },
                                            onError = { error ->
                                                isUpdating = false
                                                updateError = error
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        onUpdateField = { fieldName, newValue ->
                            isUpdating = true
                            updateError = null
                            
                            val tokenManager = TokenManager(context)
                            val token = tokenManager.getToken()
                            val savedUserId = tokenManager.getUserId() ?: userId.toString()
                            
                            if (token != null) {
                                val authHeader = "Bearer $token"
                                when (fieldName) {
                                    "glycemiaTarget" -> {
                                        clinicalDataViewModel.updateClinicalData(
                                            authHeader = authHeader,
                                            userId = savedUserId,
                                            ratio = clinicalData!!.ratio,
                                            sensitivity = clinicalData!!.sensitivity,
                                            glycemiaTarget = newValue.toDouble(),
                                            onSuccess = {
                                                isUpdating = false
                                            },
                                            onError = { error ->
                                                isUpdating = false
                                                updateError = error
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando datos clínicos...",
                        style = MaterialTheme.typography.bodyLarge
                    )
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
fun ClinicalDataCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableClinicalDataCard(
    title: String,
    value: String,
    unit: String,
    description: String,
    icon: ImageVector,
    fieldName: String,
    isEditing: Boolean,
    editValue: String,
    isUpdating: Boolean,
    onEditStart: () -> Unit,
    onEditCancel: () -> Unit,
    onValueChange: (String) -> Unit,
    onEditConfirm: () -> Unit,
    onUpdateField: (String, Int) -> Unit // Nueva función para manejar la actualización
) {
    // Estados para el modal de edición
    var showEditModal by remember { mutableStateOf(false) }
    var modalEditValue by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe de derecha a izquierda -> Editar
                    modalEditValue = value
                    showEditModal = true
                    false // No dismissamos automáticamente, mostramos el modal
                }
                else -> false
            }
        },
        // Configurar el threshold para requerir más distancia de deslizamiento
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )    // Row que contiene la card del dato clínico y la lengüeta de editar con sombra
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // SwipeToDismissBox con la card del dato clínico
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.weight(1f),
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                // Fondo dinámico dependiendo de la dirección del swipe
                val direction = dismissState.dismissDirection
                when (direction) {
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
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    bottomStart = 8.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ) // Solo bordes izquierdos redondeados
            ) {
                // Solo modo visualización estática, la edición se hace únicamente mediante swipe
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$value $unit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }        // Lengüeta de editar que sobresale del lado derecho con sombra solo externa
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight()
        ) {
            // Sombra personalizada desplazada hacia la derecha
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 2.dp, y = 2.dp)
                    .background(
                        Color.Black.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = 8.dp,
                            bottomEnd = 8.dp
                        )
                    )
            )
            
            // Lengüeta principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
    }

    // Modal de edición estético
    if (showEditModal) {
        // Actualizar el valor cada vez que se abre el modal
        LaunchedEffect(showEditModal) {
            if (showEditModal) {
                modalEditValue = value
            }
        }
        
        AlertDialog(
            onDismissRequest = { 
                showEditModal = false
                modalEditValue = ""
                coroutineScope.launch {
                    dismissState.reset()
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color(0xFF6B9DC3).copy(alpha = 0.2f), // Azul suave
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF6B9DC3),
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Editar $title",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Slider con valores según el tipo de campo
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val (minValue, maxValue, currentValue) = when (fieldName) {
                            "sensitivity" -> Triple(10f, 100f, modalEditValue.toFloatOrNull() ?: value.toFloat())
                            "ratio" -> Triple(5f, 30f, modalEditValue.toFloatOrNull() ?: value.toFloat())
                            "glycemiaTarget" -> Triple(90f, 120f, modalEditValue.toFloatOrNull() ?: value.toFloat())
                            else -> Triple(0f, 100f, modalEditValue.toFloatOrNull() ?: 0f)
                        }
                        
                        Text(
                            text = "Valor: ${currentValue.toInt()} $unit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF6B9DC3),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Slider(
                            value = currentValue,
                            onValueChange = { newValue ->
                                modalEditValue = newValue.toInt().toString()
                            },
                            valueRange = minValue..maxValue,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6B9DC3),
                                activeTrackColor = Color(0xFF6B9DC3),
                                inactiveTrackColor = Color(0xFF6B9DC3).copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Mostrar rango permitido
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${minValue.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${maxValue.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Descripción del campo
                        val descriptionText = when (fieldName) {
                            "sensitivity" -> "Indica cuánto disminuye la glucemia con una unidad de insulina rápida"
                            "ratio" -> "Es la cantidad de gramos de carbohidratos que cubre una unidad de insulina rápida"
                            "glycemiaTarget" -> "Es el valor objetivo al cual deseas mantener tu nivel de glucemia"
                            else -> ""
                        }
                        
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                                showEditModal = false
                                modalEditValue = ""
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
                        }                        // Botón Guardar
                        Button(
                            onClick = {
                                val newValue = modalEditValue.toIntOrNull()
                                when (fieldName) {
                                    "sensitivity" -> {
                                        if (newValue != null && newValue >= 10 && newValue <= 100) {
                                            onUpdateField(fieldName, newValue)
                                            showEditModal = false
                                            modalEditValue = ""
                                            coroutineScope.launch {
                                                dismissState.reset()
                                            }
                                        }
                                    }
                                    "ratio" -> {
                                        if (newValue != null && newValue >= 5 && newValue <= 30) {
                                            onUpdateField(fieldName, newValue)
                                            showEditModal = false
                                            modalEditValue = ""
                                            coroutineScope.launch {
                                                dismissState.reset()
                                            }
                                        }
                                    }
                                    "glycemiaTarget" -> {
                                        if (newValue != null && newValue >= 90 && newValue <= 120) {
                                            onUpdateField(fieldName, newValue)
                                            showEditModal = false
                                            modalEditValue = ""
                                            coroutineScope.launch {
                                                dismissState.reset()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B9DC3),
                                contentColor = Color.White
                            ),
                            enabled = !isUpdating && modalEditValue.isNotBlank()
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
