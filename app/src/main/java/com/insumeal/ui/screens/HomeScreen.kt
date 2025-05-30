package com.insumeal.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.insumeal.utils.TokenManager
import kotlinx.coroutines.launch // Para controlar el drawer

// Data class para los ítems del Navigation Drawer
data class DrawerItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Necesario si no usas el padding que provee Scaffold
@OptIn(ExperimentalMaterial3Api::class) // Para TopAppBar y NavigationDrawer
@Composable
fun HomeScreen(navController: NavController, context: Context) {
    val tokenManager = remember { TokenManager(context) } // Usar remember para TokenManager
    val token = tokenManager.getToken()

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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = false, // Puedes manejar la selección si es necesario
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route) {
                                launchSingleTop = true // Evita múltiples instancias de la misma pantalla
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                // Ítem de Cerrar Sesión separado
                Divider(modifier = Modifier.padding(vertical = 8.dp))
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
                                tint = MaterialTheme.colorScheme.primary // Color primario
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
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "InsuMeal",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Bienvenido al home",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            ButtonWithIcon(
                                text = "Analizar Plato de Comida",
                                icon = Icons.Filled.PhotoCamera,
                                onClick = { navController.navigate("uploadPhoto") }
                            )
                            ButtonWithIcon(
                                text = "Ver Historial",
                                icon = Icons.Filled.History,
                                onClick = { navController.navigate("foodHistory") }
                            )

                        }
                    }
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

