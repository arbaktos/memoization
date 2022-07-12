package com.example.android.memoization.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import androidx.navigation.NavController
import com.example.android.memoization.extensions.checkLength
import com.example.android.memoization.model.ListItem
import com.example.android.memoization.model.WordPair
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.NavScreens
import kotlinx.coroutines.launch

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
        topBar = { com.example.android.memoization.ui.composables.screens.AppBar(name = currentStack!!.name, {}) },
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                currentStack?.let {
                    if (!it.words.isEmpty()) {
                        ExtendedFloatingActionButton(
                            text = {
                                Text(
                                    text = "Learn",
                                    color = MaterialTheme.colors.surface
                                )
                            },
                            onClick = { navContoller.navigate(NavScreens.Memorization.route) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Learn this stack",
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
                    contentDesc = "Add new word pair",
                    onclick = { navContoller.navigate(NavScreens.NewPair.route) }
                )
            }
        },
        scaffoldState = scaffoldState
    ) {
        WordList(
            currentStack!!.words,
            viewModel = viewModel,
            onEditNavigate = { navContoller.navigate("new_pair?editMode=true") },
            onDelete = { navContoller.navigate(NavScreens.Stack.route) },
            scaffoldState = scaffoldState
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WordList(
    wordList: List<WordPair>,
    viewModel: StackViewModel,
    onEditNavigate: () -> Unit,
    onDelete: () -> Unit,
    scaffoldState: ScaffoldState
) {
    LazyColumn(
        reverseLayout = true
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
fun SwipeToDismiss(
    item: ListItem,
    dismissContent: @Composable () -> Unit,
    onDismiss: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                item.isVisible = false
                scope.launch {
                    onDismiss()
                }
            }
            true
        })

    SwipeToDismiss(
        directions = setOf(DismissDirection.EndToStart),
        state = dismissState,
        dismissThresholds = { direction ->
            FractionalThreshold(0.5f)
        },
        background = {
            Box(
                Modifier
                    .fillMaxSize()
                    //.fillMaxHeight()
                    .background(Color.Transparent)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Localized description",
                    tint = Color.Red
                )
            }
        },
        dismissContent = {
            Card(
                elevation = animateDpAsState(
                    if (dismissState.dismissDirection != null) 4.dp else 0.dp
                ).value
            ) {
                if (item.isVisible) {
                    dismissContent()
                }

            }
        })
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
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 10.dp, top = 5.dp)
            .clickable {
                onEdit()
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            RowIcon(
                iconSource = Icons.Filled.Circle,
                contentDesc = "Indicator how well do you know the word",
                modifier = Modifier.padding(start = 10.dp)
            )
            ListItem(
                text = { Text(wordPair.word1.checkLength()) },
                secondaryText = { wordPair.word2?.checkLength()?.let { Text(it) } }
            )
//
//            Row {
//                RowIcon(
//                    iconSource = Icons.Filled.Edit,
//                    contentDesc = "Edit wordpair",
//                    onClick = onEdit,
//                    modifier = Modifier.padding(end = 8.dp)
//                )
//            }
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




