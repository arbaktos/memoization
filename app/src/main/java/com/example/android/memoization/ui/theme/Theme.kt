package com.example.android.memoization.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200
)

/**
 * Draft only: the window's night theme is dark, so without a dark palette Compose kept
 * painting black text over it. Proper light/dark palettes are a redesign item
 * (docs/design/redesign-brief.md, "Общее"); a few components still hard-code white.
 */
private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

/**
 * The screens mix Material 2 and Material 3 components, and each reads its own
 * MaterialTheme. Providing only one of them left half the app on library defaults,
 * which is why the stack list looked dark while everything else looked light.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    secondary = Teal200,
    tertiary = Purple700
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    tertiary = Purple700
)

@Composable
fun MemoizationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Material3Theme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme) {
        MaterialTheme(
            colors = if (darkTheme) DarkColorPalette else LightColorPalette,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
