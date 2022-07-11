package com.example.android.memoization.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.extensions.checkLength
import com.example.android.memoization.model.WordPair
import com.example.android.memoization.ui.composables.components.MotionAppBar
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.NavScreens
import com.example.android.memoization.ui.composables.components.SwipeToDismiss

@Composable
fun StackScreen(
    navContoller: NavController,
    viewModel: StackViewModel
) {
    val appState by viewModel.publicStackState.collectAsState()
    val currentStack = appState.stack
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()

    BackHandler {
        navContoller.navigate(NavScreens.Folders.route)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(currentStack?.name ?: "") }) },
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                currentStack?.let { stack ->
                    if (stack.words.any { it.toLearn }) {
                        ExtendedFloatingActionButton(
                            text = {
                                Text(
                                    text = stringResource(R.string.learn),
                                    color = MaterialTheme.colors.surface
                                )
                            },
                            onClick = { navContoller.navigate(NavScreens.Memorization.route) },
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

                Fab(
                    icon = Icons.Filled.Add,
                    contentDesc = stringResource(R.string.add_new_wordpair),
                    onclick = { navContoller.navigate(NavScreens.NewPair.route) }
                )
            }
        },
        scaffoldState = scaffoldState,
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)){
                WordList(
                    currentStack?.words ?: emptyList(),
                    viewModel = viewModel,
                    onEditNavigate = { navContoller.navigate("new_pair?editMode=true") },
                    onDelete = { navContoller.navigate(NavScreens.Stack.route) },
                    scaffoldState = scaffoldState,
                )
            }

        }
    )
    }

@Composable
fun WordList(
    wordList: List<WordPair>,
    viewModel: StackViewModel,
    onEditNavigate: () -> Unit,
    onDelete: () -> Unit,
    scaffoldState: ScaffoldState,

) {
    LazyColumn(
        reverseLayout = true,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(wordList) { wordPair ->
            SwipeToDismiss(
                item = wordPair,
                dismissContent = {
                    WordPairListItem(
                        wordPair,
                        viewModel = viewModel,
                        onEditNavigate = onEditNavigate,
                    )
                },
                onDismiss = {
                    val snackBarResult =
                        scaffoldState.snackbarHostState.showOnDeleteSnackBar(wordPair)
                    when (snackBarResult) {
                        SnackbarResult.ActionPerformed -> {
                            viewModel.cancelDelayDeletionWork(wordPair)
                            wordPair.isVisible = true
                            viewModel.updateStackInDb()
                            onDelete()
                        }
                        SnackbarResult.Dismissed -> viewModel.delayDeletionWordPair(wordPair)
                    }
                }
            )
        }
    }
}



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WordPairListItem(
    wordPair: WordPair,
    viewModel: StackViewModel,
    onEditNavigate: () -> Unit,
) {
    val onEdit = {
        viewModel.updateCurrentWordPair(wordPair)
        viewModel.setWord(1, wordPair.word1)
        viewModel.setWord(2, wordPair.word2 ?: "")
        onEditNavigate()
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(start = 5.dp, end = 10.dp, top = 5.dp)
            .clickable {
                onEdit()
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)) {

            RowIcon(
                iconSource = Icons.Filled.Circle,
                contentDesc = stringResource(R.string.word_indicator),
                modifier = Modifier.padding(start = 10.dp)
            )
            ListItem(
                text = { Text(wordPair.word1.checkLength(), maxLines = 2, overflow = TextOverflow.Ellipsis) },
                secondaryText = { wordPair.word2?.checkLength()?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) } }
            )
        }
    }
}

suspend fun SnackbarHostState.showOnDeleteSnackBar(wordPair: WordPair): SnackbarResult {
    val result = this.showSnackbar(
        message = "Do you want to delete ${wordPair.word1}/${wordPair.word2}?",
        actionLabel = "Undo",
        duration = SnackbarDuration.Long
    )
    return result
}


//
//@Composable
//fun ImageListItem(index: Int) {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//
//        Image(
//            painter = rememberImagePainter(
//                data = "https://developer.android.com/images/brand/Android_Robot.png"
//            ),
//            contentDescription = "Android Logo",
//            modifier = Modifier.size(50.dp)
//        )
//        Spacer(Modifier.width(10.dp))
//        Text("Item #$index", style = MaterialTheme.typography.subtitle1)
//    }
//}
//




