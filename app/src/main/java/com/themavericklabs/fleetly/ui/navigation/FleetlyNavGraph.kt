package com.themavericklabs.fleetly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.themavericklabs.fleetly.ui.screens.*
import com.themavericklabs.fleetly.ui.viewmodels.AuthViewModel

@Composable
fun FleetlyNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingView(
                onGetStarted = {
                    navController.navigate("welcome")
                }
            )
        }
        composable("welcome") {
            WelcomeView(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("platformSelector") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable("platformSelector") {
            PlatformSelectorView(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo("platformSelector") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "deviceList?platform={platform}",
            arguments = listOf(
                navArgument("platform") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val platform = backStackEntry.arguments?.getString("platform")
            DeviceListView(
                platform = platform,
                onDeviceClick = { deviceId ->
                    navController.navigate("deviceDetail/$deviceId")
                },
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "deviceDetail/{deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            DeviceDetailView(
                deviceId = deviceId,
                navController = navController,
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "bitlocker/{deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            BitLockerView(
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "laps/{deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            LAPSView(
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "filevault/{deviceId}",
            arguments = listOf(
                navArgument("deviceId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            FileVaultView(
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("settings") {
            SettingsView(
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
    }
} 