package com.example.android.memoization.ui.features.stackscreen

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.BaseWordPair
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.extensions.checkLength
import com.example.android.memoization.ui.composables.components.AddNewCardFab
import com.example.android.memoization.ui.composables.components.AddStackAlertDialog
import com.example.android.memoization.ui.composables.components.MotionAppBar
import com.example.android.memoization.ui.composables.components.RowIcon
import com.example.android.memoization.ui.composables.components.SwipeToDismiss
import com.example.android.memoization.ui.theme.indicatorColors
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.NewPairNavArgs
import kotlinx.coroutines.flow.stateIn

const val TAG = "DisplayStack"

@Composable
fun StackScreen(
    navController: NavController,
    stackId: Long,
    viewModel: StackViewModel = hiltViewModel()
) {
    viewModel.setArgs(stackId)
    var state by remember { mutableStateOf<LoadingState<MemoStack>>(LoadingState.Loading) }

    LaunchedEffect(key1 = state, block = {
        state = viewModel.getDataToDisplay().stateIn(this).value
    })

    BackHandler {
        viewModel.onBackPressed(navController)
    }

    DisplayStackState(state = state, navController = navController)
}

@Composable
fun DisplayStackState(
    state: LoadingState<MemoStack>,
    navController: NavController,
    viewmodel: StackViewModel = hiltViewModel()
) {
    val showDialog = viewmodel.showEditStackDialog.collectAsState().value
    when (state) {
        is LoadingState.Collected<MemoStack> -> DisplayStack(
            loadingState = state,
            navController = navController,
            showDialog = showDialog
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
        isFloatingActionButtonDocked = false,
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
    loadingState: LoadingState.Collected<MemoStack>,
    navController: NavController,
    viewModel: StackViewModel = hiltViewModel(),
    showDialog: Boolean = false
) {
    val scaffoldState = rememberScaffoldState()
    val lazyListState = rememberLazyListState()

    val currentStack = loadingState.content

    if (showDialog) {
        AddStackAlertDialog(
            viewModel = hiltViewModel(),
            isEditMode = true,
            stack = loadingState.content
        ) {
            viewModel.showEditStackDialog(false)
        }
    }

    Scaffold(
        topBar = {
            MotionAppBar(
                lazyScrollState = lazyListState,
                stackName = loadingState.content.name
            )
        },
        isFloatingActionButtonDocked = false,
        floatingActionButton = {
            Column {
                AddNewCardFab(
                    onAdd = {
                        navController
                            .navigate(
                                StackScreenFragmentDirections
                                    .actionStackScreenFragmentToNewPairFragment(
                                        NewPairNavArgs.NewWordPair(stackId = currentStack.stackId)
                                    )
                            )
                    }
                )
                StackFab(navController = navController, currentStack = currentStack)
            }
        },
        scaffoldState = scaffoldState,
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                WordList(
                    wordList = loadingState.content.words,
                    viewModel = viewModel,
                    navController = navController,
                    listState = lazyListState,
                )
            }
        }
    )
}

@Composable
fun StackFab(navController: NavController, currentStack: MemoStack?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        currentStack?.let { stack ->
            if (stack.hasWordsToLearn()) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = stringResource(R.string.learn),
                            color = MaterialTheme.colors.surface
                        )
                    },
                    onClick = {
                        navController.navigate(
                            StackScreenFragmentDirections.toMemorizationFragment(
                                stack.stackId
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.learn_this_stack),
                            tint = MaterialTheme.colors.surface
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordList(
    wordList: List<BaseWordPair>,
    viewModel: StackViewModel,
    navController: NavController,
    listState: LazyListState,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        state = listState
    ) {
        items(wordList.reversed(), key = { it.wordPairId }) { wordPair ->
            wordPair as WordPair
            SwipeToDismiss(
                item = wordPair,
                dismissContent = {
                    WordPairListItem(
                        wordPair = wordPair,
                        onEditNavigate = {
                            navController.navigate(
                                StackScreenFragmentDirections.actionStackScreenFragmentToNewPairFragment(
                                    NewPairNavArgs.EditPair(wordPair.wordPairId)
                                )
                            )
                        },
                        modifier = Modifier.animateItemPlacement()
                    )
                },
                onDismiss = {
                    viewModel.deleteWordPairFromDb(wordPair)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WordPairListItem(
    wordPair: WordPair,
    onEditNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .padding(top = 4.dp, bottom = 4.dp)
            .clickable {
                onEditNavigate()
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
                text = {
                    Text(
                        wordPair.word1.checkLength(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryText = {
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