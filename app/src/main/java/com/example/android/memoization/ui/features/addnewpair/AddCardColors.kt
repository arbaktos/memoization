package com.example.android.memoization.ui.features.addnewpair

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The add-word screen's palette, from the design handoff "Добавление карточки" (11b). */
@Immutable
data class AddCardColors(
    val background: Color,
    val card: Color,
    /** The card's outline and the line between word and meaning. */
    val outline: Color,
    val cardShadow: Color,
    val cardElevation: Dp,
    val translateFill: Color,
    /** The translate icon, the cursor and the draft's Clear. */
    val accent: Color,
    val text: Color,
    /** Field labels, the stack's name, the auto-translation note. */
    val muted: Color,
    val closeIcon: Color,
    val saveFill: Color,
    val onSave: Color,
    val nextOutline: Color,
    val nextText: Color,
)

private val Dark = AddCardColors(
    background = Color(0xFF2A2A2E),
    card = Color(0xFF3A3A41),
    outline = Color(0xFF4E4E57),
    cardShadow = Color.Black.copy(alpha = 0.35f),
    cardElevation = 16.dp,
    translateFill = Color(0xFF4A4A52),
    accent = Color(0xFF3DB3A6),
    text = Color(0xFFECECEE),
    muted = Color(0xFF8D8D95),
    closeIcon = Color(0xFFCFCFD4),
    saveFill = Color(0xFF3DB3A6),
    onSave = Color(0xFF1A1A1C),
    nextOutline = Color(0xFF4A4A52),
    nextText = Color(0xFFCFCFD4),
)

private val Light = AddCardColors(
    background = Color(0xFFF3F3F0),
    card = Color(0xFFFFFFFF),
    outline = Color(0xFFE2E2DE),
    // The handoff's shadow is two soft layers; one light one at a low alpha is close.
    cardShadow = Color(0xFF14141E).copy(alpha = 0.08f),
    cardElevation = 8.dp,
    translateFill = Color(0xFFF0F0EC),
    accent = Color(0xFF1F8E83),
    text = Color(0xFF1C1C1F),
    muted = Color(0xFF7A7A82),
    closeIcon = Color(0xFF3A3A40),
    saveFill = Color(0xFF3DB3A6),
    onSave = Color(0xFF1A1A1C),
    nextOutline = Color(0xFFD6D6D2),
    nextText = Color(0xFF3A3A40),
)

@Composable
fun addCardColors(dark: Boolean = isSystemInDarkTheme()): AddCardColors = if (dark) Dark else Light
