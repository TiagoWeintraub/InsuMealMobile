package com.insumeal.services

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Servicio de traducción usando Google ML Kit Offline
 * Maneja la traducción entre inglés y español
 */
class TranslationService {
    
    private var englishToSpanishTranslator: Translator? = null
    private var spanishToEnglishTranslator: Translator? = null
    private var isEnglishToSpanishReady = false
    private var isSpanishToEnglishReady = false
      // Diccionario de traducciones específicas para ingredientes comunes
    private val specificTranslations = mapOf(
        "hamburger bun" to "pan de hamburguesa",
        "burger bun" to "pan de hamburguesa",
        "bun" to "pan",
        "sesame bun" to "pan con sésamo",
        "whole wheat bun" to "pan integral",
        "brioche bun" to "pan brioche",
        "french fries" to "papas fritas",
        "potato chips" to "papas fritas",
        "chicken breast" to "pechuga de pollo",
        "ground beef" to "carne molida",
        "beef patty" to "hamburguesa de carne",
        "chicken patty" to "hamburguesa de pollo",
        "cheese slice" to "rebanada de queso",
        "cheddar cheese" to "queso cheddar",
        "mozzarella cheese" to "queso mozzarella",
        "swiss cheese" to "queso suizo",
        "american cheese" to "queso americano",
        "lettuce leaves" to "hojas de lechuga",
        "tomato slices" to "rebanadas de tomate",
        "red onion" to "cebolla roja",
        "white onion" to "cebolla blanca",
        "pickle slices" to "pepinillos en rodajas",
        "bacon strips" to "tiras de tocino",
        "fried egg" to "huevo frito",
        "scrambled eggs" to "huevos revueltos",
        "white bread" to "pan blanco",
        "whole wheat bread" to "pan integral",
        "sourdough bread" to "pan de masa madre",
        "rye bread" to "pan de centeno",
        "pita bread" to "pan pita",
        "tortilla wrap" to "tortilla",
        "corn tortilla" to "tortilla de maíz",
        "flour tortilla" to "tortilla de harina",
        "rice grains" to "granos de arroz",
        "white rice" to "arroz blanco",
        "brown rice" to "arroz integral",
        "pasta noodles" to "fideos",
        "spaghetti noodles" to "fideos espagueti",
        "macaroni pasta" to "pasta macarrones",
        "penne pasta" to "pasta penne",
        "mashed potatoes" to "puré de papas",
        "baked potato" to "papa al horno",
        "sweet potato" to "batata",
        "roasted vegetables" to "verduras asadas",
        "mixed vegetables" to "verduras mixtas",
        "green beans" to "judías verdes",
        "black beans" to "frijoles negros",
        "kidney beans" to "frijoles rojos",
        "chickpeas" to "garbanzos",
        "corn kernels" to "granos de maíz",
        "bell pepper" to "pimiento",
        "red bell pepper" to "pimiento rojo",
        "green bell pepper" to "pimiento verde",
        "yellow bell pepper" to "pimiento amarillo",
        "mushroom slices" to "rebanadas de champiñones",
        "button mushrooms" to "champiñones",
        "avocado slices" to "rebanadas de aguacate",
        "lime wedges" to "gajos de lima",
        "lemon wedges" to "gajos de limón",
        "olive oil" to "aceite de oliva",
        "vegetable oil" to "aceite vegetal",
        "butter" to "mantequilla",
        "margarine" to "margarina",
        "mayo" to "mayonesa",
        "mayonnaise" to "mayonesa",
        "ketchup" to "salsa de tomate",
        "mustard" to "mostaza",
        "bbq sauce" to "salsa barbacoa",
        "hot sauce" to "salsa picante",
        "soy sauce" to "salsa de soja",
        "teriyaki sauce" to "salsa teriyaki",
        "ranch dressing" to "aderezo ranch",
        "caesar dressing" to "aderezo césar",
        "italian dressing" to "aderezo italiano",
        "vinaigrette" to "vinagreta",
        "balsamic vinegar" to "vinagre balsámico",
        "apple cider vinegar" to "vinagre de manzana",
        "garlic cloves" to "dientes de ajo",
        "fresh herbs" to "hierbas frescas",
        "dried herbs" to "hierbas secas",
        "black pepper" to "pimienta negra",
        "sea salt" to "sal marina",
        "table salt" to "sal de mesa",
        "paprika" to "pimentón",
        "cumin" to "comino",
        "oregano" to "orégano",
        "basil" to "albahaca",
        "thyme" to "tomillo",
        "rosemary" to "romero",
        "parsley" to "perejil",
        "cilantro" to "cilantro",
        "green onions" to "cebolletas",
        "scallions" to "cebolletas",
        "chives" to "cebollinos",
        "breadcrumbs" to "pan rallado",
        "bolognese sauce" to "salsa boloñesa",
        "grated cheese" to "queso rallado",
        "lasagna noodles" to "láminas de lasaña",
        "lasagna" to "lasaña",
        "beans" to "arvejas",
        "pie" to "tarta",
        "pie crust" to "masa de tarta",
        "quiche" to "tarta"
    )
    
    companion object {
        @Volatile
        private var INSTANCE: TranslationService? = null
        
        fun getInstance(): TranslationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationService().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Capitaliza la primera letra de cada palabra en una cadena
     */
    private fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.lowercase().replaceFirstChar { it.uppercase() }
            } else {
                word
            }
        }
    }
    
    /**
     * Inicializa los traductores y descarga los modelos necesarios
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Configurar traductor de inglés a español
            val englishToSpanishOptions = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build()
            englishToSpanishTranslator = Translation.getClient(englishToSpanishOptions)
            
            // Configurar traductor de español a inglés
            val spanishToEnglishOptions = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.SPANISH)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            spanishToEnglishTranslator = Translation.getClient(spanishToEnglishOptions)
            
            // Configurar condiciones de descarga (WiFi preferido)
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            
            // Descargar modelos
            englishToSpanishTranslator?.downloadModelIfNeeded(conditions)?.await()
            spanishToEnglishTranslator?.downloadModelIfNeeded(conditions)?.await()
            
            isEnglishToSpanishReady = true
            isSpanishToEnglishReady = true
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
      /**
     * Traduce texto de inglés a español
     * @param text Texto en inglés a traducir
     * @return Texto traducido al español, o el texto original si hay error
     */    suspend fun translateEnglishToSpanish(text: String): String = withContext(Dispatchers.IO) {
        try {
            if (text.isBlank()) return@withContext text
            
            val cleanText = text.lowercase().trim()
            android.util.Log.d("TranslationService", "Traduciendo: '$text' -> texto limpio: '$cleanText'")
            
            // Verificar primero en el diccionario específico
            specificTranslations[cleanText]?.let { specificTranslation ->
                android.util.Log.d("TranslationService", "Encontrado en diccionario: '$cleanText' -> '$specificTranslation'")
                return@withContext capitalizeWords(specificTranslation)
            }
            
            android.util.Log.d("TranslationService", "No encontrado en diccionario, usando ML Kit para: '$cleanText'")
            
            // Si no está en el diccionario, usar Google ML Kit
            if (!isEnglishToSpanishReady) {
                initialize()
            }
            
            val translatedText = englishToSpanishTranslator?.translate(text)?.await() ?: text
            android.util.Log.d("TranslationService", "ML Kit tradujo: '$text' -> '$translatedText'")
            
            // Capitalizar la primera letra de cada palabra
            val finalText = capitalizeWords(translatedText)
            android.util.Log.d("TranslationService", "Resultado final: '$finalText'")
            finalText
        } catch (e: Exception) {
            android.util.Log.e("TranslationService", "Error traduciendo '$text': ${e.message}", e)
            // Retorna el texto original capitalizado si hay error
            capitalizeWords(text)
        }
    }
      /**
     * Traduce texto de español a inglés
     * @param text Texto en español a traducir
     * @return Texto traducido al inglés, o el texto original si hay error
     */
    suspend fun translateSpanishToEnglish(text: String): String = withContext(Dispatchers.IO) {
        try {
            if (!isSpanishToEnglishReady) {
                initialize()
            }
            
            if (text.isBlank()) return@withContext text
            
            val translatedText = spanishToEnglishTranslator?.translate(text)?.await() ?: text
            
            // Capitalizar la primera letra de cada palabra
            capitalizeWords(translatedText)
        } catch (e: Exception) {
            e.printStackTrace()
            capitalizeWords(text) // Retorna el texto original capitalizado si hay error
        }
    }
    
    /**
     * Traduce una lista de ingredientes de inglés a español
     * @param ingredients Lista de ingredientes con nombres en inglés
     * @return Lista de ingredientes con nombres traducidos al español
     */
    suspend fun translateIngredientsToSpanish(ingredients: List<com.insumeal.models.Ingredient>): List<com.insumeal.models.Ingredient> {
        return ingredients.map { ingredient ->
            val translatedName = translateEnglishToSpanish(ingredient.name)
            ingredient.copy(name = translatedName)
        }
    }
    
    /**
     * Traduce el nombre de un MealPlate de inglés a español
     * @param mealPlate MealPlate con nombre en inglés
     * @return MealPlate con nombre traducido al español
     */
    suspend fun translateMealPlateToSpanish(mealPlate: com.insumeal.models.MealPlate): com.insumeal.models.MealPlate {
        val translatedName = translateEnglishToSpanish(mealPlate.name)
        val translatedIngredients = translateIngredientsToSpanish(mealPlate.ingredients)
        
        return mealPlate.copy(
            name = translatedName,
            ingredients = translatedIngredients
        )
    }
    
    /**
     * Traduce el tipo de un MealPlateHistory de inglés a español
     * @param mealPlateHistory MealPlateHistory con tipo en inglés
     * @return MealPlateHistory con tipo traducido al español
     */
    suspend fun translateMealPlateHistoryToSpanish(mealPlateHistory: com.insumeal.models.MealPlateHistory): com.insumeal.models.MealPlateHistory {
        val translatedType = translateEnglishToSpanish(mealPlateHistory.type)
        
        return mealPlateHistory.copy(
            type = translatedType
        )
    }
    
    /**
     * Traduce una lista de MealPlateHistory de inglés a español
     * @param historyList Lista de MealPlateHistory con tipos en inglés
     * @return Lista de MealPlateHistory con tipos traducidos al español
     */
    suspend fun translateMealPlateHistoryListToSpanish(historyList: List<com.insumeal.models.MealPlateHistory>): List<com.insumeal.models.MealPlateHistory> {
        return historyList.map { history ->
            translateMealPlateHistoryToSpanish(history)
        }
    }
    
    /**
     * Verifica si los traductores están listos
     */
    fun isReady(): Boolean {
        return isEnglishToSpanishReady && isSpanishToEnglishReady
    }
    
    /**
     * Libera los recursos de los traductores
     */
    fun close() {
        englishToSpanishTranslator?.close()
        spanishToEnglishTranslator?.close()
        isEnglishToSpanishReady = false
        isSpanishToEnglishReady = false
    }
}
