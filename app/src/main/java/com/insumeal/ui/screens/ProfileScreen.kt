package com.insumeal.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insumeal.ui.viewmodel.UserProfileViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.insumeal.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userId: Int = 1, navController: NavController) {
    val context = LocalContext.current
    val userProfileViewModel = remember { UserProfileViewModel() }
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    // Si el perfil es nulo, intentamos cargarlo
    LaunchedEffect(Unit) {
        if (userProfile == null) {
            val tokenManager = TokenManager(context)
            val token = tokenManager.getToken()
            val savedUserId = tokenManager.getUserId() ?: userId.toString()

            if (token != null) {
                val authHeader = "Bearer $token"
                userProfileViewModel.loadUserProfile(authHeader, savedUserId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 12.dp) // Añadir margen desde el borde izquierdo
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color(0xFFF7FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header con gradiente y avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFF7FAFC)
                            )
                        )
                    )
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar grande
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF6B35), Color(0xFFFF8E53))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile != null) {
                            Text(
                                text = userProfile!!.username.take(2).uppercase(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp
                                ),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre del usuario
                    Text(
                        text = if (userProfile != null) "${userProfile!!.username} ${userProfile!!.lastName}" else "Cargando...",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color(0xFF2D3748)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Perfil de Usuario",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF4A5568)
                    )
                }
            }

            // Contenido del perfil
            if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF2D3748),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ModernProfileCard(
                        title = "Nombre",
                        value = userProfile!!.username,
                        icon = Icons.Default.Person,
                        backgroundColor = Color(0xFFF0FFF4),
                        iconColor = Color(0xFF38A169)
                    )

                    ModernProfileCard(
                        title = "Apellido",
                        value = userProfile!!.lastName,
                        icon = Icons.Default.Person,
                        backgroundColor = Color(0xFFF7FAFF),
                        iconColor = Color(0xFF4299E1)
                    )

                    ModernProfileCard(
                        title = "Correo Electrónico",
                        value = userProfile!!.email,
                        icon = Icons.Default.Email,
                        backgroundColor = Color(0xFFFFFAF0),
                        iconColor = Color(0xFFED8936)
                    )

                    ModernProfileCard(
                        title = "ID de Usuario",
                        value = "#${userProfile!!.id}",
                        icon = Icons.Default.Badge,
                        backgroundColor = Color(0xFFF8F0FF),
                        iconColor = Color(0xFF9F7AEA)
                    )
                }
            } else {
                // Estado de carga
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF6B35),
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Cargando perfil...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF718096)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernProfileCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(backgroundColor),
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

            // Contenido de texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF718096)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF2D3748)
                )
            }
        }
    }
}
