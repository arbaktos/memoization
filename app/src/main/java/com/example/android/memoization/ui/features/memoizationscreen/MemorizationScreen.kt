package com.example.android.memoization.ui.features.memoizationscreen

import android.view.HapticFeedbackConstants
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
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.domain.session.SessionSide
import com.example.android.memoization.ui.composables.components.StackCompleteDialog
import com.example.android.memoization.ui.features.memoizationscreen.MemoizationViewModel.Turned
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

    MemorizationBody(
        screen = state,
        onEasy = viewModel::onEasy,
        onHard = viewModel::onHard,
        onForgot = viewModel::onForgot,
        onCheck = viewModel::onCheck,
        onNext = viewModel::onNext,
        onStop = viewModel::onStop,
        onComplete = { navController.navigateToFolderScreen() }
    )
}

@Composable
fun MemorizationBody(
    screen: MemoizationViewModel.Screen?,
    onEasy: () -> Unit,
    onHard: () -> Unit,
    onForgot: () -> Unit,
    onCheck: (right: Boolean) -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
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
        val item = current.card
        if (session.isFinished || item == null) {
            StackCompleteDialog(waiting = session.waiting, onClick = onComplete)
            return@Box
        }

        // A fresh angle for every side that comes onto the card, the serial included: after a
        // Forgot the same side can come straight back. The next side so starts face-up at once
        // and never turns back through its own answer.
        val angle = remember(item.id, current.cardSerial) { Animatable(0f) }
        LaunchedEffect(angle, current.turned) {
            angle.animateTo(
                targetValue = if (current.turned != null) 180f else 0f,
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
            if (session.stopOffered) Header(colors = colors, onStop = onStop)
            SessionProgress(session = session, colors = colors)
            SessionCard(
                item = item,
                angle = angle.value,
                backBorder = if (current.turned == Turned.FORGOT) colors.forgotBorder else colors.backBorder,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            AnswerButtons(
                turned = current.turned,
                enabled = enabled,
                colors = colors,
                onEasy = onEasy,
                onHard = onHard,
                onForgot = onForgot,
                onCheck = onCheck,
                onNext = onNext,
            )
        }
    }
}

/** "Enough for today", in a row of its own that is there only while it is offered. */
@Composable
private fun Header(colors: SessionColors, onStop: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        TextButton(onClick = onStop) {
            Text(
                text = stringResource(R.string.enough_for_today),
                color = colors.secondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * One bar for the whole session, filled from the left by the sides answered so far in the
 * colour of how each went - a Forgot at once - with the counts under it. Nothing says how many
 * are left: a session has no size, and the empty part of the track shows it well enough.
 */
@Composable
private fun SessionProgress(session: MemorizationSession.State, colors: SessionColors) {
    val results = session.results
    val total = session.total.coerceAtLeast(1).toFloat()
    val good by animateFloatAsState(results.easy / total, tween(BAR_MS), label = "good")
    val hard by animateFloatAsState(results.hard / total, tween(BAR_MS), label = "hard")
    val forgot by animateFloatAsState(results.forgot / total, tween(BAR_MS), label = "forgot")
    val progressLabel = stringResource(R.string.session_progress, session.total - session.remaining, session.total)
    val legendLabel = stringResource(R.string.session_legend, results.easy, results.hard, results.forgot)

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
            LegendItem(colors.goodBar, results.easy, colors)
            LegendItem(colors.hardBar, results.hard, colors)
            LegendItem(colors.forgotBar, results.forgot, colors)
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
 * recalled, and after Forgot, so they see what they could not; past the half-turn the back is
 * drawn, itself turned so it does not read mirrored, in a frame of [backBorder].
 */
@Composable
private fun SessionCard(
    item: SessionSide,
    angle: Float,
    backBorder: Color,
    colors: SessionColors,
    modifier: Modifier,
) {
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
            .then(if (showBack) Modifier.border(1.dp, backBorder, shape) else Modifier),
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
 * Forgot / Hard / Easy on the front; after Hard, the check in their place; after Forgot, the
 * way on. The block has a fixed height so the card does not move when one set gives way to
 * another.
 */
@Composable
private fun AnswerButtons(
    turned: Turned?,
    enabled: Boolean,
    colors: SessionColors,
    onEasy: () -> Unit,
    onHard: () -> Unit,
    onForgot: () -> Unit,
    onCheck: (right: Boolean) -> Unit,
    onNext: () -> Unit,
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
            targetState = turned,
            transitionSpec = {
                (fadeIn(tween(BUTTONS_MS)) + slideInVertically(tween(BUTTONS_MS)) { it / 6 }) togetherWith
                    (fadeOut(tween(BUTTONS_MS)) + slideOutVertically(tween(BUTTONS_MS)) { it / 6 })
            },
            contentAlignment = Alignment.BottomCenter,
            label = "answer buttons",
        ) { shown ->
            when (shown) {
                Turned.FORGOT -> AnswerButton(
                    label = stringResource(R.string.next),
                    textColor = colors.noText,
                    outline = colors.noOutline,
                    enabled = enabled,
                    onClick = tap(onNext),
                    modifier = Modifier.fillMaxWidth(),
                )
                Turned.CHECK -> {
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
                }
                null -> {
                    // Rising from Forgot to Easy, so Easy sits under the right thumb.
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AnswerButton(
                            label = stringResource(R.string.wrong),
                            textColor = colors.onResult,
                            fill = colors.forgotFill,
                            enabled = enabled,
                            onClick = tap(onForgot),
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
                            label = stringResource(R.string.easy),
                            textColor = colors.onResult,
                            fill = colors.goodFill,
                            enabled = enabled,
                            onClick = tap(onEasy),
                            modifier = Modifier.weight(1f),
                        )
                    }
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
