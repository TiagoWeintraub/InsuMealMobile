package com.insumeal.utils

import android.content.ContentResolver
import android.content.Context
import android.media.ExifInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import coil.request.ImageRequest
import java.io.File
import java.io.ByteArrayOutputStream
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

        val originalSizeBytes = contentResolver.openFileDescriptor(uri, "r")?.use {
            it.statSize
        } ?: 0L

        val orientation = readExifOrientation(contentResolver, uri)
        val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IOException("No se pudo decodificar la imagen")

        val normalizedBitmap = applyExifOrientation(bitmap, orientation)

        val compressedFile = File(context.cacheDir, "img_${System.currentTimeMillis()}_normalized.jpg")
        var low = MIN_JPEG_QUALITY
        var high = 100
        var bestCompressedBytes: ByteArray? = null
        var bestQuality: Int? = null

        // Buscamos la mayor calidad posible que siga por debajo de 1MB.
        while (low <= high) {
            val quality = (low + high) / 2
            val byteStream = ByteArrayOutputStream()
            normalizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
            val candidateBytes = byteStream.toByteArray()

            if (candidateBytes.size <= MAX_UPLOAD_SIZE_BYTES) {
                bestCompressedBytes = candidateBytes
                bestQuality = quality
                low = quality + 1
            } else {
                high = quality - 1
            }
        }

        val compressedBytes = bestCompressedBytes ?: run {
            val fallbackStream = ByteArrayOutputStream()
            normalizedBitmap.compress(Bitmap.CompressFormat.JPEG, MIN_JPEG_QUALITY, fallbackStream)
            fallbackStream.toByteArray()
        }

        FileOutputStream(compressedFile).use { output ->
            output.write(compressedBytes)
            output.flush()
        }

        Log.d(
            TAG,
            "Normalizacion/compresion aplicada: orientacionExif=$orientation, qualityFinal=${bestQuality ?: MIN_JPEG_QUALITY}, tamanoOriginal=${originalSizeBytes}B, tamanoFinal=${compressedFile.length()}B"
        )

        return compressedFile
    }

    private fun readExifOrientation(contentResolver: ContentResolver, uri: Uri): Int {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo leer EXIF, usando ORIENTATION_NORMAL", e)
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            else -> return source
        }

        return try {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo aplicar transformacion EXIF, se usa bitmap original", e)
            source
        }
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
