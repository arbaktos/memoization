package com.example.android.memoization.ui.composables.screens

import android.graphics.drawable.shapes.Shape
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.model.Stack
import com.example.android.memoization.notifications.NotificationReceiver
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.*
import com.example.android.memoization.ui.theme.PlayColors
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
//    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scope = rememberCoroutineScope()

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
            drawerContent = { MenuDrawer(scaffoldState) }
                                                                                             ,

            topBar = { AppBar(name = stringResource(id = R.string.app_name)) {
                scope.launch { scaffoldState.drawerState.open() }
            }
            }
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
        horizontalAlignment = Alignment.CenterHorizontally,
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
    navController: NavController,
    stackViewModel: StackViewModel
) {
    StackListItem(
        stack = stack,
        onPlay = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.Memorization.route)
        },
        onAdd = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.NewPair.route)
        },
        onPin = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            stackViewModel.onPin()
            animateToTop()
        },
        onClickRow = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.Stack.route)
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
            modifier = Modifier.padding(12.dp))
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


