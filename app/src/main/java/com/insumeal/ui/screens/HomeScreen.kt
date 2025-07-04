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
import com.insumeal.ui.theme.*

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

    // Optimización: Evitar recrear estas variables en cada recomposición
    val drawerItems = remember {
        listOf(
            DrawerItem("profile", "Perfil", Icons.Filled.AccountCircle),
            // Puedes añadir más items aquí
        )
    }

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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar del usuario
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFE91E63), Color(0xFFC2185B))
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
        ) { _ -> // Cambiar innerPadding por _ para indicar que no se usa
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // Cambiar a blanco puro
            ) {
                // Header principal con forma ondulada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(
                            androidx.compose.foundation.shape.GenericShape { size, _ ->
                                val width = size.width
                                val height = size.height

                                // Crear la forma ondulada completa del contenedor
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
                ) {
                    // Primera onda natural - fluye desde la izquierda
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(
                                androidx.compose.foundation.shape.GenericShape { size, _ ->
                                    val width = size.width
                                    val height = size.height

                                    // Onda natural que fluye suavemente
                                    moveTo(0f, 0f)
                                    lineTo(width, 0f)
                                    lineTo(width, height * 0.2f)

                                    // Curva suave que baja en el centro
                                    cubicTo(
                                        width * 0.8f, height * 0.3f,
                                        width * 0.6f, height * 0.5f,
                                        width * 0.4f, height * 0.6f
                                    )

                                    // Continúa bajando suavemente hacia la izquierda
                                    cubicTo(
                                        width * 0.2f, height * 0.7f,
                                        width * 0.1f, height * 0.8f,
                                        0f, height * 0.75f
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

                    // Segunda onda natural - fluye desde la derecha y se cruza
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(
                                androidx.compose.foundation.shape.GenericShape { size, _ ->
                                    val width = size.width
                                    val height = size.height

                                    // Segunda onda con patrón diferente - ondas más pronunciadas
                                    moveTo(0f, 0f)
                                    lineTo(width, 0f)
                                    lineTo(width, height * 0.25f)

                                    // Primera ondulación - baja pronunciada
                                    cubicTo(
                                        width * 0.85f, height * 0.45f,
                                        width * 0.75f, height * 0.55f,
                                        width * 0.65f, height * 0.4f
                                    )

                                    // Segunda ondulación - sube
                                    cubicTo(
                                        width * 0.55f, height * 0.25f,
                                        width * 0.45f, height * 0.15f,
                                        width * 0.35f, height * 0.3f
                                    )

                                    // Tercera ondulación - baja suave hacia la izquierda
                                    cubicTo(
                                        width * 0.25f, height * 0.45f,
                                        width * 0.15f, height * 0.5f,
                                        0f, height * 0.4f
                                    )

                                    close()
                                }
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Turquoise300.copy(alpha = 0.7f), // Más transparente para el efecto de cruce
                                        Turquoise400.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )

                    // Solo el botón de menú dentro de las ondas
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
                    }
                }

                // Logo posicionado fuera de las ondas para que aparezca delante
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-115).dp), // Mover el logo más arriba
                    contentAlignment = Alignment.Center
                ) {
                    // Logo de la app - más grande y posicionado encima de las ondas
                    Image(
                        painter = painterResource(id = R.drawable.logo_insumeal),
                        contentDescription = "Logo de Insumeal",
                        modifier = Modifier
                            .size(160.dp), // Aumentado de 120.dp a 160.dp
                        contentScale = ContentScale.Fit
                    )
                }

                // Contenido principal con offset negativo para subirlo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-65).dp) // Subir el contenido principal
                        .padding(horizontal = 20.dp)
                ) {
                    // Mensajes movidos fuera del gradiente
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
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
                            backgroundColor = Color.White, // Turquoise50
                            iconColor = Turquoise600,
                            onClick = { navController.navigate("uploadPhoto") }
                        )

                        ModernActionCard(
                            title = "Ver Historial",
                            description = "Consulta tus platos anteriores",
                            icon = Icons.Filled.History,
                            backgroundColor = Color.White,
                            iconColor = Color(0xFF4299E1),
                            onClick = { navController.navigate("foodHistory") }
                        )

                        ModernActionCard(
                            title = "Información Clínica",
                            description = "Consulta o modifica tus datos médicos",
                            icon = Icons.Filled.Info,
                            backgroundColor = Color.White,
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
