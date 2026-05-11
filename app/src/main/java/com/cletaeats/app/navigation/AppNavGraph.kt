package com.cletaeats.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cletaeats.app.ui.screens.auth.LoginScreen
import com.cletaeats.app.ui.screens.auth.RegisterScreen
import com.cletaeats.app.ui.screens.client.CheckoutScreen
import com.cletaeats.app.ui.screens.client.ClientePedidosScreen
import com.cletaeats.app.ui.screens.client.CombosScreen
import com.cletaeats.app.ui.screens.client.FeedbackScreen
import com.cletaeats.app.ui.screens.client.PedidoDetalleScreen
import com.cletaeats.app.ui.screens.client.PedidoTrackingScreen
import com.cletaeats.app.ui.screens.client.RestaurantesScreen
import com.cletaeats.app.ui.screens.delivery.RepartidorPedidosScreen
import com.cletaeats.app.ui.screens.home.DireccionFormScreen
import com.cletaeats.app.ui.screens.home.DireccionesScreen
import com.cletaeats.app.ui.screens.home.HomeScreen
import com.cletaeats.app.ui.screens.splash.SplashScreen

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
                onFinish = { hasSession ->
                    navController.navigate(if (hasSession) Routes.HOME else Routes.LOGIN) {
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
                onRestaurantes = {
                    navController.navigate(Routes.RESTAURANTES)
                },
                onDirecciones = {
                    navController.navigate(Routes.DIRECCIONES)
                },
                onMisPedidos = {
                    navController.navigate(Routes.CLIENTE_PEDIDOS)
                },
                onPedidosRepartidor = {
                    navController.navigate(Routes.REPARTIDOR_PEDIDOS)
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
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Routes.DIRECCION_FORM) }
            )
        }

        composable(Routes.DIRECCION_FORM) {
            DireccionFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.RESTAURANTES) {
            RestaurantesScreen(
                onBack = { navController.popBackStack() },
                onOpenRestaurant = { restauranteId ->
                    navController.navigate(Routes.combos(restauranteId))
                },
                onCart = {
                    navController.navigate(Routes.CHECKOUT)
                }
            )
        }

        composable(
            route = Routes.COMBOS,
            arguments = listOf(
                navArgument("restauranteId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val restauranteId = backStackEntry.arguments
                ?.getLong("restauranteId")
                ?: return@composable

            CombosScreen(
                restauranteId = restauranteId,
                onBack = { navController.popBackStack() },
                onCheckout = {
                    navController.navigate(Routes.CHECKOUT)
                }
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onAddAddress = {
                    navController.navigate(Routes.DIRECCION_FORM)
                },
                onPedidoCreado = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId)) {
                        popUpTo(Routes.RESTAURANTES)
                    }
                }
            )
        }

        composable(Routes.CLIENTE_PEDIDOS) {
            ClientePedidosScreen(
                onBack = { navController.popBackStack() },
                onDetail = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId))
                },
                onRestaurants = {
                    navController.navigate(Routes.RESTAURANTES)
                }
            )
        }

        composable(
            route = Routes.PEDIDO_DETALLE,
            arguments = listOf(
                navArgument("pedidoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments
                ?.getLong("pedidoId")
                ?: return@composable

            PedidoDetalleScreen(
                pedidoId = pedidoId,
                onBack = {
                    navController.popBackStack()
                },
                onTracking = { id ->
                    navController.navigate(Routes.pedidoTracking(id))
                },
                onFeedback = { id ->
                    navController.navigate(Routes.feedback(id))
                }
            )
        }

        composable(
            route = Routes.PEDIDO_TRACKING,
            arguments = listOf(
                navArgument("pedidoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments
                ?.getLong("pedidoId")
                ?: return@composable

            PedidoTrackingScreen(
                pedidoId = pedidoId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.FEEDBACK,
            arguments = listOf(
                navArgument("pedidoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments
                ?.getLong("pedidoId")
                ?: return@composable

            FeedbackScreen(
                pedidoId = pedidoId,
                onBack = {
                    navController.popBackStack()
                },
                onDone = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REPARTIDOR_PEDIDOS) {
            RepartidorPedidosScreen(
                onBack = {
                    navController.popBackStack()
                },
                onDetail = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId))
                }
            )
        }
    }
}