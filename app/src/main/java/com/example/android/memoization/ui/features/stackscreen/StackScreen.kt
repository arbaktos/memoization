package com.example.android.memoization.ui.features.stackscreen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.extensions.checkLength
import com.example.android.memoization.ui.composables.components.AddNewCardFab
import com.example.android.memoization.ui.composables.components.CustomAddFab
import com.example.android.memoization.ui.composables.components.MotionAppBar
import com.example.android.memoization.ui.composables.components.RowIcon
import com.example.android.memoization.ui.composables.components.SwipeToDismiss
import com.example.android.memoization.ui.composables.dialog.EditStackDialog
import com.example.android.memoization.ui.features.folderscreen.TDEBUG
import com.example.android.memoization.ui.navigateToMemorization
import com.example.android.memoization.ui.navigateToEditPair
import com.example.android.memoization.ui.navigateToNewPair
import com.example.android.memoization.ui.theme.indicatorColors
import com.example.android.memoization.utils.LoadingState


const val TAG = "DisplayStack"

@Composable
fun StackScreen(
    navController: NavController,
    viewModel: StackViewModel = hiltViewModel()
) {
    Log.d(TDEBUG, "StackScreen: ")
    val state by viewModel.getDataToDisplay().collectAsStateWithLifecycle(initialValue = LoadingState.Loading)
    val showDialog by viewModel.showEditStackDialog.collectAsStateWithLifecycle()

    val navigateToNewPair = remember(navController) {
        { stack: MemoStack ->
            navController.navigateToNewPair(
                stackId = stack.stackId,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage
            )
        }
    }
    val navigateToEditPair =
        remember(navController) { { wordPair: WordPair, stack: MemoStack ->
            navController.navigateToEditPair(
                wordPairId = wordPair.wordPairId,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage
            )
        } }
    val navigateToMemorization =
        remember(navController) { { stackId: Long -> navController.navigateToMemorization(stackId) } }

    DisplayStackState(
        state = state,
        showDialog = showDialog,
        navigateToNewPair = navigateToNewPair,
        navigateToEditPair = navigateToEditPair,
        navigateToMemorization = navigateToMemorization,
        updateStack = viewModel::updateStackInDb,
        deletePair = viewModel::deletePair,
        onDismissDialog = { viewModel.showEditStackDialog(false) })
}



@Composable
fun DisplayStackState(
    state: LoadingState<MemoStack>,
    showDialog: Boolean,
    navigateToNewPair: (stack: MemoStack) -> Unit,
    navigateToEditPair: (wordPair: WordPair, stack: MemoStack) -> Unit,
    navigateToMemorization: (stackId: Long) -> Unit,
    updateStack: (stack: MemoStack) -> Unit,
    deletePair: (wordPair: WordPair) -> Unit,
    onDismissDialog: () -> Unit,
) {

    when (state) {
        is LoadingState.Collected<MemoStack> -> DisplayStack(
            currentStack = state.content,
            showDialog = showDialog,
            navigateToNewPair = navigateToNewPair,
            navigateToEditPair = navigateToEditPair,
            navigateToMemorization = navigateToMemorization,
            updateStack = updateStack,
            deletePair = deletePair,
            onDismissDialog = onDismissDialog,
        )

        is LoadingState.Loading -> DisplayLoadingStack()
        is LoadingState.Error -> DisplayStackError()
    }
}

@Composable
fun DisplayStackError() {
    Text(text = "Error")
}

@Composable
fun DisplayLoadingStack() {
    Scaffold(
        topBar = {
            MotionAppBar(
                lazyScrollState = rememberLazyListState(),
                stackName = stringResource(id = R.string.loading)
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            Column {
                AddNewCardFab(
                    onAdd = { }
                )
            }
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                LinearProgressIndicator()
            }
        }
    )
}


@Composable
fun DisplayStack(
    currentStack: MemoStack,
    showDialog: Boolean,
    navigateToNewPair: (stack: MemoStack) -> Unit,
    navigateToMemorization: (stackId: Long) -> Unit,
    updateStack: (stack: MemoStack) -> Unit,
    deletePair: (wordPair: WordPair) -> Unit,
    onDismissDialog: () -> Unit,
    navigateToEditPair: (wordPair: WordPair, stack: MemoStack) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    if (showDialog) {
        EditStackDialog(
            stack = currentStack,
            onStackUpdated = updateStack,
            onDismiss = onDismissDialog
        )
    }

    Scaffold(
        topBar = {

            Text(text = "STACK ID ${currentStack.stackId}", fontSize = 56.sp)
//            MotionAppBar(
//                lazyScrollState = lazyListState,
//                stackName = currentStack.name
//            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            Column {
                CustomAddFab(
                    isVisible = true,
                    onClick = { navigateToNewPair(currentStack) }
                )
                StackFab(
                    { navigateToMemorization(currentStack.stackId) },
                    hasWordsToLearn = currentStack.hasWordsToLearn()
                )
            }
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                WordList(
                    words = currentStack.words.reversed(),
                    navigateToEditPair = { navigateToEditPair(it, currentStack) },
                    deletePair = deletePair,
                    listState = lazyListState,
                )
            }
        }
    )
}

@Composable
fun StackFab(navigateToMemorization: () -> Unit, hasWordsToLearn: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (hasWordsToLearn) {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = stringResource(R.string.learn),
//                            color = MaterialTheme.colors.surface
                    )
                },
                onClick = navigateToMemorization,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.learn_this_stack),
//                            tint = MaterialTheme.colors.surface
                    )
                },
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordList(
    deletePair: (wordPair: WordPair) -> Unit,
    navigateToEditPair: (wordPair: WordPair) -> Unit,
    listState: LazyListState,
    words: List<WordPair>,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        reverseLayout = true
    ) {
        items(words, key = { it.wordPairId }) { wordPair ->
            SwipeToDismiss(
                item = wordPair,
                dismissContent = {
                    WordPairListItem(
                        wordPair = wordPair,
                        onEditNavigate = navigateToEditPair,
                        modifier = Modifier//.animateItemPlacement()
                    )
                },
                onDismiss = { deletePair(wordPair) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WordPairListItem(
    wordPair: WordPair,
    onEditNavigate: (wordPair: WordPair) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(top = 4.dp, bottom = 4.dp)
            .clickable {
                onEditNavigate(wordPair)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {

            RowIcon(
                iconSource = Icons.Filled.Circle,
                contentDesc = stringResource(R.string.word_indicator),
                tint = indicatorColors(wordPair.levelOfKnowledge),
                modifier = Modifier.padding(start = 10.dp)
            )
            ListItem(
                headlineContent = {
                    Text(
                        wordPair.word1.checkLength(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    wordPair.word2?.checkLength()
                        ?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                }
            )
        }
    }
}

suspend fun SnackbarHostState.showOnDeleteSnackBar(
    wordPair: WordPair,
    context: Context
): SnackbarResult {
    val result = this.showSnackbar(
        message = context.getString(R.string.confirm_deletion) + "${wordPair.word1}/${wordPair.word2}?",
        actionLabel = context.getString(R.string.undo),
        duration = SnackbarDuration.Long
    )
    return result
}
