package com.example.android.memoization.ui.features.memoizationscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The session screen's own palette, from the design handoff (variant 1a dark, 2a light). The
 * result colours come twice: a button fill, and a slightly deeper shade for the progress bar
 * and legend, so the segments still read against a light track.
 */
@Immutable
data class SessionColors(
    val background: Color,
    val card: Color,
    val track: Color,
    val text: Color,
    val secondaryText: Color,
    val question: Color,
    val mutedText: Color,
    val onResult: Color,
    val forgotFill: Color,
    val hardFill: Color,
    val goodFill: Color,
    val forgotBar: Color,
    val hardBar: Color,
    val goodBar: Color,
    val noOutline: Color,
    val noText: Color,
    /** The 1dp frame of the card's back after Hard. */
    val backBorder: Color,
    /** The same frame after Forgot, so the turned card says why it is turned. */
    val forgotBorder: Color,
    val shadow: Color,
    val shadowElevation: Dp,
)

private val Dark = SessionColors(
    background = Color(0xFF2A2A2E),
    card = Color(0xFF333338),
    track = Color(0xFF3C3C42),
    text = Color(0xFFECECEE),
    secondaryText = Color(0xFFA8A8B0),
    question = Color(0xFFB5B5BC),
    mutedText = Color(0xFF9A9AA2),
    onResult = Color(0xFF1A1A1C),
    forgotFill = Color(0xFFE5627A),
    hardFill = Color(0xFFE8C15C),
    goodFill = Color(0xFF3DB3A6),
    forgotBar = Color(0xFFE5627A),
    hardBar = Color(0xFFE8C15C),
    goodBar = Color(0xFF3DB3A6),
    noOutline = Color(0xFFE5627A),
    noText = Color(0xFFF58A98),
    backBorder = Color(0xFFE8C15C).copy(alpha = 0.35f),
    forgotBorder = Color(0xFFE5627A).copy(alpha = 0.35f),
    shadow = Color.Black.copy(alpha = 0.35f),
    shadowElevation = 12.dp,
)

private val Light = SessionColors(
    background = Color(0xFFF3F3F0),
    card = Color(0xFFFFFFFF),
    track = Color(0xFFE2E2DE),
    text = Color(0xFF1C1C1F),
    secondaryText = Color(0xFF5E5E66),
    question = Color(0xFF5E5E66),
    mutedText = Color(0xFF7A7A82),
    onResult = Color(0xFF1A1A1C),
    forgotFill = Color(0xFFEC6A80),
    hardFill = Color(0xFFF0C95E),
    goodFill = Color(0xFF3DB3A6),
    forgotBar = Color(0xFFD9506B),
    hardBar = Color(0xFFE6B846),
    goodBar = Color(0xFF26A094),
    noOutline = Color(0xFFD9506B),
    noText = Color(0xFFB5304F),
    backBorder = Color(0xFFE6B846).copy(alpha = 0.6f),
    forgotBorder = Color(0xFFD9506B).copy(alpha = 0.6f),
    // The handoff's light shadow is two soft layers; one light one at a low alpha is close.
    shadow = Color(0xFF14141E).copy(alpha = 0.14f),
    shadowElevation = 6.dp,
)

@Composable
fun sessionColors(dark: Boolean = isSystemInDarkTheme()): SessionColors = if (dark) Dark else Light
