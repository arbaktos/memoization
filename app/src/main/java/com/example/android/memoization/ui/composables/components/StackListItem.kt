package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.StackLevels
import com.example.android.memoization.data.model.levels
import com.example.android.memoization.domain.session.reviewUrgency
import com.example.android.memoization.ui.features.folderscreen.StackListColors
import com.example.android.memoization.ui.features.folderscreen.study

/**
 * One stack in the list (design handoff "Список колод", 6c/6e): name and size, the pairs by
 * level as four bars, Study and Add. Study is as bright as the review is urgent; with nothing
 * due, it turns grey and studies ahead.
 */
@Composable
fun StackListItem(
    stack: MemoStack,
    practice: PracticeSides,
    colors: StackListColors,
    modifier: Modifier = Modifier,
    onPin: () -> Unit = {},
    onAdd: () -> Unit = {},
    onPlay: (ahead: Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    // Not remembered: MemoStack.words sits outside the data-class constructor, so no key would
    // ever change, and a remembered value would freeze at first composition.
    val hasDue = stack.hasDue(practice)
    val levels = stack.levels(practice)
    val (studyFill, studyText) = colors.study(if (hasDue) stack.reviewUrgency(practice) else 0f)
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(colors.cardElevation, shape, ambientColor = colors.cardShadow, spotColor = colors.cardShadow)
            .clip(shape)
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stack.name,
                    color = colors.text,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(R.plurals.stack_word_count, stack.words.size, stack.words.size),
                    color = colors.secondaryText,
                    fontSize = 14.sp,
                )
            }
            PinToggle(pinned = stack.pinnedTime != null, colors = colors, onPin = onPin)
        }
        LevelBars(levels, colors)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val buttonShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(buttonShape)
                    .background(if (hasDue) studyFill else colors.studyAheadFill)
                    .clickable(role = Role.Button) { onPlay(!hasDue) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(if (hasDue) R.string.study else R.string.study_anyway),
                    color = if (hasDue) studyText else colors.studyAheadText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(buttonShape)
                    .border(BorderStroke(1.5.dp, colors.addOutline), buttonShape)
                    .clickable(role = Role.Button, onClick = onAdd)
                    .padding(start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = colors.icon, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.add),
                    color = colors.icon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * Filled in blue when the stack is pinned, an outline otherwise - the only way to pin, so it is
 * always there. The touch target is wider than the 18dp icon and reaches into the card padding.
 */
@Composable
private fun PinToggle(pinned: Boolean, colors: StackListColors, onPin: () -> Unit) {
    Box(
        modifier = Modifier
            .offset(x = 13.dp)
            .size(44.dp)
            .clip(CircleShape)
            .toggleable(value = pinned, role = Role.Switch, onValueChange = { onPin() }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = stringResource(R.string.pin_stack),
            tint = if (pinned) colors.pin else colors.secondaryText,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Four bars on a shared scale - the tallest level fills the 36dp - with no track behind them. */
@Composable
private fun LevelBars(levels: StackLevels, colors: StackListColors) {
    val counts = levels.counts
    val max = counts.max()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        counts.forEachIndexed { index, count ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BAR_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (count > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(BAR_HEIGHT * (count.toFloat() / max))
                                .background(colors.levels[index], RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        )
                    }
                }
                Text(
                    text = stringResource(LEVEL_LABELS[index], count),
                    color = colors.secondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

private val BAR_HEIGHT = 36.dp

private val LEVEL_LABELS = listOf(
    R.string.stack_level_new,
    R.string.stack_level_learning,
    R.string.stack_level_known,
    R.string.stack_level_learned,
)
