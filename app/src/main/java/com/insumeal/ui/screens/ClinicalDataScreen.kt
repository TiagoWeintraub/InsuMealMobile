package com.insumeal.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.insumeal.ui.viewmodel.ClinicalDataViewModel
import com.insumeal.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalDataScreen(userId: Int = 1, navController: NavController) {
    val context = LocalContext.current
    val clinicalDataViewModel: ClinicalDataViewModel = viewModel()
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
                ) {                    EditableClinicalDataCard(
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
                        },
                        onValueChange = { editValue = it },                        onEditConfirm = {
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
                        },
                        onValueChange = { editValue = it },                        onEditConfirm = {
                            val newValue = editValue.toIntOrNull()
                            when {
                                newValue == null -> {
                                    updateError = "Por favor, ingresa un número válido"
                                }
                                newValue < 5 || newValue > 50 -> {
                                    updateError = "El ratio debe estar entre 5 y 50"
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
                        isUpdating = isUpdating,                        onEditStart = { 
                            editingField = "glycemiaTarget"
                            editValue = clinicalData!!.glycemiaTarget.toInt().toString()
                        },
                        onEditCancel = { 
                            editingField = null
                            editValue = ""
                            updateError = null
                        },
                        onValueChange = { editValue = it },                        onEditConfirm = {
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
    onEditConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) 6.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        if (isEditing) {
            // Modo edición
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {                        OutlinedTextField(
                            value = editValue,
                            onValueChange = onValueChange,
                            label = { Text("Valor") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !isUpdating,
                            supportingText = {
                                val rangeText = when (fieldName) {
                                    "sensitivity" -> "Rango permitido: 10 - 100"
                                    "ratio" -> "Rango permitido: 5 - 50"
                                    "glycemiaTarget" -> "Rango permitido: 90 - 120"
                                    else -> ""
                                }
                                Text(rangeText)
                            }
                        )
                        
                        Text(
                            text = unit,
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
                            enabled = !isUpdating && editValue.isNotBlank()
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                IconButton(onClick = onEditStart) {
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
