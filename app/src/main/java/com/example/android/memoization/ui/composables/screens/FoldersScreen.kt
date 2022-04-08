package com.example.android.memoization.ui.composables.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.model.Folder
import com.example.android.memoization.model.Stack
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.NavScreens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//TODO make function shorter and more readable
const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    folderViewModel: FolderViewModel,
    stackViewModel: StackViewModel,
) {
    val scaffoldState = rememberScaffoldState()
    MemoizationTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { AppBar(name = "Memoization") }
        ) {
            BodyContent(
                viewModel = folderViewModel,
                navController = navController,
                stackViewModel = stackViewModel,
                scaffoldState = scaffoldState,
                update = { navController.navigate(NavScreens.Folders.route) }
            )
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
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

//    val listState = rememberLazyListState()

    LazyColumn( state = listState,
        modifier = Modifier.padding(8.dp)) {
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
                    val snackBarResult =
                        scaffoldState.snackbarHostState.showOnStackDeleteSnackBar(stack)
                    when (snackBarResult) {
                        SnackbarResult.ActionPerformed -> {
                            //Undo action performed
                            viewModel.getFoldersWithStackFromDb()
                            update()
                        }
                        SnackbarResult.Dismissed -> {
                            //nothing is done so proceed to deletion
                            viewModel.deleteStackWithDelay(stack)
                            update()
                        }
                    }
                }
            )


        }
        item {
            AddStack(
                folder = appState.currentFolder,
                viewModel = viewModel,
                listState = listState,
                position = currentFolder.stacks.size + 1,
                modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)
            )
        }
    }
}

suspend fun SnackbarHostState.showOnStackDeleteSnackBar(stack: Stack): SnackbarResult {
    return showSnackbar(
        message = "Deletion of ${stack.name} with all the words",
        actionLabel = "Undo",
        duration = SnackbarDuration.Long
    )
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
            navController.navigate(NavScreens.Memorization.route)
        },
        onClickRow = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.Stack.route)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
@Composable
fun AddStack(
    folder: Folder,
    viewModel: FolderViewModel,
    listState: LazyListState,
    position: Int,
    modifier: Modifier
) {

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") }
    var isChosen = text.isNotBlank()
    val scope = rememberCoroutineScope()


    val onClick: () -> Unit = {
        onTextChange("")
        viewModel.addStackToFolder(folder = folder, stack = Stack(text))
        keyboardController?.hide()
//        focusManager.clearFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        AddStackTextField(
            text = text,
            onTextChange = {
//                onTextChange(it)
//                isChosen = true
            },
            label = stringResource(R.string.add_stack),
            onFinish = {
//                onClick()
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        scope.launch {
                            delay(300)
                            listState.animateScrollToItem(position)

                            Log.d(TDEBUG, "onFocusEvent focused")
                        }
                    } else {
                        Log.d(TDEBUG, "onFocusEvent not focused")
                    }
                }
        )
//        if (isChosen) {
//            SubmitIcon(
//                inputName = text,
//                onFinish = onClick
//            )
//        }
    }
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
//            .padding(top = 4.dp, end = 15.dp, bottom = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_stack),
            contentDescription = "stack symbol",
            modifier = Modifier
                .size(50.dp)
                .padding(top = 15.dp)
        )
        H5TextBox(
            text = stack.name,
            modifier = Modifier
                .weight(1f)
                .clickable { onClickRow() })
        if (stack.hasWords) {
            Row {
                H6TextBox(
                    text = stack.words.size.toString(),
                    modifier = Modifier
                )
                RowIcon(
                    iconSource = Icons.Filled.PlayCircle,
                    contentDesc = "Start stack memorization",
                    onClick = { onClickPlay() }
                )
            }

        }
    }
}


@Composable
fun AppBar(name: String) {
    TopAppBar(
        title = { Text(name) },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Settings, null)
            }
        })
}


