package com.example.android.memoization.ui.features.memoizationscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
        Text(
            text = stringResource(R.string.words_left, session.remaining),
            style = MaterialTheme.typography.caption
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
    MemoIcon(
        contentDesc = stringResource(id = R.string.easy),
        tint = colorResource(R.color.teal_700),
        onClick = onClick
    )
}

@Composable
fun HardIcon(onClick: () -> Unit) {
    MemoIcon(
        contentDesc = stringResource(R.string.hard),
        tint = colorResource(R.color.yellow),
        onClick = onClick
    )
}

@Composable
fun WrongIcon(onClick: () -> Unit) {
    MemoIcon(
        contentDesc = stringResource(id = R.string.wrong),
        tint = colorResource(R.color.red),
        onClick = onClick
    )
}
