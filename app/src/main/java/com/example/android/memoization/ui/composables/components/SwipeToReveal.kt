package com.example.android.memoization.ui.composables.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.android.memoization.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Swiping left slides the row aside and snaps, leaving a delete button standing behind it.
 * Nothing is destroyed by the gesture itself - swiping back puts the row where it was, so
 * a swipe caught by a coat sleeve costs nothing.
 */
@Composable
fun SwipeToReveal(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    revealedWidth: Dp = 72.dp,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val revealedPx = with(LocalDensity.current) { revealedWidth.toPx() }
    val offsetX = remember { Animatable(0f) }

    // Clipped so the row slides out of sight rather than over its neighbours, and so the
    // delete button stays hidden behind the row until it is actually swiped.
    Box(modifier = modifier.clipToBounds()) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(revealedWidth)
                .fillMaxHeight()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFB04238))
                .clickable {
                    scope.launch { offsetX.animateTo(0f) }
                    onDelete()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_word),
                tint = Color.White
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(-revealedPx, 0f))
                        }
                    },
                    onDragStopped = {
                        // Past halfway it stays open, otherwise it falls closed again.
                        val target = if (offsetX.value < -revealedPx / 2) -revealedPx else 0f
                        scope.launch { offsetX.animateTo(target) }
                    }
                )
        ) {
            content()
        }
    }
}
