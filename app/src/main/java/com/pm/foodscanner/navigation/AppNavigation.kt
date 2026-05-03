package com.pm.foodscanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.pm.foodscanner.ui.auth.AuthViewModel
import com.pm.foodscanner.ui.auth.LoginScreen
import com.pm.foodscanner.ui.auth.RegisterScreen
import com.pm.foodscanner.ui.barcode.BarcodeResultScreen
import com.pm.foodscanner.ui.barcode.BarcodeScanScreen
import com.pm.foodscanner.ui.barcode.BarcodeViewModel
import com.pm.foodscanner.ui.main.MainScreen
import com.pm.foodscanner.ui.meal.MealResultScreen
import com.pm.foodscanner.ui.meal.MealScanScreen
import com.pm.foodscanner.ui.meal.MealViewModel
import com.pm.foodscanner.ui.profile.ProfileSetupScreen
import com.pm.foodscanner.ui.profile.ProfileViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROFILE_SETUP = "profile_setup"
    const val MAIN = "main"

    const val BARCODE_GRAPH = "barcode_graph"
    const val BARCODE_SCAN = "barcode_scan"
    const val BARCODE_RESULT = "barcode_result"

    const val MEAL_GRAPH = "meal_graph"
    const val MEAL_SCAN = "meal_scan"
    const val MEAL_RESULT = "meal_result"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = when {
        authState.isLoggedIn && authState.hasProfile -> Routes.MAIN
        authState.isLoggedIn && !authState.hasProfile -> Routes.PROFILE_SETUP
        else -> Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { hasProfile ->
                    val dest = if (hasProfile) Routes.MAIN else Routes.PROFILE_SETUP
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE_SETUP) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileSetupScreen(
                viewModel = profileViewModel,
                onProfileSaved = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onScanBarcode = { navController.navigate(Routes.BARCODE_GRAPH) },
                onScanMeal = { navController.navigate(Routes.MEAL_GRAPH) },
                onEditProfile = { navController.navigate(Routes.PROFILE_SETUP) },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        navigation(startDestination = Routes.BARCODE_SCAN, route = Routes.BARCODE_GRAPH) {
            composable(Routes.BARCODE_SCAN) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(Routes.BARCODE_GRAPH) }
                val barcodeViewModel: BarcodeViewModel = hiltViewModel(parentEntry)
                BarcodeScanScreen(
                    viewModel = barcodeViewModel,
                    onBack = { navController.popBackStack(Routes.MAIN, false) },
                    onBarcodeResult = {
                        navController.navigate(Routes.BARCODE_RESULT) {
                            popUpTo(Routes.BARCODE_SCAN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.BARCODE_RESULT) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(Routes.BARCODE_GRAPH) }
                val barcodeViewModel: BarcodeViewModel = hiltViewModel(parentEntry)
                BarcodeResultScreen(
                    viewModel = barcodeViewModel,
                    onScanAnother = {
                        navController.navigate(Routes.BARCODE_GRAPH) {
                            popUpTo(Routes.MAIN)
                        }
                    },
                    onBack = { navController.popBackStack(Routes.MAIN, false) }
                )
            }
        }

        navigation(startDestination = Routes.MEAL_SCAN, route = Routes.MEAL_GRAPH) {
            composable(Routes.MEAL_SCAN) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(Routes.MEAL_GRAPH) }
                val mealViewModel: MealViewModel = hiltViewModel(parentEntry)
                MealScanScreen(
                    viewModel = mealViewModel,
                    onBack = { navController.popBackStack(Routes.MAIN, false) },
                    onResult = {
                        navController.navigate(Routes.MEAL_RESULT) {
                            popUpTo(Routes.MEAL_SCAN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.MEAL_RESULT) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry(Routes.MEAL_GRAPH) }
                val mealViewModel: MealViewModel = hiltViewModel(parentEntry)
                MealResultScreen(
                    viewModel = mealViewModel,
                    onScanAnother = {
                        navController.navigate(Routes.MEAL_GRAPH) {
                            popUpTo(Routes.MAIN)
                        }
                    },
                    onBack = { navController.popBackStack(Routes.MAIN, false) }
                )
            }
        }
    }
}
