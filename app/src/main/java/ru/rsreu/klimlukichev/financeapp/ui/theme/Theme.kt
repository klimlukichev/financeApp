package ru.rsreu.klimlukichev.financeapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF00513A),
    onPrimaryContainer = Color(0xFFB8F6D9),
    secondary = Blue80,
    onSecondary = Color(0xFF002E6B),
    secondaryContainer = Color(0xFF0A4BA8),
    onSecondaryContainer = Color(0xFFD7E2FF),
    tertiary = Mint80,
    background = FinanceBackgroundDark,
    onBackground = FinanceOnSurfaceDark,
    surface = FinanceSurfaceDark,
    onSurface = FinanceOnSurfaceDark,
    surfaceVariant = FinanceSurfaceVariantDark,
    onSurfaceVariant = FinanceOnSurfaceVariantDark,
    surfaceContainer = Color(0xFF1B2622),
    surfaceContainerHigh = Color(0xFF22302B),
    outline = Color(0xFF718178),
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F5E8),
    onPrimaryContainer = Color(0xFF003827),
    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE7FF),
    onSecondaryContainer = Color(0xFF002B66),
    tertiary = Mint40,
    background = FinanceBackgroundLight,
    onBackground = FinanceOnSurfaceLight,
    surface = FinanceSurfaceLight,
    onSurface = FinanceOnSurfaceLight,
    surfaceVariant = FinanceSurfaceVariantLight,
    onSurfaceVariant = FinanceOnSurfaceVariantLight,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF8FBF9),
    outline = Color(0xFFC9D5CF),
)

@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable (() -> Unit),
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        content()
    }
}