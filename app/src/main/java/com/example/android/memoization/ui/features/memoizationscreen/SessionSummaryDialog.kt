package com.example.android.memoization.ui.features.memoizationscreen

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.android.memoization.R
import com.example.android.memoization.domain.session.Mistakes
import com.example.android.memoization.domain.session.SessionSummary

/**
 * The end of a session, however it ended (design handoff "Итог сессии"): how many sides were
 * practised, how each was first answered, and a message chosen by how the mistakes compare with
 * what the scheduler expected - never a count of them, and never in red: mistakes read as part
 * of learning, not as a failure.
 */
@Composable
fun SessionSummaryDialog(
    summary: SessionSummary,
    waiting: Int,
    colors: SessionColors,
    onDone: () -> Unit,
) {
    Dialog(onDismissRequest = onDone, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val shape = RoundedCornerShape(28.dp)
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .shadow(colors.shadowElevation, shape, ambientColor = colors.shadow, spotColor = colors.shadow)
                .background(colors.dialog, shape)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (waiting == 0) {
                        stringResource(R.string.stack_complete)
                    } else {
                        pluralStringResource(R.plurals.session_complete_waiting, waiting, waiting)
                    },
                    color = colors.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pluralStringResource(R.plurals.summary_practised, summary.practised, summary.practised),
                    color = colors.secondaryText,
                    fontSize = 15.sp,
                )
            }
            val answers = listOf(
                Triple(R.string.easy, summary.firstAnswers.easy, colors.goodBar),
                Triple(R.string.hard, summary.firstAnswers.hard, colors.hardBar),
                Triple(R.string.wrong, summary.firstAnswers.forgot, colors.forgotBar),
            ).filter { (_, count, _) -> count > 0 }
            ResultsBar(answers.map { (_, count, color) -> count to color })
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                answers.forEach { (label, count, color) -> LegendRow(label, count, color, colors) }
            }
            summary.mistakes?.let { MessageBox(it.verdict, colors) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.goodFill)
                    .clickable(role = Role.Button, onClick = onDone),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    color = colors.onResult,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** One segment per kind of answer, as wide as its count, 2dp apart; fills left to right on show. */
@Composable
private fun ResultsBar(segments: List<Pair<Int, Color>>) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(durationMillis = 450, delayMillis = 200)) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .drawWithContent { clipRect(right = size.width * reveal.value) { this@drawWithContent.drawContent() } },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        segments.forEach { (count, color) ->
            Box(
                Modifier
                    .weight(count.toFloat())
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun LegendRow(@StringRes label: Int, count: Int, dot: Color, colors: SessionColors) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(8.dp)
                .background(dot, CircleShape)
        )
        Text(
            text = stringResource(label),
            color = colors.legendText,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        Text(text = count.toString(), color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MessageBox(verdict: Mistakes.Verdict, colors: SessionColors) {
    val (title, text, box) = when (verdict) {
        Mistakes.Verdict.AS_EXPECTED ->
            Triple(R.string.summary_on_track_title, R.string.summary_on_track_text, colors.onTrackBox)
        Mistakes.Verdict.MANY ->
            Triple(R.string.summary_productive_title, R.string.summary_productive_text, colors.productiveBox)
        Mistakes.Verdict.FEW ->
            Triple(R.string.summary_great_recall_title, R.string.summary_great_recall_text, colors.greatRecallBox)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(box, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = stringResource(title), color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(text = stringResource(text), color = colors.legendText, fontSize = 14.sp, lineHeight = 21.sp)
    }
}
