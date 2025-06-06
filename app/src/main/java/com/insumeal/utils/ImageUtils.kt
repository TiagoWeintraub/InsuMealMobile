package com.insumeal.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Clase de utilidad para el manejo de imágenes
 */
object ImageUtils {
    
    /**
     * Convierte una URI en un archivo temporal
     * @param context Contexto de la aplicación
     * @param uri URI de la imagen
     * @return Archivo temporal con la imagen
     */
    @Throws(IOException::class)
    fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        
        // Crear un archivo temporal
        val fileExtension = getFileExtension(contentResolver, uri) ?: "jpg"
        val fileName = "img_${System.currentTimeMillis()}.$fileExtension"
        val tempFile = File(context.cacheDir, fileName)
        
        // Copiar contenido al archivo temporal
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { fileOut ->
                inputStream.copyTo(fileOut)
            }
        } ?: throw IOException("No se pudo abrir el stream de la URI")
        
        return tempFile
    }
    
    /**
     * Convierte un Bitmap en un archivo temporal
     * @param context Contexto de la aplicación
     * @param bitmap Bitmap de la imagen
     * @return Archivo temporal con la imagen
     */
    @Throws(IOException::class)
    fun bitmapToFile(context: Context, bitmap: Bitmap): File {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
        }
        
        return file
    }
    
    /**
     * Obtiene la extensión del archivo de una URI
     */
    private fun getFileExtension(contentResolver: ContentResolver, uri: Uri): String? {
        // Primero intentamos con el MimeTypeMap
        val mimeType = contentResolver.getType(uri)
        return if (mimeType != null) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            // Si falla, intentamos con la ruta de la URI
            val path = uri.path
            if (path != null) {
                val extensionIndex = path.lastIndexOf(".")
                if (extensionIndex > 0) {
                    return path.substring(extensionIndex + 1)
                }
            }
            // Por defecto, usamos jpg
            "jpg"
        }
    }
}
