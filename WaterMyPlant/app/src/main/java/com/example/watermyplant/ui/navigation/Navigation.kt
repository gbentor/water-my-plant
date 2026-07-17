package com.example.watermyplant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.watermyplant.ui.screens.auth.LoginScreen
import com.example.watermyplant.ui.screens.auth.RegisterScreen
import com.example.watermyplant.ui.screens.plants.PlantListScreen
import com.example.watermyplant.ui.screens.plants.PlantDetailScreen
import com.example.watermyplant.ui.screens.plants.AddPlantScreen
import com.example.watermyplant.ui.screens.plants.EditPlantScreen
import com.example.watermyplant.ui.screens.plants.DeletePlantScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object PlantList : Screen("plant_list")
    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: String) = "plant_detail/$plantId"
    }
    object AddPlant : Screen("add_plant")
    object RegisterSensor : Screen("register_sensor")
    object EditPlant : Screen("edit_plant/{plantId}") {
        fun createRoute(plantId: String) = "edit_plant/$plantId"
    }
    object DeletePlant : Screen("delete_plant/{plantId}") {
        fun createRoute(plantId: String) = "delete_plant/$plantId"
    }
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Login.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.PlantList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBackClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.PlantList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PlantList.route) {
            PlantListScreen(
                onPlantClick = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                },
                onAddPlantClick = {
                    navController.navigate(Screen.AddPlant.route)
                },
                onRegisterSensorClick = {
                    navController.navigate(Screen.RegisterSensor.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.PlantList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PlantDetail.route) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            PlantDetailScreen(
                plantId = plantId,
                onEditClick = { plantId ->
                    navController.navigate(Screen.EditPlant.createRoute(plantId))
                },
                onDeleteClick = { plantId ->
                    navController.navigate(Screen.DeletePlant.createRoute(plantId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AddPlant.route) {
            AddPlantScreen(
                onPlantAdded = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditPlant.route) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            EditPlantScreen(
                plantId = plantId,
                onEditClick = { plantId ->
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DeletePlant.route) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            DeletePlantScreen(
                plantId = plantId,
                onDeleteClick = { plantId ->
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
} 