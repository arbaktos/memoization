package com.example.android.memoization.ui.features.memoizationscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.Rating
import com.example.android.memoization.domain.session.MemorizationSession
import com.example.android.memoization.ui.composables.components.FlipCard
import com.example.android.memoization.ui.composables.components.MemoIcon
import com.example.android.memoization.ui.composables.components.StackCompleteDialog
import com.example.android.memoization.ui.navigateToFolderScreen

@Composable
fun MemorizationScreen(
    navController: NavController,
) {
    val viewModel: MemoizationViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    MemorizationBody(
        state = state,
        onRate = viewModel::onRate,
        onComplete = { navController.navigateToFolderScreen() }
    )
}

@Composable
fun MemorizationBody(
    state: MemorizationSession.State?,
    onRate: (Rating) -> Unit,
    onComplete: () -> Unit,
) {
    // null = still loading the stack; nothing to draw yet.
    val session = state ?: return

    if (session.isFinished) {
        StackCompleteDialog(onClick = onComplete)
        return
    }
    val wordPair = session.current ?: return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        // Session progress: pairs closed out of the pairs the session started with. A pair
        // rated Again stays in the queue, so the bar holds still until it is finally recalled.
        val progressLabel = stringResource(R.string.session_progress, session.total - session.remaining, session.total)
        LinearProgressIndicator(
            progress = session.progress,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(horizontal = 16.dp)
                .semantics { contentDescription = progressLabel }
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Keyed on the serial too: after an Again the same pair can come straight back
            // (e.g. when it is the only one left) and must start face-up again.
            key(wordPair.wordPairId, session.serial) {
                FlipCard(wordPair)
            }
        }
        Spacer(Modifier.height(100.dp))
        Row(modifier = Modifier.weight(0.3f)) {
            EasyIcon { onRate(Rating.Good) }
            HardIcon { onRate(Rating.Hard) }
            WrongIcon { onRate(Rating.Again) }
        }
    }
}

@Composable
fun EasyIcon(onClick: () -> Unit) {
    RatingButton(label = stringResource(R.string.easy), tint = colorResource(R.color.teal_700), onClick = onClick)
}

@Composable
fun HardIcon(onClick: () -> Unit) {
    RatingButton(label = stringResource(R.string.hard), tint = colorResource(R.color.yellow), onClick = onClick)
}

@Composable
fun WrongIcon(onClick: () -> Unit) {
    RatingButton(label = stringResource(R.string.wrong), tint = colorResource(R.color.red), onClick = onClick)
}

/** A rating circle with its name underneath, so the colours don't have to be learnt. */
@Composable
private fun RatingButton(label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MemoIcon(contentDesc = label, tint = tint, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.caption)
    }
}
