package com.insumeal.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MealPlateViewModel : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
    var bitmap by mutableStateOf<Bitmap?>(null)

    // Ejemplo de campos editables
    var nombre by mutableStateOf("")
    var cantidad by mutableStateOf("")
    var carbohidratos by mutableStateOf("")
    var ingredientes by mutableStateOf("")

    fun setImage(uri: Uri?, bmp: Bitmap?) {
        imageUri = uri
        bitmap = bmp
    }

    // Simula un GET para obtener los datos del plato
    fun cargarDatosEjemplo() {
        nombre = "Plato ejemplo"
        cantidad = "200"
        carbohidratos = "50"
        ingredientes = "Arroz, Pollo, Verduras"
    }
}

