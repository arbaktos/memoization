package com.example.android.memoization.ui.features.stackscreen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.android.memoization.ui.composables.components.RowIcon
import com.example.android.memoization.ui.composables.components.SwipeToReveal
import com.example.android.memoization.ui.composables.dialog.EditStackDialog
import com.example.android.memoization.ui.features.folderscreen.TDEBUG
import com.example.android.memoization.ui.icons.ClickableVectorIcon
import com.example.android.memoization.ui.navigateToMemorization
import com.example.android.memoization.ui.navigateToEditPair
import com.example.android.memoization.ui.navigateToNewPair
import com.example.android.memoization.ui.theme.indicatorColors
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.launch


const val TAG = "DisplayStack"

@Composable
fun StackScreen(
    navController: NavController,
    viewModel: StackViewModel = hiltViewModel()
) {
    Log.d(TDEBUG, "StackScreen: ")
    val state by viewModel.getDataToDisplay().collectAsStateWithLifecycle(initialValue = LoadingState.Loading)
    val showDialog by viewModel.showEditStackDialog.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Deleting is immediate; the snackbar puts the row back if it was a mistake.
    val deletePair: (WordPair) -> Unit = { wordPair ->
        viewModel.deletePair(wordPair)
        scope.launch {
            val result = snackbarHostState.showOnDeleteSnackBar(wordPair, context)
            Log.d(TDEBUG, "snackbar result = $result")
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        }
    }

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
        deletePair = deletePair,
        snackbarHostState = snackbarHostState,
        onEditStack = { viewModel.showEditStackDialog(true) },
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
    onEditStack: () -> Unit,
    onDismissDialog: () -> Unit,
    snackbarHostState: SnackbarHostState,
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
            onEditStack = onEditStack,
            onDismissDialog = onDismissDialog,
            snackbarHostState = snackbarHostState,
        )

        is LoadingState.Loading -> DisplayLoadingStack()
        is LoadingState.Error -> DisplayStackError()
    }
}

@Composable
fun DisplayStackError() {
    Text(text = "Error")
}

/**
 * MotionAppBar leaves its children unconstrained, so the title never gets laid out -
 * a plain bar until that motion scene is fixed.
 */
@Composable
fun StackAppBar(
    stackName: String,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stackName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (onEdit != null) {
            ClickableVectorIcon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = R.string.edit_stack_desc,
                onClick = onEdit,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun DisplayLoadingStack() {
    Scaffold(
        topBar = { StackAppBar(stackName = stringResource(id = R.string.loading)) },
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
    snackbarHostState: SnackbarHostState,
    navigateToNewPair: (stack: MemoStack) -> Unit,
    navigateToMemorization: (stackId: Long) -> Unit,
    updateStack: (stack: MemoStack) -> Unit,
    deletePair: (wordPair: WordPair) -> Unit,
    onEditStack: () -> Unit,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StackAppBar(
                stackName = currentStack.name,
                onEdit = onEditStack
            )
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
                    hasWordsToLearn = currentStack.hasDueWords()
                )
            }
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                WordList(
                    // Newest first: ids grow with insertion, so this is "last added on top".
                    words = currentStack.words.sortedByDescending { it.wordPairId },
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
    ) {
        items(words, key = { it.wordPairId }) { wordPair ->
            SwipeToReveal(
                onDelete = { deletePair(wordPair) },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                WordPairListItem(
                    wordPair = wordPair,
                    onEditNavigate = navigateToEditPair,
                    modifier = Modifier//.animateItemPlacement()
                )
            }
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
            .fillMaxWidth()
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
                tint = indicatorColors(wordPair.level),
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
