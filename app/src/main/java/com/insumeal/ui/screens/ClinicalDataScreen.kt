package com.insumeal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.insumeal.ui.theme.Turquoise500
import com.insumeal.ui.theme.Turquoise600
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

    // Estado de scroll
    val scrollState = rememberScrollState()

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

    Scaffold(
        containerColor = Color(0xFFF7FAFC)
    ) { paddingValues ->
        // TODO EL CONTENIDO AHORA ES SCROLLABLE, INCLUYENDO EL HEADER
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // SCROLL APLICADO A TODO EL CONTENIDO
                .background(Color(0xFFF7FAFC))
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
                                text = "Información Clínica",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Contenido principal - AHORA DENTRO DEL SCROLL GENERAL
            if (clinicalData != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // Sección Sensibilidad a la Insulina
                    ParameterSection(
                        title = "Sensibilidad a la Insulina",
                        description = "Indica cuántos mg/dL disminuye la glucemia con una unidad de insulina rápida",
                        icon = Icons.Default.MonitorHeart,
                        iconColor = Color(0xFF38A169),
                        value = clinicalData!!.sensitivity.toInt().toString(),
                        unit = "mg/dl por unidad",
                        fieldName = "sensitivity",
                        editingField = editingField,
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
                        onValueChange = { editValue = it },
                        onUpdateField = { fieldName, newValue ->
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
                                    onSuccess = { isUpdating = false },
                                    onError = { error ->
                                        isUpdating = false
                                        updateError = error
                                    }
                                )
                            }
                        }
                    )

                    // Sección Ratio Insulina/Carbohidratos
                    ParameterSection(
                        title = "Ratio Insulina/Carbohidratos",
                        description = "Cantidad de gramos de carbohidratos que cubre una unidad de insulina rápida",
                        icon = Icons.Default.Scale,
                        iconColor = Color(0xFF4299E1),
                        value = clinicalData!!.ratio.toInt().toString(),
                        unit = "gramos por unidad",
                        fieldName = "ratio",
                        editingField = editingField,
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
                        onValueChange = { editValue = it },
                        onUpdateField = { fieldName, newValue ->
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
                                    onSuccess = { isUpdating = false },
                                    onError = { error ->
                                        isUpdating = false
                                        updateError = error
                                    }
                                )
                            }
                        }
                    )

                    // Sección Objetivo de Glucemia - ASEGURÁNDONOS QUE ESTÉ COMPLETAMENTE VISIBLE
                    ParameterSection(
                        title = "Objetivo de Glucemia",
                        description = "Nivel de glucosa en sangre que deseas mantener como objetivo",
                        icon = Icons.Default.Favorite,
                        iconColor = Color(0xFFE53E3E),
                        value = (clinicalData?.glycemiaTarget?.toInt() ?: 100).toString(),
                        unit = "mg/dl",
                        fieldName = "glycemiaTarget",
                        editingField = editingField,
                        editValue = editValue,
                        isUpdating = isUpdating,
                        onEditStart = {
                            editingField = "glycemiaTarget"
                            editValue = (clinicalData?.glycemiaTarget?.toInt() ?: 100).toString()
                        },
                        onEditCancel = {
                            editingField = null
                            editValue = ""
                            updateError = null
                        },
                        onValueChange = { editValue = it },
                        onUpdateField = { fieldName, newValue ->
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
                                    ratio = clinicalData?.ratio ?: 15.0,
                                    sensitivity = clinicalData?.sensitivity ?: 50.0,
                                    glycemiaTarget = newValue.toDouble(),
                                    onSuccess = { isUpdating = false },
                                    onError = { error ->
                                        isUpdating = false
                                        updateError = error
                                    }
                                )
                            }
                        }
                    )

                    // Mostrar error si existe
                    updateError?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFED7D7)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE53E3E),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE53E3E)
                                )
                            }
                        }
                    }

                    // ESPACIADO EXTRA AL FINAL PARA ASEGURAR QUE TODO SEA VISIBLE
                    Spacer(modifier = Modifier.height(80.dp))
                }
            } else {
                // Estado de carga
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF6B35),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando datos clínicos...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSection(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    unit: String,
    fieldName: String,
    editingField: String?,
    editValue: String,
    isUpdating: Boolean,
    onEditStart: () -> Unit,
    onEditCancel: () -> Unit,
    onValueChange: (String) -> Unit,
    onUpdateField: (String, Int) -> Unit
) {
    // Estados para el modal de edición
    var showEditModal by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe de derecha a izquierda -> Editar
                    showEditModal = true
                    false // No dismissamos automáticamente, mostramos el modal
                }
                else -> false
            }
        },
        // Configurar el threshold para ser más sensible al swipe
        positionalThreshold = { totalDistance -> totalDistance * 0.25f }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header de la sección
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            iconColor.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF2D3748)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Row que contiene la card del parámetro y la lengüeta de editar con swipe
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SwipeToDismissBox con la card del parámetro
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
                                        .background(iconColor),
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
                    // Card de valor editable con bordes rectos en el lado derecho
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            bottomStart = 12.dp,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        ) // Solo bordes izquierdos redondeados
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$value $unit",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Lengüeta de editar que sobresale del lado derecho (AHORA CLICKEABLE COMO BACKUP)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .fillMaxHeight()
                ) {
                    // Sombra desplazada hacia la derecha
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = 2.dp, y = 2.dp)
                            .background(
                                Color.Black.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    bottomStart = 0.dp,
                                    topEnd = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            )
                    )

                    // Lengüeta principal (AHORA CLICKEABLE PARA BACKUP)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                iconColor, // Usar el color temático del parámetro
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    bottomStart = 0.dp,
                                    topEnd = 12.dp,
                                    bottomEnd = 12.dp
                                )
                            )
                            .clickable {
                                // BACKUP: Si el swipe no funciona, permitir click en la lengüeta
                                showEditModal = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUpdating && editingField == fieldName) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de edición estético (activado por swipe)
    if (showEditModal) {
        // Actualizar el valor cada vez que se abre el modal
        LaunchedEffect(showEditModal) {
            if (showEditModal) {
                onEditStart()
            }
        }

        EditParameterDialog(
            title = title,
            currentValue = value,
            unit = unit,
            fieldName = fieldName,
            icon = icon,
            iconColor = iconColor,
            onDismiss = {
                showEditModal = false
                onEditCancel()
                coroutineScope.launch {
                    dismissState.reset()
                }
            },
            onConfirm = { newValue ->
                onUpdateField(fieldName, newValue)
                showEditModal = false
                onEditCancel()
                coroutineScope.launch {
                    dismissState.reset()
                }
            }
        )
    }
}

@Composable
fun EditParameterDialog(
    title: String,
    currentValue: String,
    unit: String,
    fieldName: String,
    icon: ImageVector,
    iconColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var editValue by remember { mutableStateOf(currentValue) }

    val (minValue, maxValue) = when (fieldName) {
        "sensitivity" -> Pair(10f, 100f)
        "ratio" -> Pair(5f, 30f)
        "glycemiaTarget" -> Pair(70f, 180f)
        else -> Pair(0f, 100f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        iconColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
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
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentValueFloat = editValue.toFloatOrNull() ?: currentValue.toFloat()

                Text(
                    text = "Valor: ${currentValueFloat.toInt()} $unit",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = iconColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = currentValueFloat,
                    onValueChange = { newValue ->
                        editValue = newValue.toInt().toString()
                    },
                    valueRange = minValue..maxValue,
                    colors = SliderDefaults.colors(
                        thumbColor = iconColor,
                        activeTrackColor = iconColor,
                        inactiveTrackColor = iconColor.copy(alpha = 0.3f)
                    )
                )

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

                Spacer(modifier = Modifier.height(16.dp))

                val descriptionText = when (fieldName) {
                    "sensitivity" -> "Indica cuánto disminuye la glucemia con una unidad de insulina"
                    "ratio" -> "Gramos de carbohidratos que cubre una unidad de insulina"
                    "glycemiaTarget" -> "Valor objetivo de glucemia que deseas mantener"
                    else -> ""
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    editValue.toIntOrNull()?.let { newValue ->
                        onConfirm(newValue)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColor
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
