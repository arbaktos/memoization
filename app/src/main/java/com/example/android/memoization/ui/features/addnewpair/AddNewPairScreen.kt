package com.example.android.memoization.ui.features.addnewpair

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.extensions.findActivity
import com.example.android.memoization.ui.composables.components.ShowToast
import com.example.android.memoization.ui.features.folderscreen.stackListColors
import com.example.android.memoization.ui.features.stackscreen.UndoSnackbar
import kotlinx.coroutines.launch

/**
 * Adding a word, or editing one (design handoff "Добавление карточки", 11b): the word and its
 * meaning on one card, like the two sides in a session, with translate on the line between them
 * and the save buttons always above the keyboard.
 *
 * Closing keeps whatever was typed as a draft for the next visit. Editing has Save alone: Save
 * & next is for adding words in a row.
 */
@Composable
fun AddNewPairScreen(
    navController: NavController,
    viewModel: AddNewPairViewModel = hiltViewModel(),
) {
    val toastMessage by viewModel.toastMessage.observeAsState()
    ShowToast(text = toastMessage)

    val colors = addCardColors()
    val word by viewModel.word.collectAsStateWithLifecycle()
    val meaning by viewModel.meaning.collectAsStateWithLifecycle()
    val canSave by viewModel.canSave.collectAsStateWithLifecycle()
    val translating by viewModel.translating.collectAsStateWithLifecycle()
    val autoTranslated by viewModel.autoTranslated.collectAsStateWithLifecycle()
    val fromDraft by viewModel.fromDraft.collectAsStateWithLifecycle()
    val stackName by viewModel.stackName.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val wordFocus = remember { FocusRequester() }
    val meaningFocus = remember { FocusRequester() }

    // Nothing to go back to means the pair came from a share: close, and the learner is back in
    // the app the word came from.
    val leave = { if (!navController.popBackStack()) context.findActivity()?.finish() }
    val close: () -> Unit = {
        scope.launch {
            viewModel.keepDraft()
            leave()
        }
    }
    val save: () -> Unit = {
        if (canSave) scope.launch {
            keyboard?.hide()
            viewModel.save()
            leave()
        }
    }
    val savedMessage = stringResource(R.string.saved)
    val saveAndNext: () -> Unit = {
        if (canSave) scope.launch {
            viewModel.saveAndNext()
            wordFocus.requestFocus()
            snackbarHostState.currentSnackbarData?.dismiss()
            launch { snackbarHostState.showSnackbar(savedMessage, duration = SnackbarDuration.Short) }
        }
    }
    BackHandler(onBack = close)

    LaunchedEffect(Unit) {
        // A shared word is already in; the cursor belongs in the field still empty.
        if (viewModel.sharedWord != null) meaningFocus.requestFocus() else wordFocus.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Header(
                title = stringResource(if (viewModel.editMode) R.string.edit_word_title else R.string.new_word_title),
                stackName = stackName,
                colors = colors,
                onClose = close,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (fromDraft) DraftLine(colors, onClear = viewModel::clearDraft)
                WordCard(
                    word = word,
                    meaning = meaning,
                    wordLanguage = viewModel.wordLanguage,
                    meaningLanguage = viewModel.meaningLanguage,
                    wordKeyboardLocale = viewModel.wordKeyboardLocale,
                    meaningKeyboardLocale = viewModel.meaningKeyboardLocale,
                    canTranslate = viewModel.needsTranslation(),
                    translating = translating,
                    autoTranslated = autoTranslated,
                    colors = colors,
                    wordFocus = wordFocus,
                    meaningFocus = meaningFocus,
                    onWordChange = viewModel::onWordChange,
                    onMeaningChange = viewModel::onMeaningChange,
                    onTranslate = viewModel::onTranslate,
                    // Done on the keyboard is Save, adding or editing; Save & next is the button.
                    onDone = save,
                )
            }
            SaveButtons(
                enabled = canSave,
                colors = colors,
                onSaveAndNext = if (viewModel.editMode) null else saveAndNext,
                onSave = save,
            )
        }
        // Above the buttons, which stay where they are.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = BUTTON_HEIGHT + 12.dp),
        ) { data -> UndoSnackbar(data, stackListColors()) }
    }
}

@Composable
private fun Header(title: String, stackName: String?, colors: AddCardColors, onClose: () -> Unit) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Filled.Close, stringResource(R.string.close), tint = colors.closeIcon, modifier = Modifier.size(24.dp))
        }
        Text(
            text = title,
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (stackName != null) {
            Text(
                text = stackName,
                color = colors.muted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

/** Shown while the fields hold last time's unsaved word: one tap empties them. */
@Composable
private fun DraftLine(colors: AddCardColors, onClear: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.draft_restored),
            color = colors.muted,
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )
        Text(
            text = stringResource(R.string.clear),
            color = colors.accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onClear)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun WordCard(
    word: String,
    meaning: String,
    wordLanguage: String?,
    meaningLanguage: String?,
    wordKeyboardLocale: String?,
    meaningKeyboardLocale: String?,
    canTranslate: Boolean,
    translating: Boolean,
    autoTranslated: Boolean,
    colors: AddCardColors,
    wordFocus: FocusRequester,
    meaningFocus: FocusRequester,
    onWordChange: (String) -> Unit,
    onMeaningChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onDone: () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    // Where the line between the sides is; the translate button is centred on it.
    var lineY by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(colors.cardElevation, shape, ambientColor = colors.cardShadow, spotColor = colors.cardShadow)
            .background(colors.card, shape)
            .border(BorderStroke(1.dp, colors.outline), shape)
    ) {
        Column {
            CardSide(
                label = fieldLabel(wordLanguage, stringResource(R.string.word_to_learn)),
                colors = colors,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp),
            ) {
                CardField(
                    value = word,
                    onValueChange = onWordChange,
                    textStyle = TextStyle(color = colors.text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
                    colors = colors,
                    keyboardLocale = wordKeyboardLocale,
                    imeAction = ImeAction.Next,
                    onImeAction = { meaningFocus.requestFocus() },
                    modifier = Modifier.focusRequester(wordFocus),
                )
            }
            // The line stops 10dp short of the translate button, which sits on it.
            Box(
                Modifier
                    .padding(start = 18.dp, end = if (canTranslate) 16.dp + TRANSLATE_SIZE + 10.dp else 18.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.outline)
                    .onPlaced { lineY = it.positionInParent().y.roundToInt() }
            )
            CardSide(
                label = fieldLabel(meaningLanguage, stringResource(R.string.word2_label)),
                colors = colors,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
            ) {
                CardField(
                    value = meaning,
                    onValueChange = onMeaningChange,
                    textStyle = TextStyle(color = colors.text, fontSize = 18.sp),
                    colors = colors,
                    keyboardLocale = meaningKeyboardLocale,
                    imeAction = ImeAction.Done,
                    onImeAction = onDone,
                    modifier = Modifier.focusRequester(meaningFocus),
                )
                if (autoTranslated) {
                    Text(stringResource(R.string.auto_translated_hint), color = colors.muted, fontSize = 12.sp)
                }
            }
        }
        // Over the card rather than inside the line: it reaches into both sides, and an element
        // spilling out of a 1dp row is not reliably redrawn.
        if (canTranslate) {
            TranslateButton(
                enabled = word.isNotBlank() && !translating,
                translating = translating,
                colors = colors,
                onClick = onTranslate,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(-(16.dp - RING).roundToPx(), lineY - (TRANSLATE_SIZE / 2 + RING).roundToPx()) },
            )
        }
    }
}

private fun fieldLabel(language: String?, label: String): String =
    (if (language != null) "$language · $label" else label).uppercase()

@Composable
private fun CardSide(
    label: String,
    colors: AddCardColors,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.06.em,
        )
        content()
    }
}

@Composable
private fun CardField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle,
    colors: AddCardColors,
    keyboardLocale: String?,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier,
) {
    // Several lines for a long word or meaning, but never a line break: Enter, from a hardware
    // keyboard or a keyboard without the action key, does what the action key does.
    BasicTextField(
        value = value,
        onValueChange = { text ->
            if ('\n' in text) {
                onValueChange(text.replace("\n", ""))
                onImeAction()
            } else {
                onValueChange(text)
            }
        },
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
            hintLocales = keyboardLocale?.let { LocaleList(Locale(it)) },
        ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        modifier = modifier.fillMaxWidth(),
    )
}

private val TRANSLATE_SIZE = 40.dp
private val RING = 4.dp

/** 40dp, with a ring of the card's colour to part it from the line; a spinner while it works. */
@Composable
private fun TranslateButton(
    enabled: Boolean,
    translating: Boolean,
    colors: AddCardColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(TRANSLATE_SIZE + RING * 2)
            .background(colors.card, CircleShape)
            .padding(RING)
            // Drawn straight onto the card: with the circle in a clip layer and the dimming in an
            // alpha layer, the fill vanished once the button turned on after the first frame.
            .drawBehind { drawCircle(colors.translateFill, alpha = if (enabled || translating) 1f else 0.4f) }
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = null,
                indication = ripple(bounded = false, radius = TRANSLATE_SIZE / 2),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (translating) {
            CircularProgressIndicator(color = colors.accent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Icon(
                imageVector = Icons.Filled.Translate,
                contentDescription = stringResource(R.string.translate_btn_desc),
                tint = colors.accent.copy(alpha = if (enabled) 1f else 0.4f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Save & next (adding only) and Save, 12dp above the keyboard; greyed until both fields are filled. */
@Composable
private fun SaveButtons(enabled: Boolean, colors: AddCardColors, onSaveAndNext: (() -> Unit)?, onSave: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val button = Modifier
        .height(BUTTON_HEIGHT)
        .alpha(if (enabled) 1f else 0.4f)
        .clip(shape)
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (onSaveAndNext != null) {
            Box(
                modifier = button
                    .weight(1f)
                    .border(BorderStroke(1.5.dp, colors.nextOutline), shape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onSaveAndNext),
                contentAlignment = Alignment.Center,
            ) {
                ButtonText(stringResource(R.string.save_and_next), colors.nextText)
            }
        }
        Row(
            modifier = button
                .weight(1f)
                .background(colors.saveFill)
                .clickable(enabled = enabled, role = Role.Button, onClick = onSave),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Icon(Icons.Filled.Check, null, tint = colors.onSave, modifier = Modifier.size(18.dp))
            ButtonText(stringResource(R.string.save), colors.onSave)
        }
    }
}

@Composable
private fun ButtonText(text: String, color: Color) {
    Text(text = text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
}

private val BUTTON_HEIGHT: Dp = 52.dp
