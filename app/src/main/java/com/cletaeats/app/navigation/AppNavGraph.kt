package com.cletaeats.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cletaeats.app.ui.screens.auth.LoginScreen
import com.cletaeats.app.ui.screens.auth.RegisterScreen
import com.cletaeats.app.ui.screens.home.HomeScreen
import com.cletaeats.app.ui.screens.splash.SplashScreen
import com.cletaeats.app.ui.screens.home.DireccionFormScreen
import com.cletaeats.app.ui.screens.home.DireccionesScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinish = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onDirecciones = {
                    navController.navigate(Routes.DIRECCIONES)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DIRECCIONES) {
            DireccionesScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAdd = {
                    navController.navigate(Routes.DIRECCION_FORM)
                }
            )
        }

        composable(Routes.DIRECCION_FORM) {
            DireccionFormScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}