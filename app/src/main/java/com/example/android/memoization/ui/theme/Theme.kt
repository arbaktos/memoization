package com.example.android.memoization.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = Purple500,
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

@Composable
fun MemoizationTheme(
    content: @Composable () -> Unit
) {
    Material3Theme(colorScheme = LightColorScheme) {
        MaterialTheme(
            colors = LightColorPalette,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
