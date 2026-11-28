package com.example.android.memoization.ui.features.folderscreen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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
    /** Delete text and icons on a plain background: the menu item, the trash in selection mode. */
    val danger: Color,
    /** Deleting words (handoff "Удаление карточки", 10a-10d): the fill behind a swiped row and the dialog's Delete. */
    val deleteFill: Color,
    val onDeleteFill: Color,
    val snackbar: Color,
    val onSnackbar: Color,
    val snackbarAction: Color,
    val snackbarShadow: Color,
    val selectedRow: Color,
    val checkboxOutline: Color,
    val dialog: Color,
    val cancelFill: Color,
    /** How much the screen darkens behind the delete dialog. */
    val scrim: Float,
    /** The Deleted section (handoff "Удалённые", 12a-12c): header, chevron, the dashed rows. */
    val deletedHeader: Color,
    val deletedChevron: Color,
    val deletedOutline: Color,
    val deletedName: Color,
    val deletedCaption: Color,
    val restoreOutline: Color,
    val restoreContent: Color,
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
    deleteFill = Color(0xFFE5627A),
    onDeleteFill = Color(0xFF1A1A1C),
    snackbar = Color(0xFFECECEE),
    onSnackbar = Color(0xFF1C1C1F),
    snackbarAction = Color(0xFF13776E),
    snackbarShadow = Color.Black.copy(alpha = 0.4f),
    selectedRow = Color(0xFF28403D),
    checkboxOutline = Color(0xFF6A6A72),
    dialog = Color(0xFF2A2A2E),
    cancelFill = Color(0xFF424248),
    scrim = 0.55f,
    deletedHeader = Color(0xFFCFCFD4),
    deletedChevron = Color(0xFF8D8D95),
    deletedOutline = Color(0xFF44444B),
    deletedName = Color(0xFF9A9AA2),
    deletedCaption = Color(0xFF7A7A82),
    restoreOutline = Color(0xFF4A4A52),
    restoreContent = Color(0xFFCFCFD4),
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
    deleteFill = Color(0xFFEC6A80),
    onDeleteFill = Color(0xFF1A1A1C),
    snackbar = Color(0xFF2A2A2E),
    onSnackbar = Color(0xFFECECEE),
    snackbarAction = Color(0xFF6FCBBF),
    snackbarShadow = Color(0xFF14141E).copy(alpha = 0.18f),
    selectedRow = Color(0xFFE1F4F1),
    checkboxOutline = Color(0xFFB5B5B0),
    dialog = Color(0xFFFFFFFF),
    cancelFill = Color(0xFFECECE8),
    // The handoff's scrim is a dark blue-grey at 40%; the window dim is black, at the same strength.
    scrim = 0.4f,
    deletedHeader = Color(0xFF3A3A40),
    deletedChevron = Color(0xFF7A7A82),
    deletedOutline = Color(0xFFD0D0CB),
    deletedName = Color(0xFF7A7A82),
    deletedCaption = Color(0xFF9A9AA2),
    restoreOutline = Color(0xFFD6D6D2),
    restoreContent = Color(0xFF3A3A40),
)

@Composable
fun stackListColors(dark: Boolean = isSystemInDarkTheme()): StackListColors = if (dark) Dark else Light

/** How bright Study is at the least urgent review: this share of the accent over the grey. */
private const val MIN_STUDY_BRIGHTNESS = 0.35f

/**
 * Study's fill and text for a review [urgency] from 0 to 1: a muted teal when little is about to
 * be forgotten, the full accent - as it always was - when much is. The text takes whichever of
 * the dark and the light gives more contrast on that fill.
 */
fun StackListColors.study(urgency: Float): Pair<Color, Color> {
    val fill = lerp(studyAheadFill, accent, MIN_STUDY_BRIGHTNESS + (1 - MIN_STUDY_BRIGHTNESS) * urgency.coerceIn(0f, 1f))
    val content = listOf(onAccent, text).maxBy { contrast(it, fill) }
    return fill to content
}

private fun contrast(a: Color, b: Color): Float {
    val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
    return (light + 0.05f) / (dark + 0.05f)
}
