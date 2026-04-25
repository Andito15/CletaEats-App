package com.cletaeats.app.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cletaeats.app.domain.session.SessionManager

@Composable
fun HomeScreen(
    onDirecciones: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val nombre = sessionManager.getNombre().orEmpty()
    val correo = sessionManager.getCorreo().orEmpty()
    val rol = sessionManager.getRol().orEmpty()

    val logoutAction = {
        sessionManager.clearSession()
        onLogout()
    }

    when (rol) {
        "REPARTIDOR" -> {
            DeliveryHomeScreen(
                nombre = nombre,
                correo = correo,
                onLogout = logoutAction
            )
        }

        else -> {
            ClientHomeScreen(
                nombre = nombre,
                correo = correo,
                onDirecciones = onDirecciones,
                onLogout = logoutAction
            )
        }
    }
}