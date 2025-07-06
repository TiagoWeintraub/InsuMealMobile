package com.insumeal.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.os.ConfigurationCompat
import com.insumeal.ui.theme.Turquoise300
import com.insumeal.ui.theme.Turquoise500
import com.insumeal.ui.theme.Turquoise600
import com.insumeal.ui.viewmodel.MealPlateHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodHistoryScreen(
    navController: NavController,
    viewModel: MealPlateHistoryViewModel? = null
) {
    val context = LocalContext.current
    val historyViewModel = remember { MealPlateHistoryViewModel() }

    val historyList by historyViewModel.historyList.collectAsState()
    val isLoading by historyViewModel.isLoading.collectAsState()
    val errorMessage by historyViewModel.errorMessage.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var isDeletingAll by remember { mutableStateOf(false) }
    var deleteAllError by remember { mutableStateOf<String?>(null) }

    // Estados para el filtro de calendario
    var showCalendarModal by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var filteredHistoryList by remember { mutableStateOf(listOf<com.insumeal.models.MealPlateHistory>()) }

    // Filtrar la lista cuando cambie la fecha seleccionada o la lista original
    LaunchedEffect(selectedDate, historyList) {
        filteredHistoryList = if (selectedDate != null) {
            // Usar el mismo formato que la API para el filtrado
            val selectedDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDateStr = selectedDateFormat.format(selectedDate!!)

            // Debug: Imprimir fechas para diagnosticar
            android.util.Log.d("FoodHistoryFilter", "Fecha seleccionada: $selectedDateStr")
            historyList.forEach { item ->
                android.util.Log.d("FoodHistoryFilter", "Fecha en historial: ${item.date}")
            }

            // Filtrar comparando solo la parte de la fecha (sin hora)
            val filtered = historyList.filter { item ->
                val itemDate = item.date
                // Extraer solo la parte de la fecha (antes del " - ")
                val itemDateOnly = itemDate.split(" - ")[0]
                itemDateOnly == selectedDateStr
            }

            android.util.Log.d("FoodHistoryFilter", "Elementos filtrados: ${filtered.size}")
            filtered
        } else {
            historyList
        }
    }

    LaunchedEffect(Unit) {
        historyViewModel.loadHistory(context)
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header moderno con gradiente (igual que HomeScreen)
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
                            // Botón de volver atrás con el mismo estilo que HomeScreen
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

                            // Título centrado
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Historial de Comidas",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Botón de eliminar todo el historial
                            if (!isLoading && historyList.isNotEmpty()) {
                                IconButton(
                                    onClick = { showDeleteAllDialog = true },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar todo",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                // Spacer para mantener el centrado cuando no hay botón
                                Spacer(modifier = Modifier.size(44.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Contenido principal
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp) // Reducido el espacio extra
                ) {
                    when {
                        isLoading -> {
                            // Estado de carga moderno
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    // Círculo de carga con colores del tema
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(64.dp),
                                        color = Turquoise500,
                                        strokeWidth = 6.dp
                                    )

                                    Spacer(modifier = Modifier.height(32.dp))

                                    Text(
                                        text = "Cargando historial",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = Color(0xFF2D3748),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Obteniendo tus análisis anteriores...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp
                                        ),
                                        color = Color(0xFF4A5568),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        errorMessage != null -> {
                            // Estado de error moderno
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        // Icono de error
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .background(Turquoise600.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Error,
                                                contentDescription = null,
                                                tint = Turquoise600,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Text(
                                            text = "Oops! Algo salió mal",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFF2D3748),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = errorMessage!!,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Turquoise600,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                historyViewModel.clearError()
                                                historyViewModel.loadHistory(context)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Turquoise500
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Reintentar",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        !isLoading && historyList.isEmpty() -> {
                            // Estado vacío moderno
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        // Icono de historial vacío
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(Color.White,Color.White.copy(alpha = 0.1f) )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = null,
                                                tint = Turquoise300,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Text(
                                            text = "¡Aún no tienes análisis!",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp
                                            ),
                                            color = Color(0xFF2D3748),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Comienza analizando tu primera comida para ver los resultados aquí",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = 16.sp
                                            ),
                                            color = Color(0xFF4A5568),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = { navController.navigate("uploadPhoto") },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Turquoise500
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PhotoCamera,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Analizar Primera Comida",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // Lista del historial con diseño moderno
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 20.dp),
                                contentPadding = PaddingValues(bottom = 24.dp) // Agregar padding inferior para evitar que se corten las cards
                            ) {
                                items(filteredHistoryList) { historyItem ->
                                    ModernMealPlateHistoryCard(
                                        historyItem = historyItem,
                                        onViewDetails = { mealPlateId ->
                                            navController.navigate("foodHistoryMealPlate/$mealPlateId")
                                        },
                                        onDelete = { mealPlateId ->
                                            historyViewModel.deleteMealPlate(
                                                context = context,
                                                mealPlateId = mealPlateId,
                                                onSuccess = {},
                                                onError = { error -> }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card flotante de estadísticas - posicionada sobre la onda
            if (!isLoading && historyList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 100.dp), // Posicionar sobre la onda
                    contentAlignment = Alignment.TopCenter
                ) {
                    StatCardWithCalendar(
                        label = if (selectedDate != null) "Total" else "Total",
                        value = "${filteredHistoryList.size}",
                        icon = Icons.Filled.RestaurantMenu,
                        backgroundColor = Color(0xFFF7FAFF),
                        iconColor = Color(0xFF4299E1),
                        onCalendarClick = { showCalendarModal = true },
                        hasFilter = selectedDate != null,
                        selectedDate = selectedDate,
                        onClearFilter = { selectedDate = null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar todo el historial
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text("Eliminar todo el historial")
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar todo el historial de comidas? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        isDeletingAll = true
                        deleteAllError = null
                        
                        historyViewModel.deleteAllMealPlates(
                            context = context,
                            onSuccess = {
                                isDeletingAll = false
                            },
                            onError = { error ->
                                isDeletingAll = false
                                deleteAllError = error
                            }
                        )
                    }
                ) {
                    Text("Eliminar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de error para eliminar todo
    deleteAllError?.let { error ->
        AlertDialog(
            onDismissRequest = { deleteAllError = null },
            title = {
                Text("Error al eliminar historial")
            },
            text = {
                Text(error)
            },
            confirmButton = {
                TextButton(
                    onClick = { deleteAllError = null }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Modal de calendario con el estilo de la app
    if (showCalendarModal) {
        DatePickerModal(
            selectedDate = selectedDate,
            historyList = historyList,
            onDateSelected = { date ->
                selectedDate = date
                showCalendarModal = false
            },
            onDismiss = { showCalendarModal = false },
            onClearFilter = {
                selectedDate = null
                showCalendarModal = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    selectedDate: Date?,
    historyList: List<com.insumeal.models.MealPlateHistory>,
    onDateSelected: (Date?) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit
) {
    val spanishLocale = Locale("es", "ES")

    val availableDates = remember(historyList) {
        val calendarDateFormat = SimpleDateFormat("dd/MM/yyyy", spanishLocale)
        val uniqueDates = mutableSetOf<String>()

        historyList.forEach { item ->
            try {
                val dateOnly = item.date.split(" - ")[0]
                uniqueDates.add(dateOnly)
            } catch (e: Exception) {
                android.util.Log.e("DatePicker", "Error procesando fecha: ${item.date}", e)
            }
        }

        // Convertir las fechas a timestamps normalizados (sin hora)
        uniqueDates.mapNotNull { dateStr ->
            try {
                val date = calendarDateFormat.parse(dateStr)
                date?.let {
                    val calendar = Calendar.getInstance().apply {
                        time = it
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    calendar.timeInMillis
                }
            } catch (e: Exception) {
                android.util.Log.e("DatePicker", "Error parseando fecha: $dateStr", e)
                null
            }
        }.toSet()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.time ?: System.currentTimeMillis(),
        yearRange = IntRange(2020, 2030), // Rango específico de años
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Normalizar la fecha UTC a medianoche en zona horaria local
                val localCalendar = Calendar.getInstance().apply {
                    timeInMillis = utcTimeMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val normalizedMillis = localCalendar.timeInMillis

                // Verificar si esta fecha está en nuestro conjunto de fechas disponibles
                return availableDates.contains(normalizedMillis)
            }
        }
    )

    val modalContext = LocalContext.current

    val spanishContext = remember {
        val config = android.content.res.Configuration(modalContext.resources.configuration).apply {
            setLocale(spanishLocale)
            setLocales(android.os.LocaleList(spanishLocale))
        }
        modalContext.createConfigurationContext(config)
    }

    // Usar Box overlay de pantalla completa en lugar de Dialog
    CompositionLocalProvider(
        LocalContext provides spanishContext,
        LocalConfiguration provides spanishContext.resources.configuration
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)) // Fondo semi-transparente
                .clickable { onDismiss() }, // Cerrar al tocar fuera
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f) // 92% del ancho real de la pantalla
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clickable { }, // Prevenir cierre al tocar el modal
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header del modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF7FAFF))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF4299E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DatePicker ocupando todo el ancho disponible
                    MaterialTheme(
                        typography = MaterialTheme.typography.copy(
                            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    ) {
                        CompositionLocalProvider(
                            LocalTextStyle provides LocalTextStyle.current.copy(
                                lineHeight = 24.sp, // Más espaciado entre líneas
                                fontWeight = FontWeight.Bold // Hacer todas las fechas en negrita
                            )
                        ) {
                            DatePicker(
                                state = datePickerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(480.dp), // Aumentar aún más la altura
                                dateFormatter = DatePickerDefaults.dateFormatter(
                                    selectedDateSkeleton = "ddMMyyyy"
                                ),
                                showModeToggle = false, // Deshabilitar el toggle entre modo calendario y entrada de texto
                                colors = DatePickerDefaults.colors(
                                    containerColor = Color.White,
                                    titleContentColor = Color(0xFF2D3748),
                                    headlineContentColor = Color(0xFF2D3748),
                                    weekdayContentColor = Turquoise600, // Más oscuro para mejor contraste
                                    subheadContentColor = Color(0xFF4A5568),
                                    navigationContentColor = Turquoise600,
                                    yearContentColor = Color(0xFF2D3748),
                                    disabledYearContentColor = Color(0xFF9CA3AF),
                                    currentYearContentColor = Turquoise600,
                                    selectedYearContentColor = Color.White,
                                    disabledSelectedYearContentColor = Color(0xFF9CA3AF),
                                    selectedYearContainerColor = Turquoise600,
                                    disabledSelectedYearContainerColor = Color(0xFFE5E7EB),
                                    // Fechas disponibles en negrita y color más oscuro
                                    dayContentColor = Color(0xFF1A202C), // Más oscuro para fechas disponibles
                                    disabledDayContentColor = Color(0xFFCBD5E0), // Más claro para fechas no disponibles
                                    selectedDayContentColor = Color.White,
                                    disabledSelectedDayContentColor = Color(0xFF9CA3AF),
                                    selectedDayContainerColor = Turquoise600,
                                    disabledSelectedDayContainerColor = Color(0xFFE5E7EB),
                                    // Fecha actual en turquesa y negrita
                                    todayContentColor = Turquoise600,
                                    todayDateBorderColor = Turquoise600.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Indicador de fechas disponibles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${availableDates.size} fechas disponibles",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF4A5568),
                            modifier = Modifier
                                .background(
                                    Color(0xFFF7FAFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedDate != null) {
                            OutlinedButton(
                                onClick = onClearFilter,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Turquoise600),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Turquoise600
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Limpiar",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val selectedMillis = datePickerState.selectedDateMillis
                                if (selectedMillis != null) {
                                    onDateSelected(Date(selectedMillis))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Turquoise600
                            ),
                            enabled = datePickerState.selectedDateMillis != null,
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Aplicar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardWithCalendar(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onCalendarClick: () -> Unit,
    hasFilter: Boolean,
    selectedDate: Date?,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF2D3748),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF4A5568),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mostrar la fecha filtrada con botón de limpiar integrado cuando hay un filtro activo
            if (hasFilter && selectedDate != null) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate)

                // Fecha filtrada con cruz integrada - clickeable para eliminar filtro
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Turquoise600.copy(alpha = 0.1f))
                        .clickable { onClearFilter() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = Turquoise600,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = Turquoise600,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Limpiar filtro",
                        tint = Turquoise600,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            IconButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (hasFilter) Color(0xFFE6F7FF) else Color(0xFFF7FAFF)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Filtrar por fecha",
                    tint = if (hasFilter) Turquoise600 else Color(0xFF4299E1),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ModernMealPlateHistoryCard(
    historyItem: com.insumeal.models.MealPlateHistory,
    onViewDetails: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = historyItem.type.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF2D3748),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF7FAFF))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF4299E1),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = historyItem.date,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF4299E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onViewDetails(historyItem.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7FAFF))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = "Ver detalles",
                            tint = Color(0xFF4299E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF5F5))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactInfoCard(
                    label = "Carbohidratos",
                    value = "${String.format("%.1f", historyItem.totalCarbs)} g",
                    backgroundColor = Color(0xFFFF9800),
                    icon = Icons.Default.Restaurant,
                    modifier = Modifier.weight(1f)
                )

                CompactInfoCard(
                    label = "Glucemia",
                    value = "${String.format("%.0f", historyItem.glycemia)} mg/dL",
                    backgroundColor = Color(0xFFE91E63),
                    icon = Icons.Default.Bloodtype,
                    modifier = Modifier.weight(1f)
                )

                CompactInfoCard(
                    label = "Dosis Total",
                    value = "${String.format("%.1f", historyItem.dosis)} U",
                    backgroundColor = Color(0xFF2196F3),
                    icon = Icons.Default.MedicalServices,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Confirmar eliminación")
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar esta comida? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(historyItem.id)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CompactInfoCard(
    label: String,
    value: String,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = backgroundColor.copy(alpha = 0.1f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .clip(androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
