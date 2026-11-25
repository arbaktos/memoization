package com.example.android.memoization.ui.features.folderscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The stack list's palette, from the design handoff "Список колод" (6c dark, 6e light). The
 * levels are one blue going from faded to bright: lighter for the weak levels on white, darker
 * for the strong ones, so that the strong end always stands out.
 */
@Immutable
data class StackListColors(
    val background: Color,
    val card: Color,
    val cardShadow: Color,
    val cardElevation: Dp,
    val text: Color,
    val secondaryText: Color,
    /** The menu icon, and the Add button's plus and label. */
    val icon: Color,
    val addOutline: Color,
    val accent: Color,
    val onAccent: Color,
    val studyAheadFill: Color,
    val studyAheadText: Color,
    val pin: Color,
    /** New, learning, known, learned. */
    val levels: List<Color>,
    /** The stack screen (handoff "Экран колоды", 9a/9b): a word row, a little flatter than a card. */
    val rowElevation: Dp,
    val plusFill: Color,
    val plusIcon: Color,
    val fabShadow: Color,
    val menu: Color,
    val danger: Color,
)

private val Dark = StackListColors(
    background = Color(0xFF2A2A2E),
    // The handoff's #333338, with 50% more difference from the background (linear light).
    card = Color(0xFF37373C),
    cardShadow = Color.Transparent,
    cardElevation = 0.dp,
    text = Color(0xFFECECEE),
    secondaryText = Color(0xFFA8A8B0),
    icon = Color(0xFFCFCFD4),
    addOutline = Color(0xFF4A4A52),
    accent = Color(0xFF3DB3A6),
    onAccent = Color(0xFF1A1A1C),
    studyAheadFill = Color(0xFF424248),
    studyAheadText = Color(0xFFA8A8B0),
    pin = Color(0xFF7FA8EE),
    levels = listOf(Color(0xFF5E6676), Color(0xFF5E7494), Color(0xFF5684C2), Color(0xFF4A95F2)),
    rowElevation = 0.dp,
    plusFill = Color(0xFF424248),
    plusIcon = Color(0xFFECECEE),
    fabShadow = Color.Black.copy(alpha = 0.35f),
    menu = Color(0xFF3A3A40),
    danger = Color(0xFFF58A98),
)

private val Light = StackListColors(
    background = Color(0xFFF3F3F0),
    card = Color(0xFFFFFFFF),
    // The handoff's shadow is two soft layers; one light one at a low alpha is close.
    cardShadow = Color(0xFF14141E).copy(alpha = 0.10f),
    cardElevation = 4.dp,
    text = Color(0xFF1C1C1F),
    secondaryText = Color(0xFF5E5E66),
    icon = Color(0xFF3A3A40),
    addOutline = Color(0xFFD6D6D2),
    accent = Color(0xFF3DB3A6),
    onAccent = Color(0xFF1A1A1C),
    studyAheadFill = Color(0xFFE6E6E2),
    studyAheadText = Color(0xFF5E5E66),
    pin = Color(0xFF3F7BD0),
    levels = listOf(Color(0xFFC7CEDA), Color(0xFF97ABC9), Color(0xFF5A88C6), Color(0xFF2F6CD0)),
    rowElevation = 2.dp,
    plusFill = Color(0xFFFFFFFF),
    plusIcon = Color(0xFF1C1C1F),
    fabShadow = Color(0xFF14141E).copy(alpha = 0.14f),
    menu = Color(0xFFFFFFFF),
    danger = Color(0xFFB5304F),
)

@Composable
fun stackListColors(dark: Boolean = isSystemInDarkTheme()): StackListColors = if (dark) Dark else Light
