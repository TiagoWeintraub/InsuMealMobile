package com.insumeal.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import coil.request.ImageRequest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Clase de utilidad para el manejo de imágenes
 */
object ImageUtils {

    private const val MAX_UPLOAD_SIZE_BYTES = 1_048_576L // 1 MB
    private const val MIN_JPEG_QUALITY = 30
    private const val TAG = "ImageUtils"
    
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

        // Si ya cumple el limite, se envia tal cual sin reprocesar.
        if (tempFile.length() <= MAX_UPLOAD_SIZE_BYTES) {
            return tempFile
        }

        val originalSizeBytes = tempFile.length()

        // Si supera 1MB, recomprimir como JPEG buscando la mayor calidad posible bajo el limite.
        val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IOException("No se pudo decodificar la imagen para compresion")

        val compressedFile = File(context.cacheDir, "img_${System.currentTimeMillis()}_compressed.jpg")
        var low = MIN_JPEG_QUALITY
        var high = 100
        var bestCompressedBytes: ByteArray? = null
        var bestQuality: Int? = null

        // Buscamos la mayor calidad posible que siga por debajo de 1MB.
        while (low <= high) {
            val quality = (low + high) / 2
            val byteStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
            val candidateBytes = byteStream.toByteArray()

            if (candidateBytes.size <= MAX_UPLOAD_SIZE_BYTES) {
                bestCompressedBytes = candidateBytes
                bestQuality = quality
                low = quality + 1
            } else {
                high = quality - 1
            }
        }

        val compressedBytes = bestCompressedBytes
            ?: throw IOException("No se pudo comprimir la imagen a 1MB")

        FileOutputStream(compressedFile).use { output ->
            output.write(compressedBytes)
            output.flush()
        }

        Log.d(
            TAG,
            "Compresion aplicada: qualityFinal=${bestQuality ?: -1}, tamanoOriginal=${originalSizeBytes}B, tamanoFinal=${compressedFile.length()}B"
        )

        return compressedFile
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
            }            // Por defecto, usamos jpg
            "jpg"
        }
    }
    
    /**
     * Crea una solicitud de imagen autenticada con token Bearer
     * @param context Contexto de la aplicación
     * @param imageUrl URL de la imagen
     * @return ImageRequest configurado con autenticación
     */
    fun createAuthenticatedImageRequest(context: Context, imageUrl: String): ImageRequest {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        return ImageRequest.Builder(context)
            .data(imageUrl)
            .apply {
                if (token != null) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()
    }
}
