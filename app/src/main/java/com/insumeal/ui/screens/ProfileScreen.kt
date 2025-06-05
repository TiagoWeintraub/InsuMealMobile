package com.insumeal.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insumeal.ui.viewmodel.UserProfileViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.insumeal.utils.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userId: Int = 1, navController: NavController) {
    val context = LocalContext.current
    val userProfileViewModel: UserProfileViewModel = viewModel()
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

    // UI para mostrar los datos de perfil
    Scaffold(        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
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
            if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileCard(
                        title = "Nombre",
                        value = userProfile!!.username,
                        icon = Icons.Default.Person
                    )

                    ProfileCard(
                        title = "Apellido",
                        value = userProfile!!.lastName,
                        icon = Icons.Default.Person
                    )

                    ProfileCard(
                        title = "Email",
                        value = userProfile!!.email,
                        icon = Icons.Default.Email
                    )

                    ProfileCard(
                        title = "ID de Usuario",
                        value = userProfile!!.id.toString(),
                        icon = Icons.Default.Badge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando perfil...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
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
