package com.example.android.memoization.ui.composables.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.folderscreen.StackListColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Swiping left slides the row aside and snaps, leaving Delete standing behind it (design handoff
 * "Удаление карточки", 10a). The gesture itself never deletes - however far the row is swiped,
 * it only opens - so a swipe caught by a coat sleeve costs nothing.
 *
 * [open] is held by the list, so that only one row is open at a time: a tap on the open row, or
 * a swipe on another one ([onSwipeStart]), closes it.
 */
@Composable
fun SwipeToReveal(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    colors: StackListColors,
    shape: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSwipeStart: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val actionPx = with(LocalDensity.current) { ACTION_WIDTH.toPx() }
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(open) { offsetX.animateTo(if (open) -actionPx else 0f) }
    val settle: (Boolean) -> Unit = { toOpen ->
        onOpenChange(toOpen)
        scope.launch { offsetX.animateTo(if (toOpen) -actionPx else 0f) }
    }

    val moved = offsetX.value != 0f
    // Clipped to the row's own shape while moved, as in the handoff: the row slides under its
    // own rounded edge rather than over the list's margin. A resting row keeps its shadow.
    Box(modifier = modifier.then(if (moved) Modifier.clip(shape) else Modifier)) {
        // Drawn only while moved, so the red never shows through a resting row's round corners.
        if (moved) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(colors.deleteFill)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(ACTION_WIDTH)
                        .fillMaxHeight()
                        .clickable(role = Role.Button) {
                            onOpenChange(false)
                            onDelete()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Icon(Icons.Outlined.Delete, null, tint = colors.onDeleteFill, modifier = Modifier.size(22.dp))
                    Text(
                        text = stringResource(R.string.delete),
                        color = colors.onDeleteFill,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    enabled = enabled,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offsetX.snapTo((offsetX.value + delta).coerceIn(-actionPx, 0f)) }
                    },
                    onDragStarted = { onSwipeStart() },
                    // Past halfway it stays open, otherwise it falls closed again.
                    onDragStopped = { settle(offsetX.value < -actionPx / 2) },
                )
        ) {
            content()
            if (open) {
                // An open row's tap closes it rather than opening the pair.
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable(remember { MutableInteractionSource() }, indication = null) { settle(false) }
                )
            }
        }
    }
}

private val ACTION_WIDTH = 96.dp
