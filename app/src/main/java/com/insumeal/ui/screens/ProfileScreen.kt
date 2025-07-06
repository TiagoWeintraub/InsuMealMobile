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
import com.insumeal.ui.theme.Turquoise500
import com.insumeal.ui.theme.Turquoise600
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
        containerColor = Color(0xFFF7FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplicar el padding del Scaffold
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
                        .height(180.dp)
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

                // Contenido encima de la forma ondulada (solo botón y título)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                text = "Mi Perfil",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                // Avatar posicionado adelante de la onda (fuera del área ondulada)
                userProfile?.let { profile ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 120.dp) // Posicionar debajo del header pero adelante de la onda
                    ) {
                        // Avatar del usuario más grande con sombra para destacar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFE91E63),
                                                Color(0xFFC2185B)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.username.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 42.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nombre del usuario en negro
                        Text(
                            text = "${profile.username} ${profile.lastName}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.Black
                        )
                    }
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
                            color = Turquoise500,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Agregada sombra elegante
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
