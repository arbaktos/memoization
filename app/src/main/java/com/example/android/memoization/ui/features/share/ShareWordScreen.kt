package com.example.android.memoization.ui.features.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.composables.dialog.AddStackDialog
import com.example.android.memoization.ui.composables.labels.SimpleLabel
import com.example.android.memoization.ui.features.folderscreen.InvitationToCreateStack
import com.example.android.memoization.ui.features.stackscreen.DisplayStackError
import com.example.android.memoization.utils.LoadingState

/**
 * Where a word shared in from another app arrives: it is shown with the library under it, and
 * the stack the learner picks opens a new pair with the word already in place.
 */
@Composable
fun ShareWordScreen(
    navController: NavController,
    viewModel: ShareWordViewModel = hiltViewModel(),
) {
    val state by viewModel.getDataToDisplay().collectAsState(initial = LoadingState.Loading)
    var toShowDialog by rememberSaveable { mutableStateOf(false) }

    ShareWordBody(
        word = viewModel.word,
        state = state,
        onStackChosen = { stack -> viewModel.onStackChosen(navController, stack) },
        onCreateStack = { toShowDialog = true },
    )

    if (toShowDialog) {
        AddStackDialog(
            onDismiss = { toShowDialog = false },
            onStackAdded = { stack -> viewModel.onStackCreated(navController, stack) }
        )
    }
}

@Composable
fun ShareWordBody(
    word: String,
    state: LoadingState<List<MemoStack>>,
    onStackChosen: (MemoStack) -> Unit,
    onCreateStack: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SharedWordCard(word)
        SimpleLabel(
            stringId = R.string.share_pick_stack,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when (state) {
            is LoadingState.Loading -> LinearProgressIndicator()
            is LoadingState.Error -> DisplayStackError()
            is LoadingState.Collected ->
                if (state.content.isEmpty()) InvitationToCreateStack(onCreateStack)
                else StackChoiceList(stacks = state.content, onStackChosen = onStackChosen)
        }
    }
}

/** The word as it was shared, so the learner sees what is about to be filed before choosing. */
@Composable
private fun SharedWordCard(word: String) {
    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = word,
            fontSize = 24.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun StackChoiceList(stacks: List<MemoStack>, onStackChosen: (MemoStack) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(stacks, key = { stack -> stack.stackId }) { stack ->
            StackChoice(stack = stack, onClick = { onStackChosen(stack) })
        }
    }
}

@Composable
private fun StackChoice(stack: MemoStack, onClick: () -> Unit) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stack.name,
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(
                    R.plurals.stack_word_count,
                    stack.words.size,
                    stack.words.size
                ),
                style = MaterialTheme.typography.caption
            )
        }
    }
}
