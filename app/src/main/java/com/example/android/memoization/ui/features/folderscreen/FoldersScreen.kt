package com.example.android.memoization.ui.features.folderscreen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.*
import com.example.android.memoization.ui.features.stackscreen.DisplayStackError
import com.example.android.memoization.ui.theme.PlayColors
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.ui.features.settings.MenuDrawer

const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel(),
    preferenceStorage: DataStore<Preferences>
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<LoadingState<List<MemoStack>>>(LoadingState.Loading) }
    val toShowDialog = viewModel.showAddStackDialog.observeAsState(false)

    LaunchedEffect(key1 = state, block = {
        state = viewModel.getDataToDisplay().stateIn(this).value
    })

    BackHandler {
        viewModel.onBackPressed(navController)
    }

    MemoizationTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            floatingActionButton = {
                CustomFab(
                    onClick = {
                        viewModel.showAddSTackDialog(true)
                    })
            },
            drawerContent = { MenuDrawer(scaffoldState, preferenceStorage) },
            topBar = {
                AppBar(name = stringResource(id = R.string.app_name)) {
                    scope.launch { scaffoldState.drawerState.open() }
                }
            }
        ) { _ ->

            FolderScreenBodyContent(
                viewModel = viewModel,
                navController = navController,
                scaffoldState = scaffoldState,
                state = state
            )
            if (toShowDialog.value) {
                AddStackAlertDialog(
                    viewModel = viewModel
                ) { viewModel.showAddSTackDialog(false) }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun FolderScreenBodyContent(
    viewModel: FolderViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState,
    state: LoadingState<List<MemoStack>>,
) {

    when (state) {
        is LoadingState.Loading -> LoadingStackList()
        is LoadingState.Collected -> FolderColumn(
            listState = rememberLazyListState(),
            state = state,
            viewModel = viewModel,
            navController = navController,
            scaffoldState = scaffoldState,
        )

        is LoadingState.Error -> DisplayStackError()
    }
}

@Composable
private fun LoadingStackList() {
    LinearProgressIndicator()
}

@Composable
fun FolderColumn(
    listState: LazyListState,
    state: LoadingState.Collected<List<MemoStack>>,
    viewModel: FolderViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState
) {
    if (state.content.isEmpty()) InvitationToCreateStack { viewModel.showAddSTackDialog(true) }
    else StackList(listState, state, viewModel, navController, scaffoldState)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StackList(
    listState: LazyListState,
    state: LoadingState.Collected<List<MemoStack>>,
    viewModel: FolderViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(
            state.content, key = { stack -> stack.stackId },
        ) { stack ->
            SwipeToDismiss(
                item = stack,
                dismissContent = {
                    ShowStack(
                        stack = stack,
                        onAddNewWord = { viewModel.onAddNewWord(navController, stack.stackId) },
                        onNavigateToStack = {
                            viewModel.onNavigateTosStack(
                                navController,
                                stack.stackId
                            )
                        },
                        onPlayWords = { viewModel.onPlayWords(navController, stack.stackId) },
                        modifier = Modifier.animateItemPlacement()
                    )
                },
                onDismiss = {
                    scope.launch {
                        val updatedStack = stack.copy(isVisible = false)
                        viewModel.updateStack(updatedStack)
                        val snackBarResult =
                            scaffoldState.snackbarHostState.showOnStackDeleteSnackBar(stack)

                        when (snackBarResult) {
                            SnackbarResult.ActionPerformed -> viewModel.cancelStackDeletion(stack)
                            SnackbarResult.Dismissed -> viewModel.deleteStackWithDelay(stack)
                        }
                    }
                }
            )

        }
        item { Spacer(modifier = Modifier.padding(40.dp)) }
    }
}

@Preview
@Composable
fun InvitationToCreateStack(onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    fontSize = 20.sp,
                    text = stringResource(id = R.string.create_first_stack),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Image(
                    painterResource(id = R.drawable.noun_create_1202533),
                    stringResource(id = R.string.create_first_stack)
                )
            }
        }
        Spacer(modifier = Modifier.padding(40.dp))
    }

}

@Composable
fun ShowStack(
    stack: MemoStack,
    onPlayWords: () -> Unit,
    onNavigateToStack: () -> Unit,
    onAddNewWord: () -> Unit,
    modifier: Modifier
) {
    StackListItem(
        stack = stack,
        modifier = modifier,
        onPlay = onPlayWords,
        onAdd = onAddNewWord,
        onPin = {
//            viewModel.onPin()//TODO
            animateToTop()
        },
        onClickRow = onNavigateToStack
    )
}

fun animateToTop() {
    TODO("Not yet implemented")
}


fun getPlayIconColor(unRepeatedPercent: Float): Color {
    return when (unRepeatedPercent.toInt()) {
        in 1..20 -> PlayColors.itsok.getColor()
        in 21..40 -> PlayColors.itsstillok.getColor()
        in 41..60 -> PlayColors.shoulddosomework.getColor()
        in 61..80 -> PlayColors.actionneeded.getColor()
        else -> PlayColors.timetolearn.getColor()
    }
}


@Composable
fun AppBar(
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            name,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = colors.primaryVariant,
            modifier = Modifier.padding(12.dp)
        )
        Icon(Icons.Filled.Menu,
            stringResource(R.string.app_menu),
            modifier = Modifier
                .padding(end = 16.dp, bottom = 14.dp)
                .clickable { onClick() })
    }

}


suspend fun SnackbarHostState.showOnStackDeleteSnackBar(stack: MemoStack): SnackbarResult {
    return showSnackbar(
        message = "Deletion of ${stack.name} with all the words",
        actionLabel = "Undo",
        duration = SnackbarDuration.Short
    )
}


