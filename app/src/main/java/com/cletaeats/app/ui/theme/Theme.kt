package com.cletaeats.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryGreenDark,
    background = BackgroundSoft,
    surface = SurfaceSoft
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryGreenDark
)

@Composable
fun CletaEatsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}