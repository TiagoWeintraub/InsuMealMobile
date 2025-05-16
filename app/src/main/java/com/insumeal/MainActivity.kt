package com.insumeal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.insumeal.ui.screens.*
import com.insumeal.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsuMealTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {

                        composable("login") {
                            LoginScreen(context = applicationContext) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }

                        composable("home") {
                            HomeScreen(navController = navController, context = applicationContext)
                        }
//
//                        composable("upload") {
//                            UploadPhotoScreen()
//                        }
//
//                        composable("historial") {
//                            FoodHistoryScreen(navController = navController)
//                        }

//                        composable(
//                            "product_detail/{productId}",
//                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
//                        ) { backStackEntry ->
//                            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
//                            ProductDetailScreen(productId = productId)
//                        }

                    }
                }
            }
        }
    }
}