package com.example.android.memoization.ui.features.memoizationscreen

import android.view.HapticFeedbackConstants
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.domain.scheduler.Rating
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.domain.session.SessionSide
import com.example.android.memoization.ui.composables.components.StackCompleteDialog
import com.example.android.memoization.ui.navigateToFolderScreen

private const val FLIP_MS = 550
private const val BUTTONS_MS = 220
private const val BAR_MS = 350

@Composable
fun MemorizationScreen(
    navController: NavController,
) {
    val viewModel: MemoizationViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    MemorizationBody(
        screen = state,
        onRate = viewModel::onRate,
        onHard = viewModel::onHard,
        onCheck = viewModel::onCheck,
        onStop = viewModel::onStop,
        // The same as the system Back: the session is closed as left, nothing to confirm.
        onClose = { backDispatcher?.onBackPressed() },
        onComplete = { navController.navigateToFolderScreen() }
    )
}

@Composable
fun MemorizationBody(
    screen: MemoizationViewModel.Screen?,
    onRate: (Rating) -> Unit,
    onHard: () -> Unit,
    onCheck: (right: Boolean) -> Unit,
    onStop: () -> Unit,
    onClose: () -> Unit,
    onComplete: () -> Unit,
) {
    val colors = sessionColors()
    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // null = still loading the stack; nothing to draw yet.
        val current = screen ?: return@Box
        val session = current.session
        if (session.isFinished) {
            StackCompleteDialog(waiting = session.waiting, onClick = onComplete)
            return@Box
        }
        val item = session.current ?: return@Box

        // A fresh angle for every side on screen, the serial included: after a Forgot the same
        // side can come straight back. The next side so starts face-up at once and never turns
        // back through its own answer.
        val angle = remember(item.id, session.serial) { Animatable(0f) }
        LaunchedEffect(angle, current.checking) {
            angle.animateTo(
                targetValue = if (current.checking) 180f else 0f,
                animationSpec = tween(FLIP_MS, easing = FastOutSlowInEasing),
            )
        }
        // No answer while the card is turning, so a double tap cannot rate twice.
        val enabled = !angle.isRunning

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Header(stopOffered = session.stopOffered, colors = colors, onClose = onClose, onStop = onStop)
            SessionProgress(session = session, colors = colors)
            SessionCard(
                item = item,
                angle = angle.value,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            AnswerButtons(
                checking = current.checking,
                enabled = enabled,
                colors = colors,
                onRate = onRate,
                onHard = onHard,
                onCheck = onCheck,
            )
        }
    }
}

/** Close on the left; "Enough for today" on the right once it is offered. */
@Composable
private fun Header(stopOffered: Boolean, colors: SessionColors, onClose: () -> Unit, onStop: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.end_session),
                tint = colors.closeIcon,
                modifier = Modifier.size(22.dp),
            )
        }
        if (stopOffered) {
            TextButton(onClick = onStop, modifier = Modifier.align(Alignment.CenterEnd)) {
                Text(
                    text = stringResource(R.string.enough_for_today),
                    color = colors.secondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * One bar for the whole session, filled from the left by the sides closed so far in the colour
 * of how each went, with the counts under it. Nothing says how many are left: a session has no
 * size, and the empty part of the track shows it well enough.
 */
@Composable
private fun SessionProgress(session: MemorizationSession.State, colors: SessionColors) {
    val closed = session.closed
    val total = session.total.coerceAtLeast(1).toFloat()
    val good by animateFloatAsState(closed.good / total, tween(BAR_MS), label = "good")
    val hard by animateFloatAsState(closed.hard / total, tween(BAR_MS), label = "hard")
    val forgot by animateFloatAsState(closed.forgot / total, tween(BAR_MS), label = "forgot")
    val progressLabel = stringResource(R.string.session_progress, session.total - session.remaining, session.total)
    val legendLabel = stringResource(R.string.session_legend, closed.good, closed.hard, closed.forgot)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.track)
                .semantics { contentDescription = progressLabel }
        ) {
            val width = maxWidth
            Row(Modifier.fillMaxHeight()) {
                Box(Modifier.fillMaxHeight().width(width * good).background(colors.goodBar))
                Box(Modifier.fillMaxHeight().width(width * hard).background(colors.hardBar))
                Box(Modifier.fillMaxHeight().width(width * forgot).background(colors.forgotBar))
            }
        }
        Row(
            modifier = Modifier.clearAndSetSemantics { contentDescription = legendLabel },
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendItem(colors.goodBar, closed.good, colors)
            LegendItem(colors.hardBar, closed.hard, colors)
            LegendItem(colors.forgotBar, closed.forgot, colors)
        }
    }
}

@Composable
private fun LegendItem(dot: Color, count: Int, colors: SessionColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(7.dp)
                .background(dot, CircleShape)
        )
        Text(text = count.toString(), color = colors.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * The side on screen. It turns over only after Hard, so the learner can check what they
 * recalled; past the half-turn the back is drawn, itself turned so it does not read mirrored.
 */
@Composable
private fun SessionCard(item: SessionSide, angle: Float, colors: SessionColors, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    val showBack = angle > 90f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = angle
                cameraDistance = 12f * density
            }
            .shadow(colors.shadowElevation, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
            .background(colors.card, shape)
            .then(if (showBack) Modifier.border(1.dp, colors.backBorder, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (showBack) {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = item.front, color = colors.mutedText, fontSize = 16.sp, textAlign = TextAlign.Center)
                Text(
                    text = item.back,
                    color = colors.text,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Text(
                text = item.front,
                color = colors.text,
                fontSize = 34.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            )
        }
    }
}

/**
 * Forgot / Hard / Good on the front; after Hard, the check in their place. The block has a
 * fixed height so the card does not move when one set gives way to the other.
 */
@Composable
private fun AnswerButtons(
    checking: Boolean,
    enabled: Boolean,
    colors: SessionColors,
    onRate: (Rating) -> Unit,
    onHard: () -> Unit,
    onCheck: (right: Boolean) -> Unit,
) {
    val view = LocalView.current
    val tap: (() -> Unit) -> () -> Unit = { action ->
        {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            action()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(
            targetState = checking,
            transitionSpec = {
                (fadeIn(tween(BUTTONS_MS)) + slideInVertically(tween(BUTTONS_MS)) { it / 6 }) togetherWith
                    (fadeOut(tween(BUTTONS_MS)) + slideOutVertically(tween(BUTTONS_MS)) { it / 6 })
            },
            contentAlignment = Alignment.BottomCenter,
            label = "answer buttons",
        ) { turned ->
            if (turned) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.check_question),
                        color = colors.question,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnswerButton(
                            label = stringResource(R.string.check_no),
                            textColor = colors.noText,
                            outline = colors.noOutline,
                            enabled = enabled,
                            onClick = tap { onCheck(false) },
                            modifier = Modifier.weight(1f),
                        )
                        AnswerButton(
                            label = stringResource(R.string.check_yes),
                            textColor = colors.onResult,
                            fill = colors.goodFill,
                            enabled = enabled,
                            onClick = tap { onCheck(true) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                // Rising from Forgot to Good, so Good sits under the right thumb.
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnswerButton(
                        label = stringResource(R.string.wrong),
                        textColor = colors.onResult,
                        fill = colors.forgotFill,
                        enabled = enabled,
                        onClick = tap { onRate(Rating.Again) },
                        modifier = Modifier.weight(1f),
                    )
                    AnswerButton(
                        label = stringResource(R.string.hard),
                        textColor = colors.onResult,
                        fill = colors.hardFill,
                        enabled = enabled,
                        onClick = tap(onHard),
                        modifier = Modifier.weight(1f),
                    )
                    AnswerButton(
                        label = stringResource(R.string.good),
                        textColor = colors.onResult,
                        fill = colors.goodFill,
                        enabled = enabled,
                        onClick = tap { onRate(Rating.Good) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** A filled button, or an outlined one when [fill] is null; the label sits inside it. */
@Composable
private fun AnswerButton(
    label: String,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Color? = null,
    outline: Color? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(shape)
            .background(fill ?: Color.Transparent)
            .then(if (outline != null) Modifier.border(2.dp, outline, shape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
