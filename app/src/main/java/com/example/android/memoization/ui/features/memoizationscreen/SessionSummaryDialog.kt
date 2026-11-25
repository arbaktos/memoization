package com.example.android.memoization.ui.features.memoizationscreen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.android.memoization.R
import com.example.android.memoization.data.model.WordStatus
import com.example.android.memoization.domain.session.Mistakes
import com.example.android.memoization.domain.session.SessionSummary
import com.example.android.memoization.ui.theme.indicatorColors

/**
 * The end of a session, however it ended: how many sides were practised, where they stand now,
 * and the mistakes against what the scheduler expected - worded so that mistakes read as part
 * of learning, never as a failure.
 */
@Composable
fun SessionSummaryDialog(
    summary: SessionSummary,
    waiting: Int,
    colors: SessionColors,
    onDone: () -> Unit,
) {
    Dialog(onDismissRequest = onDone) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, RoundedCornerShape(24.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (waiting == 0) {
                    stringResource(R.string.stack_complete)
                } else {
                    pluralStringResource(R.plurals.session_complete_waiting, waiting, waiting)
                },
                color = colors.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            val practised = pluralStringResource(R.plurals.summary_practised, summary.practised, summary.practised)
            val ofThemNew = if (summary.new == 0) "" else pluralStringResource(R.plurals.summary_new, summary.new, summary.new)
            Text(text = practised + ofThemNew, color = colors.text, fontSize = 16.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.levels.forEach { (level, count) -> LevelRow(level, count, colors) }
            }
            summary.mistakes?.let { mistakes ->
                Text(
                    text = stringResource(mistakes.verdict.text, mistakes.count, mistakes.reviewed),
                    color = colors.secondaryText,
                    fontSize = 14.sp,
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.goodFill)
                        .clickable(role = Role.Button, onClick = onDone)
                        .padding(horizontal = 28.dp),
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
}

@Composable
private fun LevelRow(level: WordStatus, count: Int, colors: SessionColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .background(indicatorColors(level), CircleShape)
        )
        Text(
            text = stringResource(level.label),
            color = colors.text,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        )
        Text(text = count.toString(), color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** A level as the learner reads it: a step on the way from just started to learned. */
@get:StringRes
private val WordStatus.label: Int
    get() = when (this) {
        WordStatus.Level1 -> R.string.level_just_started
        WordStatus.Level2 -> R.string.level_sinking_in
        WordStatus.Level3 -> R.string.level_familiar
        WordStatus.Level4 -> R.string.level_nearly_learned
        WordStatus.Learned -> R.string.level_learned
    }

@get:StringRes
private val Mistakes.Verdict.text: Int
    get() = when (this) {
        Mistakes.Verdict.FEW -> R.string.summary_mistakes_few
        Mistakes.Verdict.AS_EXPECTED -> R.string.summary_mistakes_as_expected
        Mistakes.Verdict.MANY -> R.string.summary_mistakes_many
    }
