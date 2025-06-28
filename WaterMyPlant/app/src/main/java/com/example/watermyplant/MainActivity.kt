package com.example.watermyplant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.watermyplant.ui.screens.auth.AuthViewModel
import com.example.watermyplant.ui.screens.auth.LoginScreen
import com.example.watermyplant.ui.screens.auth.RegisterScreen
import com.example.watermyplant.ui.screens.plants.AddPlantScreen
import com.example.watermyplant.ui.screens.plants.EditPlantScreen
import com.example.watermyplant.ui.screens.plants.PlantDetailScreen
import com.example.watermyplant.ui.screens.plants.PlantListScreen
import com.example.watermyplant.ui.theme.WaterMyPlantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterMyPlantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WaterMyPlantNavigation()
                }
            }
        }
    }
}

@Composable
fun WaterMyPlantNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "plantList" else "login"
    ) {
        // Auth routes
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("plantList") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("plantList") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Plant routes
        composable("plantList") {
            PlantListScreen(
                onPlantClick = { plantId ->
                    navController.navigate("plantDetail/$plantId")
                },
                onAddPlantClick = {
                    navController.navigate("addPlant")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("plantList") { inclusive = true }
                    }
                }
            )
        }
        composable("plantDetail/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            PlantDetailScreen(
                plantId = plantId,
                onEditClick = { plantId ->
                    navController.navigate("editPlant/$plantId")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("addPlant") {
            AddPlantScreen(
                onPlantAdded = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("editPlant/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            EditPlantScreen(
                plantId = plantId,
                onEditClick = { plantId ->
                    navController.navigate("editPlant/$plantId")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}