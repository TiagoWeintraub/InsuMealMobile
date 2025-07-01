package com.insumeal.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.insumeal.utils.TokenManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insumeal.ui.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch // Para controlar el drawer
import com.insumeal.R

// Data class para los ítems del Navigation Drawer
data class DrawerItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Necesario si no usas el padding que provee Scaffold
@OptIn(ExperimentalMaterial3Api::class) // Para TopAppBar y NavigationDrawer
@Composable
fun HomeScreen(navController: NavController, context: Context) {    val tokenManager = remember { TokenManager(context) } // Usar remember para TokenManager
    val token = tokenManager.getToken()
    val userProfileViewModel = remember { UserProfileViewModel() }
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    // Para controlar el estado del Navigation Drawer (abierto/cerrado)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope() // CoroutineScope para abrir/cerrar el drawer

    // Redirigir a login si no hay token
    LaunchedEffect(token) { // Observar cambios en el token también
        if (token == null) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true // Evita múltiples instancias de login
            }
        }
    }

    // Si no hay token, podríamos mostrar un loading o nada hasta que la navegación ocurra
    if (token == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // O un Composable vacío
        }
        return // Salir temprano si no hay token
    }

    // Si el perfil es nulo, intentamos cargarlo
    LaunchedEffect(Unit) {
        if (userProfile == null) {
            val savedUserId = tokenManager.getUserId()
            if (savedUserId != null) {
                val authHeader = "Bearer $token"
                userProfileViewModel.loadUserProfile(authHeader, savedUserId)
            }
        }
    }

    // Lista de ítems para el Navigation Drawer
    val drawerItems = listOf(
        DrawerItem("profile", "Perfil", Icons.Filled.AccountCircle),
        // Puedes añadir más items aquí
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Insumeal Menú",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = false, // Puedes manejar la selección si es necesario
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.route == "profile") {
                                val userId = tokenManager.getUserId()
                                if (userId != null) {
                                    navController.navigate("profile/$userId") {
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                // Ítem de Cerrar Sesión separado
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión") },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        tokenManager.clearToken()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {}, // Sin texto al lado del menú
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Abrir menú",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header con gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(16.dp))                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,  // Celeste
                                        MaterialTheme.colorScheme.secondary // Verde
                                    )
                                )
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo de la app - más grande y sin card
                            Image(
                                painter = painterResource(id = R.drawable.logo_insumeal),
                                contentDescription = "Logo de Insumeal",
                                modifier = Modifier
                                    .size(120.dp) // Tamaño mucho más grande
                                    .padding(bottom = 16.dp), // Padding solo en la parte inferior
                                contentScale = ContentScale.Fit // Mantener proporciones
                            )

                            Text(
                                text = "InsuMeal",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (userProfile != null) "¡Hola ${userProfile!!.username}!" else "¡Bienvenido --!",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sección de funcionalidades
                    Text(
                        text = "¿Qué quieres hacer hoy?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 16.dp, start = 8.dp)
                    )

                    // Tarjetas de opciones
                    HomeOptionCard(
                        title = "Analizar Plato de Comida",
                        description = "Conocé los carbohidratos e insulina sugerida con una foto de tu comida",
                        icon = Icons.Filled.PhotoCamera,
                        onClick = { navController.navigate("uploadPhoto") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HomeOptionCard(
                        title = "Ver Historial",
                        description = "Consulta tus platos anteriores",
                        icon = Icons.Filled.History,
                        onClick = { navController.navigate("loadingHistory") }, // Cambiar a loadingHistory
                        showArrow = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeOptionCard(
                        title = "Información Clínica",
                        description = "Consulta o modifica tus datos médicos",
                        icon = Icons.Filled.Info,
                        onClick = {
                            val userId = tokenManager.getUserId()
                            if (userId != null) {
                                navController.navigate("clinicalData/$userId")
                            }
                        },
                        showArrow = false
                    )
                }
            }
        }
    }
}

// Composable auxiliar para botones con icono (opcional, para un look más consistente)
@Composable
fun ButtonWithIcon(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = colors
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // El texto del botón sirve como descripción
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

// Componente para las tarjetas de opciones en el Home
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    showArrow: Boolean = true // Parámetro para mostrar/ocultar la flecha
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo para el icono
            Box(                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texto de la tarjeta
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Flecha de navegación (opcional)
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
