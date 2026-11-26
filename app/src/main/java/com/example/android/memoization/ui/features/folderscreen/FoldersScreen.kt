package com.example.android.memoization.ui.features.folderscreen

import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.ui.composables.components.StackListItem
import com.example.android.memoization.ui.composables.components.SwipeToDismiss
import com.example.android.memoization.ui.composables.dialog.AddStackDialog
import com.example.android.memoization.ui.composables.labels.SimpleLabel
import com.example.android.memoization.ui.features.settings.MenuDrawer
import com.example.android.memoization.ui.features.stackscreen.DisplayStackError
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.launch

const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    preferenceStorage: DataStore<Preferences>,
    viewModel: FolderViewModel = hiltViewModel(),
) {
    Log.d(TDEBUG, "FoldersScreen: ")
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val state by viewModel.getDataToDisplay().collectAsState(initial = LoadingState.Loading)
    val toShowDialog = viewModel.showAddStackDialog.observeAsState(false)
    val colors = stackListColors()
    val context = LocalContext.current
    val hiddenFromStackScreen by viewModel.hiddenFromStackScreen.collectAsState()
    LaunchedEffect(hiddenFromStackScreen) {
        val stackId = hiddenFromStackScreen ?: return@LaunchedEffect
        val name = viewModel.hiddenStackName()
        viewModel.onHiddenStackShown()
        if (scaffoldState.snackbarHostState.showOnStackDeleteSnackBar(name, context) == SnackbarResult.ActionPerformed) {
            viewModel.unhideStack(stackId)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
            backgroundColor = colors.background,
            floatingActionButton = {
                val collectedState = state
                if (collectedState is LoadingState.Collected && collectedState.content.isNotEmpty()) {
                    NewStackFab(colors) { viewModel.showAddStackDialog(true) }
                }
            },
            drawerContent = { MenuDrawer(preferenceStorage = preferenceStorage) },
            topBar = {
                AppBar(name = stringResource(id = R.string.app_name), colors = colors) {
                    scope.launch { scaffoldState.drawerState.open() }
                }
            }
        ) { padding ->
            FolderScreenBodyContent(
                viewModel = viewModel,
                navController = navController,
                scaffoldState = scaffoldState,
                state = state,
                modifier = Modifier.padding(padding)
            )
            if (toShowDialog.value) {
                AddStackDialog(
                    onDismiss = { viewModel.showAddStackDialog(false) },
                    onStackAdded = { viewModel.addStackAndOpenIt(it, navController) }
                )
            }
        }
}

@ExperimentalComposeUiApi
@Composable
fun FolderScreenBodyContent(
    modifier: Modifier = Modifier,
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
    if (state.content.isEmpty()) InvitationToCreateStack { viewModel.showAddStackDialog(true) }
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
    val practice by viewModel.practiceSides.collectAsState()
    val context = LocalContext.current
    val colors = stackListColors()
    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        // Room under the last card for the New stack button.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            state.content, key = { stack -> stack.stackId },
        ) { stack ->
            SwipeToDismiss(
                item = stack,
                dismissContent = {
                    ShowStack(
                        stack = stack,
                        practice = practice,
                        colors = colors,
                        onAddNewWord = { viewModel.onAddNewWord(navController, stack) },
                        onNavigateToStack = {
                            viewModel.onNavigateToStack(
                                navController,
                                stack.stackId
                            )
                        },
                        onPlayWords = { ahead -> viewModel.onPlayWords(navController, stack.stackId, ahead) },
                        onPin = { viewModel.onPin(stack) },
                        modifier = Modifier//.animateItemPlacement()
                    )
                },
                onDismiss = {
                    scope.launch {
                        // Hidden, never deleted; undo just puts it back.
                        viewModel.hideStack(stack)
                        if (scaffoldState.snackbarHostState.showOnStackDeleteSnackBar(stack, context) ==
                            SnackbarResult.ActionPerformed
                        ) {
                            viewModel.unhideStack(stack)
                        }
                    }
                }
            )

        }
    }
}


//TODO change design
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
                SimpleLabel(
                    fontSize = 20.sp,
                    stringId = R.string.create_first_stack,
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
    practice: PracticeSides,
    colors: StackListColors,
    onPlayWords: (ahead: Boolean) -> Unit,
    onNavigateToStack: () -> Unit,
    onAddNewWord: () -> Unit,
    onPin: () -> Unit,
    modifier: Modifier,
) {
    StackListItem(
        stack = stack,
        practice = practice,
        colors = colors,
        modifier = modifier,
        onPlay = onPlayWords,
        onAdd = onAddNewWord,
        onPin = onPin,
        onClick = onNavigateToStack
    )
}

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    name: String,
    colors: StackListColors,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, color = colors.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onClick) {
            Icon(
                Icons.Filled.Menu,
                contentDescription = stringResource(R.string.app_menu),
                tint = colors.icon,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Extended, so it says what it makes; shown once there is a stack, the empty list has its own. */
@Composable
private fun NewStackFab(colors: StackListColors, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .height(56.dp)
            .shadow(12.dp, shape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.35f))
            .clip(shape)
            .background(colors.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
        Text(
            text = stringResource(R.string.new_stack),
            color = colors.onAccent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}


suspend fun SnackbarHostState.showOnStackDeleteSnackBar(stack: MemoStack, context: Context): SnackbarResult =
    showOnStackDeleteSnackBar(stack.name, context)

suspend fun SnackbarHostState.showOnStackDeleteSnackBar(name: String, context: Context): SnackbarResult {
    return showSnackbar(
        message = context.getString(R.string.stack_deleted, name),
        actionLabel = context.getString(R.string.undo),
        duration = SnackbarDuration.Short
    )
}
