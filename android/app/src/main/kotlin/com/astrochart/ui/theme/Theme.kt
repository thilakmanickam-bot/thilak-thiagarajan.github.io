package com.astrochart.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Single fixed dark scheme — the app commits to the celestial night aesthetic.
private val CelestialColorScheme = darkColorScheme(
    primary = GoldDeep,
    onPrimary = OnGold,
    secondary = GoldLight,
    onSecondary = OnGold,
    background = AstroBgBottom,
    onBackground = TextPrimary,
    surface = CardFill,
    onSurface = TextPrimary,
    surfaceVariant = CardFill,
    onSurfaceVariant = TextMuted,
    secondaryContainer = CardFill,
    onSecondaryContainer = TextPrimary,
    outline = CardBorder,
    error = AstroError
)

@Composable
fun AstroChartTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent status bar with light icons so the
            // celestial gradient runs underneath it.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = AndroidColor.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = CelestialColorScheme,
        typography = Typography,
        content = content
    )
}
