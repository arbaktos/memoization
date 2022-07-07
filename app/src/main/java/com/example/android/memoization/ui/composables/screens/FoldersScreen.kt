package com.example.android.memoization.ui.composables.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.model.Stack
import com.example.android.memoization.notifications.NotificationReceiver
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.AddStackAlerDialog
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.NavScreens
import kotlinx.coroutines.launch

const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    folderViewModel: FolderViewModel,
    stackViewModel: StackViewModel,
) {
    val scaffoldState = rememberScaffoldState()
    var showAddStackDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    MemoizationTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            floatingActionButton = {
                Fab(
                    icon = Icons.Filled.Add,
                    contentDesc = stringResource(R.string.add_new_stack),
                    onclick = {
                        showAddStackDialog = true

                    })
            },
            topBar = { AppBar(name = "Memoization") }
        ) {
            BodyContent(
                viewModel = folderViewModel,
                navController = navController,
                stackViewModel = stackViewModel,
                scaffoldState = scaffoldState,
                update = { navController.navigate(NavScreens.Folders.route) },
            )
            if (showAddStackDialog)
                AddStackAlerDialog(
                    viewModel = folderViewModel,
                    onClick = { showAddStackDialog = false })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Composable
fun BodyContent(
    viewModel: FolderViewModel,
    navController: NavController,
    stackViewModel: StackViewModel,
    scaffoldState: ScaffoldState,
    update: () -> Unit,
) {
    val appState by viewModel.publicAppState.collectAsState()
    val currentFolder = appState.folders[0]
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(currentFolder.stacks) { stack ->
            SwipeToDismiss(
                item = stack,
                dismissContent = {
                    ShowStack(
                        stack = stack,
                        viewModel = viewModel,
                        navController = navController,
                        stackViewModel = stackViewModel
                    )
                },
                onDismiss = {
                    scope.launch {
                        val snackBarResult =
                            scaffoldState.snackbarHostState.showOnStackDeleteSnackBar(stack)
                        viewModel.deleteStackWithDelay(stack)

                        when (snackBarResult) {
                            SnackbarResult.ActionPerformed -> {
                                //Undo action performed
                                viewModel.cancelStackDeletion(stack)
                                viewModel.getFoldersWithStackFromDb()
                                update()
                            }
                            SnackbarResult.Dismissed -> { }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ShowStack(
    stack: Stack,
    viewModel: FolderViewModel,
    navController: NavController,
    stackViewModel: StackViewModel
) {
    StackRow(
        stack = stack,
        onClickPlay = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.Memorization.route)
        },
        onClickRow = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.Stack.route)
        }
    )
}

@Composable
fun StackRow(
    stack: Stack,
    onClickRow: () -> Unit,
    onClickPlay: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val wordsToLearn = stack.words.filter { it.toLearn }
        if (wordsToLearn.isNotEmpty()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_noun_play_1423160),
                contentDescription = "Start stack memorization",
                modifier = Modifier
                    .clickable { onClickPlay() }
                    .size(50.dp)
                    .padding(top = 15.dp)
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_stack),
                contentDescription = "stack symbol",
                modifier = Modifier
                    .size(50.dp)
                    .padding(top = 15.dp)
            )
        }

        H5TextBox(
            text = stack.name,
            modifier = Modifier
                .weight(1f)
                .clickable { onClickRow() })

        if (wordsToLearn.isNotEmpty()) {
            H6TextBox(text = wordsToLearn.size.toString())
        }
    }
}


@Composable
fun AppBar(
    name: String
) {
    TopAppBar(
        title = { Text(name) },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Settings, null)
            }
        }
    )
}

suspend fun SnackbarHostState.showOnStackDeleteSnackBar(stack: Stack): SnackbarResult {
    return showSnackbar(
        message = "Deletion of ${stack.name} with all the words",
        actionLabel = "Undo",
        duration = SnackbarDuration.Short
    )
}


