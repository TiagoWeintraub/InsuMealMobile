package com.insumeal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.insumeal.ui.viewmodel.MealPlateViewModel

@Composable
fun MealPlateScreen(mealPlateViewModel: MealPlateViewModel = viewModel()) {
    // Simula GET al cargar la pantalla
    LaunchedEffect(Unit) {
        mealPlateViewModel.cargarDatosEjemplo()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagen en la parte superior
        if (mealPlateViewModel.bitmap != null) {
            Image(
                bitmap = mealPlateViewModel.bitmap!!.asImageBitmap(),
                contentDescription = "Foto del plato",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        } else if (mealPlateViewModel.imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(mealPlateViewModel.imageUri),
                contentDescription = "Foto del plato",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Campos editables
        OutlinedTextField(
            value = mealPlateViewModel.nombre,
            onValueChange = { mealPlateViewModel.nombre = it },
            label = { Text("Nombre del plato") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = mealPlateViewModel.cantidad,
            onValueChange = { mealPlateViewModel.cantidad = it },
            label = { Text("Cantidad (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = mealPlateViewModel.carbohidratos,
            onValueChange = { mealPlateViewModel.carbohidratos = it },
            label = { Text("Carbohidratos (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = mealPlateViewModel.ingredientes,
            onValueChange = { mealPlateViewModel.ingredientes = it },
            label = { Text("Ingredientes") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

