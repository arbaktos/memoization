package com.example.android.memoization.ui.features.stackscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.folderscreen.StackListColors
import kotlinx.coroutines.withTimeoutOrNull

/*
 * Deleting words on the stack screen (design handoff "Удаление карточки", 10b-10d). Deleting
 * hides the pairs; the undo puts them back with their progress.
 */

/** Selection mode: the picked pairs, or null outside it, and what can be done with them. */
@Immutable
class WordSelection(
    val selected: Set<Long>?,
    val onStart: (wordPairId: Long) -> Unit,
    val onToggle: (wordPairId: Long) -> Unit,
    val onClear: () -> Unit,
    val onDelete: () -> Unit,
) {
    companion object {
        val NONE = WordSelection(null, {}, {}, {}, {})
    }
}

/** The header while selecting (10c): close, how many are picked, and delete. Back closes too. */
@Composable
fun SelectionAppBar(count: Int, colors: StackListColors, onClose: () -> Unit, onDelete: () -> Unit) {
    BackHandler(onBack = onClose)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Filled.Close, stringResource(R.string.close_selection), tint = colors.icon)
        }
        Text(
            text = stringResource(R.string.selected_count, count),
            color = colors.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Outlined.Delete, stringResource(R.string.delete_selected), tint = colors.danger)
        }
    }
}

/** The round check in front of a row while selecting. */
@Composable
fun SelectCircle(selected: Boolean, colors: StackListColors) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .then(
                if (selected) Modifier.background(colors.accent, CircleShape)
                else Modifier.border(2.dp, colors.checkboxOutline, CircleShape)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Filled.Check, null, tint = colors.onAccent, modifier = Modifier.size(16.dp))
    }
}

/** Asked before deleting the selection (10d); one word or many, the undo still follows. */
@Composable
fun DeleteWordsDialog(count: Int, colors: StackListColors, onCancel: () -> Unit, onDelete: () -> Unit) =
    ConfirmDialog(
        title = pluralStringResource(R.plurals.delete_words_title, count, count),
        text = stringResource(R.string.delete_words_text),
        confirm = stringResource(R.string.delete),
        colors = colors,
        onCancel = onCancel,
        onConfirm = onDelete,
    )

/** A question with Cancel and a red action, in the delete dialog's look (10b). */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirm: String,
    colors: StackListColors,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(colors.scrim) }
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(colors.dialog, RoundedCornerShape(28.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                color = colors.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = text,
                color = colors.secondaryText,
                fontSize = 15.sp,
                lineHeight = 22.5.sp,
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DialogButton(stringResource(R.string.cancel), colors.cancelFill, colors.text, onCancel, Modifier.weight(1f))
                DialogButton(confirm, colors.deleteFill, colors.onDeleteFill, onConfirm, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DialogButton(label: String, fill: Color, text: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(fill)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/** The snackbar after a delete (10b): what went, and Undo. */
@Composable
fun UndoSnackbar(data: SnackbarData, colors: StackListColors) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp)
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(12.dp, shape, ambientColor = colors.snackbarShadow, spotColor = colors.snackbarShadow)
            .background(colors.snackbar, shape)
            .padding(start = 18.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = data.visuals.message,
            color = colors.onSnackbar,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
        )
        data.visuals.actionLabel?.let { label ->
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { data.performAction() }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = colors.snackbarAction, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Shows [message] with Undo for five seconds, replacing a snackbar already up - a new delete
 * closes the old one's chance to undo. True when Undo was tapped.
 */
suspend fun SnackbarHostState.showUndo(message: String, undo: String): Boolean {
    currentSnackbarData?.dismiss()
    return withTimeoutOrNull(UNDO_MS) {
        showSnackbar(message, undo, duration = SnackbarDuration.Indefinite)
    } == SnackbarResult.ActionPerformed
}

private const val UNDO_MS = 5_000L
