package com.insumeal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insumeal.ui.screens.*
import com.insumeal.ui.theme.InsuMealTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsuMealTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White // Cambiar temporalmente a Color.White para forzar fondo blanco
                ) {
                    val navController = rememberNavController()
                    
                    // Contenedor para mantener el ViewModel compartido
                    MealPlateScreensContainer(navController = navController)
                }
            }
        }
    }
}

@Composable
fun MealPlateScreensContainer(navController: NavHostController) {
    // Crear los ViewModels compartidos en un contexto @Composable con keys específicas para mantenerlos
    val mealPlateViewModel: com.insumeal.ui.viewmodel.MealPlateViewModel = viewModel(key = "shared_meal_plate_vm")
    val historyViewModel: com.insumeal.ui.viewmodel.MealPlateHistoryViewModel = viewModel(key = "shared_history_vm")

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                context = LocalContext.current.applicationContext,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack("login", false)
                }
            )
        }
        composable("home") {
            HomeScreen(navController = navController, context = LocalContext.current.applicationContext)
        }
        
        composable("uploadPhoto") {
            UploadPhotoScreen(navController = navController, mealPlateViewModel = mealPlateViewModel)
        }
        
        composable("mealPlate") {
            MealPlateScreen(navController, mealPlateViewModel)
        }

        composable("dosis") {
            DosisScreen(navController, mealPlateViewModel)
        }

        composable("foodHistory") {
            FoodHistoryScreen(navController, historyViewModel)
        }
        
        composable(
            route = "foodHistoryMealPlate/{mealPlateId}",
            arguments = listOf(navArgument("mealPlateId") { type = NavType.IntType })
        ) { backStackEntry ->
            val mealPlateId = backStackEntry.arguments?.getInt("mealPlateId") ?: 0
            FoodHistoryMealPlateScreen(navController = navController, mealPlateId = mealPlateId)
        }
        
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            ProfileScreen(userId = userId, navController = navController)
        }
        
        composable(
            route = "clinicalData/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            ClinicalDataScreen(userId = userId, navController = navController)
        }
    }
}
