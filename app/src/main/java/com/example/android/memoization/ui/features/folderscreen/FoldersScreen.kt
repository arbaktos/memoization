package com.example.android.memoization.ui.features.folderscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.*
import com.example.android.memoization.ui.theme.PlayColors
import com.example.android.memoization.utils.NewPairNavArgs
import kotlinx.coroutines.launch

const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    folderViewModel: FolderViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    var showAddStackDialog by remember { mutableStateOf(false) } //TODO move to viewmodel
    val scope = rememberCoroutineScope()

    MemoizationTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            floatingActionButton = {
                Fab(
                    icon = Icons.Filled.Add,
                    contentDesc = stringResource(R.string.add_new_stack),
                    onClick = {
                        showAddStackDialog = true
                    })
            },
            drawerContent = { MenuDrawer(scaffoldState) },
            topBar = {
                AppBar(name = stringResource(id = R.string.app_name)) {
                    scope.launch { scaffoldState.drawerState.open() }
                }
            }
        ) { _ ->

            FolderScreenBodyContent(
                viewModel = folderViewModel,
                navController = navController,
                scaffoldState = scaffoldState,
                update = { },
            )
            if (showAddStackDialog)
                AddStackAlertDialog(
                    viewModel = folderViewModel
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
    update: () -> Unit,
) {

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val stacks = viewModel.stacksWithWords.collectAsState(initial = emptyList())

    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(stacks.value) { stack ->
            SwipeToDismiss(
                item = stack,
                dismissContent = {
                    ShowStack(
                        stack = stack,
                        viewModel = viewModel,
                        navController = navController,
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
//                                viewModel.getFoldersWithStackFromDb()
                                update()
                            }
                            SnackbarResult.Dismissed -> {}
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
    navController: NavController
) {
    StackListItem(
        stack = stack,
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


suspend fun SnackbarHostState.showOnStackDeleteSnackBar(stack: Stack): SnackbarResult {
    return showSnackbar(
        message = "Deletion of ${stack.name} with all the words",
        actionLabel = "Undo",
        duration = SnackbarDuration.Short
    )
}


