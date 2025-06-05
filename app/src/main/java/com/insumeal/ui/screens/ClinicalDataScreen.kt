package com.insumeal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrackChanges // Importar el icono correcto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                ) {
                    ClinicalDataCard(
                        title = "Sensibilidad a la Insulina",
                        value = "${clinicalData!!.sensitivity} mg/dl por unidad",
                        icon = Icons.Default.MonitorHeart
                    )

                    ClinicalDataCard(
                        title = "Ratio Insulina/Carbohidratos",
                        value = "1:${clinicalData!!.ratio} unidades",
                        icon = Icons.Default.Scale
                    )

                    ClinicalDataCard(
                        title = "Objetivo de Glucemia",
                        value = "${clinicalData!!.glycemiaTarget} mg/dl",
                        icon = Icons.Filled.TrackChanges // Usar el icono correcto
                    )

                    ClinicalDataCard(
                        title = "ID de Usuario",
                        value = clinicalData!!.userId.toString(),
                        icon = Icons.Default.Bloodtype
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
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
