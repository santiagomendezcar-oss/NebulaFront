package com.example.nebulagourmet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BoutiqueColorScheme = lightColorScheme(
    primary = ElegantBlack,        // Texto e iconos principales
    secondary = ElegantBrown,      // Acentos
    tertiary = ElegantGold,        // Detalles de lujo
    background = BoutiqueCream,    // Fondo general (Crema)
    surface = BoutiqueSurface,     // Un tono un poco más oscuro para contenedores
    onPrimary = ElegantWhite,      // Texto sobre botones negros
    onSecondary = ElegantWhite,
    onTertiary = ElegantBlack,
    onBackground = ElegantBlack,   // Texto sobre fondo crema (MUY IMPORTANTE)
    onSurface = ElegantBrown,      // Texto sobre tarjetas beige
    outline = ElegantBorder
)

@Composable
fun NebulaGourmetTheme(
    darkTheme: Boolean = false, // Forzamos false para evitar el fondo negro del sistema
    content: @Composable () -> Unit
) {
    val colorScheme = BoutiqueColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
