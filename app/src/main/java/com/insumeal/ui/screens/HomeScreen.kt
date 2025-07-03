package com.insumeal.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.insumeal.utils.TokenManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insumeal.ui.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch
import com.insumeal.R

// Data class para los ítems del Navigation Drawer
data class DrawerItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, context: Context) {
    val tokenManager = remember { TokenManager(context) }
    val token = tokenManager.getToken()
    val userProfileViewModel = remember { UserProfileViewModel() }
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    // Para controlar el estado del Navigation Drawer (abierto/cerrado)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                Spacer(Modifier.height(20.dp))

                // Header del drawer moderno
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Avatar del usuario
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF6B35), Color(0xFFFF8E53))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userProfile != null) userProfile!!.username.take(1).uppercase() else "U",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (userProfile != null) userProfile!!.username else "Usuario",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF2D3748)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    color = Color(0xFFE2E8F0)
                )

                // Items del menú
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = Color(0xFF64748B)
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF2D3748)
                            )
                        },
                        selected = false,
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = Color(0xFFFFF5F5)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Ítem de Cerrar Sesión
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = Color(0xFFE2E8F0)
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Color(0xFFE53E3E)
                        )
                    },
                    label = {
                        Text(
                            "Cerrar Sesión",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFE53E3E)
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        tokenManager.clearToken()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = Color.White // Cambiar a blanco puro
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // Cambiar a blanco puro
            ) {
                // Header principal unificado con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color.White // Cambiar a blanco puro
                                )
                            )
                        )
                ) {
                    Column {
                        // Top bar integrado en el header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .statusBarsPadding(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón de menú
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Abrir menú",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Solo el logo dentro del gradiente
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 16.dp)
                        ) {
                            // Logo de la app
                            Image(
                                painter = painterResource(id = R.drawable.logo_insumeal),
                                contentDescription = "Logo de Insumeal",
                                modifier = Modifier
                                    .size(120.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                // Contenido principal (ahora incluye los mensajes fuera del gradiente)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Mensajes movidos fuera del gradiente
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = if (userProfile != null) "¡Hola ${userProfile!!.username}!" else "¡Bienvenido!",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = Color(0xFF2D3748),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "¿Qué vas a comer hoy?",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF4A5568),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Sección de acciones rápidas
                    Text(
                        text = "Acciones Rápidas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Grid de tarjetas modernas
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModernActionCard(
                            title = "Analizar Plato de Comida",
                            description = "Conocé los carbohidratos e insulina sugerida con una foto de tu comida",
                            icon = Icons.Filled.PhotoCamera,
                            MaterialTheme.colorScheme.primaryContainer,
                            iconColor = Color(0xFF38A169),
                            onClick = { navController.navigate("uploadPhoto") }
                        )

                        ModernActionCard(
                            title = "Ver Historial",
                            description = "Consulta tus platos anteriores",
                            icon = Icons.Filled.History,
                            backgroundColor = Color(0xFFF7FAFF),
                            iconColor = Color(0xFF4299E1),
                            onClick = { navController.navigate("foodHistory") }
                        )

                        ModernActionCard(
                            title = "Información Clínica",
                            description = "Consulta o modifica tus datos médicos",
                            icon = Icons.Filled.Info,
                            backgroundColor = Color(0xFFFFFAF0),
                            iconColor = Color(0xFFED8936),
                            onClick = {
                                val userId = tokenManager.getUserId()
                                if (userId != null) {
                                    navController.navigate("clinicalData/$userId")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo colorido
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenido de texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF2D3748)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF718096),
                    maxLines = 2
                )
            }

            // Flecha
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFCBD5E0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
