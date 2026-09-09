package com.example.android.memoization.ui.features.stackscreen

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.domain.search.PairSearch
import com.example.android.memoization.extensions.checkLength
import com.example.android.memoization.ui.composables.components.AddNewCardFab
import com.example.android.memoization.ui.composables.components.CustomAddFab
import com.example.android.memoization.ui.composables.components.RowIcon
import com.example.android.memoization.ui.composables.components.SwipeToReveal
import com.example.android.memoization.ui.composables.dialog.EditStackDialog
import com.example.android.memoization.ui.features.folderscreen.TDEBUG
import com.example.android.memoization.ui.icons.ClickableVectorIcon
import com.example.android.memoization.ui.navigateToMemorization
import com.example.android.memoization.ui.navigateToEditPair
import com.example.android.memoization.ui.navigateToNewPair
import com.example.android.memoization.ui.theme.indicatorColors
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.launch


const val TAG = "DisplayStack"

@Composable
fun StackScreen(
    navController: NavController,
    viewModel: StackViewModel = hiltViewModel()
) {
    Log.d(TDEBUG, "StackScreen: ")
    val state by viewModel.getDataToDisplay().collectAsStateWithLifecycle(initialValue = LoadingState.Loading)
    val practice by viewModel.practiceSides.collectAsStateWithLifecycle()
    val showDialog by viewModel.showEditStackDialog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Deleting is immediate; the snackbar puts the row back if it was a mistake.
    val deletePair: (WordPair) -> Unit = { wordPair ->
        viewModel.deletePair(wordPair)
        scope.launch {
            val result = snackbarHostState.showOnDeleteSnackBar(wordPair, context)
            Log.d(TDEBUG, "snackbar result = $result")
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        }
    }

    val navigateToNewPair = remember(navController) {
        { stack: MemoStack ->
            navController.navigateToNewPair(
                stackId = stack.stackId,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage
            )
        }
    }
    val navigateToEditPair =
        remember(navController) { { wordPair: WordPair, stack: MemoStack ->
            navController.navigateToEditPair(
                wordPairId = wordPair.wordPairId,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage
            )
        } }
    val navigateToMemorization =
        remember(navController) { { stackId: Long -> navController.navigateToMemorization(stackId) } }

    DisplayStackState(
        state = state,
        showDialog = showDialog,
        navigateToNewPair = navigateToNewPair,
        navigateToEditPair = navigateToEditPair,
        navigateToMemorization = navigateToMemorization,
        updateStack = viewModel::updateStackInDb,
        deletePair = deletePair,
        practice = practice,
        snackbarHostState = snackbarHostState,
        onEditStack = { viewModel.showEditStackDialog(true) },
        onDismissDialog = { viewModel.showEditStackDialog(false) },
        searchQuery = searchQuery,
        onOpenSearch = viewModel::openSearch,
        onCloseSearch = viewModel::closeSearch,
        onSearchQueryChange = viewModel::onSearchQueryChange,
    )
}



@Composable
fun DisplayStackState(
    state: LoadingState<MemoStack>,
    showDialog: Boolean,
    navigateToNewPair: (stack: MemoStack) -> Unit,
    navigateToEditPair: (wordPair: WordPair, stack: MemoStack) -> Unit,
    navigateToMemorization: (stackId: Long) -> Unit,
    updateStack: (stack: MemoStack) -> Unit,
    deletePair: (wordPair: WordPair) -> Unit,
    practice: PracticeSides,
    onEditStack: () -> Unit,
    onDismissDialog: () -> Unit,
    snackbarHostState: SnackbarHostState,
    searchQuery: String? = null,
    onOpenSearch: () -> Unit = {},
    onCloseSearch: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
) {

    when (state) {
        is LoadingState.Collected<MemoStack> -> DisplayStack(
            currentStack = state.content,
            showDialog = showDialog,
            navigateToNewPair = navigateToNewPair,
            navigateToEditPair = navigateToEditPair,
            navigateToMemorization = navigateToMemorization,
            updateStack = updateStack,
            deletePair = deletePair,
            practice = practice,
            onEditStack = onEditStack,
            onDismissDialog = onDismissDialog,
            snackbarHostState = snackbarHostState,
            searchQuery = searchQuery,
            onOpenSearch = onOpenSearch,
            onCloseSearch = onCloseSearch,
            onSearchQueryChange = onSearchQueryChange,
        )

        is LoadingState.Loading -> DisplayLoadingStack()
        is LoadingState.Error -> DisplayStackError()
    }
}

@Composable
fun DisplayStackError() {
    Text(text = stringResource(R.string.error))
}

/**
 * The stack's name with its pencil right beside it, and the search at the far end.
 *
 * MotionAppBar leaves its children unconstrained, so the title never gets laid out -
 * a plain bar until that motion scene is fixed.
 */
@Composable
fun StackAppBar(
    stackName: String,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stackName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (onEdit != null) {
                ClickableVectorIcon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = R.string.edit_stack_desc,
                    onClick = onEdit,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
        if (onSearch != null) {
            ClickableVectorIcon(
                imageVector = Icons.Filled.Search,
                contentDescription = R.string.search_desc,
                onClick = onSearch,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

/**
 * The app bar while searching: the title gives way to a text box, the arrow or the system back
 * closes it, the cross empties it. The keyboard comes up as soon as it opens.
 */
@Composable
fun StackSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    BackHandler(onBack = onClose)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClickableVectorIcon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = R.string.close_search_desc,
            onClick = onClose,
        )
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.search_in_stack)) },
            trailingIcon = if (query.isEmpty()) null else {
                {
                    ClickableVectorIcon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = R.string.clear_search_desc,
                        onClick = { onQueryChange("") },
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .focusRequester(focusRequester)
        )
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
fun DisplayLoadingStack() {
    Scaffold(
        topBar = { StackAppBar(stackName = stringResource(id = R.string.loading)) },
        floatingActionButtonPosition = FabPosition.End,
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
    currentStack: MemoStack,
    showDialog: Boolean,
    practice: PracticeSides,
    snackbarHostState: SnackbarHostState,
    navigateToNewPair: (stack: MemoStack) -> Unit,
    navigateToMemorization: (stackId: Long) -> Unit,
    updateStack: (stack: MemoStack) -> Unit,
    deletePair: (wordPair: WordPair) -> Unit,
    onEditStack: () -> Unit,
    onDismissDialog: () -> Unit,
    navigateToEditPair: (wordPair: WordPair, stack: MemoStack) -> Unit,
    searchQuery: String? = null,
    onOpenSearch: () -> Unit = {},
    onCloseSearch: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    // Newest first: ids grow with insertion, so this is "last added on top".
    val words = remember(currentStack.words, searchQuery) {
        PairSearch.filter(currentStack.words.sortedByDescending { it.wordPairId }, searchQuery ?: "")
    }

    if (showDialog) {
        EditStackDialog(
            stack = currentStack,
            onStackUpdated = updateStack,
            onDismiss = onDismissDialog
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (searchQuery != null) {
                StackSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClose = onCloseSearch,
                )
            } else {
                StackAppBar(
                    stackName = currentStack.name,
                    onSearch = onOpenSearch,
                    onEdit = onEditStack,
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            Column {
                CustomAddFab(
                    isVisible = true,
                    onClick = { navigateToNewPair(currentStack) }
                )
                StackFab(
                    { navigateToMemorization(currentStack.stackId) },
                    hasWordsToLearn = currentStack.hasDue(practice)
                )
            }
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (words.isEmpty() && !searchQuery.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.no_pairs_found),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp)
                    )
                } else {
                    WordList(
                        words = words,
                        navigateToEditPair = { navigateToEditPair(it, currentStack) },
                        deletePair = deletePair,
                        practice = practice,
                        listState = lazyListState,
                    )
                }
            }
        }
    )
}

@Composable
fun StackFab(navigateToMemorization: () -> Unit, hasWordsToLearn: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (hasWordsToLearn) {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = stringResource(R.string.learn),
//                            color = MaterialTheme.colors.surface
                    )
                },
                onClick = navigateToMemorization,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.learn_this_stack),
//                            tint = MaterialTheme.colors.surface
                    )
                },
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordList(
    deletePair: (wordPair: WordPair) -> Unit,
    practice: PracticeSides,
    navigateToEditPair: (wordPair: WordPair) -> Unit,
    listState: LazyListState,
    words: List<WordPair>,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        state = listState,
    ) {
        items(words, key = { it.wordPairId }) { wordPair ->
            SwipeToReveal(
                onDelete = { deletePair(wordPair) },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                WordPairListItem(
                    wordPair = wordPair,
                    practice = practice,
                    onEditNavigate = navigateToEditPair,
                    modifier = Modifier//.animateItemPlacement()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WordPairListItem(
    wordPair: WordPair,
    practice: PracticeSides,
    onEditNavigate: (wordPair: WordPair) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .clickable {
                onEditNavigate(wordPair)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {

            RowIcon(
                iconSource = Icons.Filled.Circle,
                contentDesc = stringResource(R.string.word_indicator),
                tint = indicatorColors(wordPair.level(practice)),
                modifier = Modifier.padding(start = 10.dp)
            )
            ListItem(
                headlineContent = {
                    Text(
                        wordPair.word1.checkLength(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
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
