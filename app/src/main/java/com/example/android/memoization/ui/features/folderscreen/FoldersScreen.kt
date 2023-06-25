package com.example.android.memoization.ui.features.folderscreen

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
import com.example.android.memoization.domain.model.MemoStack
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.*
import com.example.android.memoization.ui.features.stackscreen.DisplayStackError
import com.example.android.memoization.ui.theme.PlayColors
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.NewPairNavArgs
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    var showAddStackDialog by remember { mutableStateOf(false) } //TODO move to viewmodel
    val scope = rememberCoroutineScope()


    var state by remember { mutableStateOf<LoadingState<List<MemoStack>>>(LoadingState.Loading) }
    LaunchedEffect(key1 = state, block = {
        state = viewModel.stacksWithWords().stateIn(this).value
    })

    val stackExists = state is LoadingState.Collected && (state as LoadingState.Collected<List<MemoStack>>).content.isNotEmpty()

    MemoizationTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            floatingActionButton = {
                if (stackExists) {
                    CustomFab(
                        onClick = {
                            showAddStackDialog = true
                        })
                }
            },
            drawerContent = { MenuDrawer(scaffoldState) },
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
                update = { },
                state = state
            )
            if (showAddStackDialog)
                AddStackAlertDialog(
                    viewModel = viewModel
                ) { showAddStackDialog = false }
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
    update: () -> Unit,
) {
    val listState = rememberLazyListState()

    when (state) {
        is LoadingState.Loading -> LoadingStackList()
        is LoadingState.Collected -> FolderColumn(
            listState = listState,
            state = state as LoadingState.Collected<List<MemoStack>>,
            viewModel = viewModel,
            navController = navController,
            scaffoldState = scaffoldState,
            update = update
        )

        is LoadingState.Error -> DisplayStackError()
    }
}

@Composable
private fun LoadingStackList() {
    LinearProgressIndicator()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderColumn(
    listState: LazyListState,
    state: LoadingState.Collected<List<MemoStack>>,
    viewModel: FolderViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState,
    update: () -> Unit
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
                        viewModel = viewModel,
                        navController = navController,
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
                            SnackbarResult.ActionPerformed -> {
                                //Undo action performed
                                viewModel.cancelStackDeletion(stack)
                                update()
                            }

                            SnackbarResult.Dismissed -> {
                                viewModel.deleteStackWithDelay(stack)
                            }
                        }
                    }
                }
            )
        }
        item { Spacer(modifier = Modifier.padding(40.dp))}
    }
    if (state.content.isEmpty()) {
        InvitationToCreateStack()
    }

}
@Preview
@Composable
fun InvitationToCreateStack() {
    Card(elevation = 4.dp, border = BorderStroke(1.dp, color = Color.Gray), shape = RoundedCornerShape(8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(fontSize = 20.sp, text = stringResource(id = R.string.create_first_stack), modifier = Modifier.padding(bottom = 10.dp))
            Image(painterResource(id = R.drawable.noun_create_1202533), stringResource(id = R.string.create_first_stack))
        }
    }
}

@Composable
fun ShowStack(
    stack: MemoStack,
    viewModel: FolderViewModel,
    navController: NavController,
    modifier: Modifier
) {
    StackListItem(
        stack = stack,
        modifier = modifier,
        onPlay = {
            val action = FolderScreenFragmentDirections.toMemorization(stack.stackId)
            navController.navigate(action)
        },
        onAdd = {
            navController.navigate(
                FolderScreenFragmentDirections.toNewPairFragment(
                    NewPairNavArgs.NewWordPair(stackId = stack.stackId)
                )
            )
        },
        onPin = {
//            stackViewModel.onPin()//TODO
            animateToTop()
        },
        onClickRow = {
            navController.navigate(FolderScreenFragmentDirections.toStackScreen(stack.stackId))
        }
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


