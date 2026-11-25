package com.example.android.memoization.ui.features.stackscreen

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FabPosition
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import com.example.android.memoization.data.model.Shown
import com.example.android.memoization.data.model.covers
import com.example.android.memoization.data.model.step
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import com.example.android.memoization.ui.features.folderscreen.StackListColors
import com.example.android.memoization.ui.features.folderscreen.stackListColors
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.android.memoization.ui.composables.components.SwipeToReveal
import com.example.android.memoization.ui.composables.dialog.EditStackDialog
import com.example.android.memoization.ui.features.folderscreen.TDEBUG
import com.example.android.memoization.ui.icons.ClickableVectorIcon
import com.example.android.memoization.ui.navigateToMemorization
import com.example.android.memoization.ui.navigateToEditPair
import com.example.android.memoization.ui.navigateToNewPair
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
    val duplicates by viewModel.duplicates.collectAsStateWithLifecycle()
    val reviewStep by viewModel.reviewStep.collectAsStateWithLifecycle()
    val duplicatesBusy by viewModel.duplicatesBusy.collectAsStateWithLifecycle()
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

    when (val step = reviewStep) {
        is ReviewStep.Copies -> RemoveCopiesDialog(
            groups = step.groups,
            busy = duplicatesBusy,
            onRemove = { viewModel.removeCopies(step.groups) },
            onCancel = viewModel::skipCopies,
            onDismiss = viewModel::stopDuplicatesReview,
        )
        is ReviewStep.Merge -> MergePairsDialog(
            proposal = step.proposal,
            practice = practice,
            busy = duplicatesBusy,
            onMerge = { word1, word2 -> viewModel.merge(step.proposal, word1, word2) },
            onCancel = { viewModel.cancelMerge(step.proposal) },
            onDismiss = viewModel::stopDuplicatesReview,
        )
        null -> Unit
    }

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
        onPinStack = viewModel::togglePin,
        onDeleteStack = { stack ->
            viewModel.hideStack {
                // The list offers the undo, as it does after a swipe.
                navController.previousBackStackEntry?.savedStateHandle?.apply {
                    set(FolderViewModel.HIDDEN_STACK_NAME, stack.name)
                    set(FolderViewModel.HIDDEN_STACK_ID, stack.stackId)
                }
                navController.popBackStack()
            }
        },
        searchQuery = searchQuery,
        onOpenSearch = viewModel::openSearch,
        onCloseSearch = viewModel::closeSearch,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        duplicatesToReview = duplicates.toReview,
        onReviewDuplicates = viewModel::startDuplicatesReview,
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
    onPinStack: (MemoStack) -> Unit = {},
    onDeleteStack: (MemoStack) -> Unit = {},
    searchQuery: String? = null,
    onOpenSearch: () -> Unit = {},
    onCloseSearch: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    duplicatesToReview: Int = 0,
    onReviewDuplicates: () -> Unit = {},
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
            onPinStack = onPinStack,
            onDeleteStack = onDeleteStack,
            searchQuery = searchQuery,
            onOpenSearch = onOpenSearch,
            onCloseSearch = onCloseSearch,
            onSearchQueryChange = onSearchQueryChange,
            duplicatesToReview = duplicatesToReview,
            onReviewDuplicates = onReviewDuplicates,
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
 * The stack's name and size, search, and a menu for the rest: edit, pin, delete (design handoff
 * "Экран колоды", header from 8a).
 */
@Composable
fun StackAppBar(
    stackName: String,
    colors: StackListColors,
    modifier: Modifier = Modifier,
    wordCount: Int? = null,
    pinned: Boolean = false,
    onSearch: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stackName,
                color = colors.text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (wordCount != null) {
                Text(
                    text = pluralStringResource(R.plurals.stack_word_count, wordCount, wordCount),
                    color = colors.secondaryText,
                    fontSize = 14.sp,
                )
            }
        }
        if (onSearch != null) {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, stringResource(R.string.search_desc), tint = colors.icon)
            }
        }
        if (onEdit != null && onPin != null && onDelete != null) {
            var open by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { open = true }) {
                    Icon(Icons.Filled.MoreVert, stringResource(R.string.stack_menu), tint = colors.icon)
                }
                DropdownMenu(
                    expanded = open,
                    onDismissRequest = { open = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = colors.menu,
                ) {
                    val item: @Composable (Int, Color, () -> Unit) -> Unit = { label, color, action ->
                        DropdownMenuItem(
                            text = { Text(stringResource(label), color = color, fontSize = 15.sp) },
                            onClick = { open = false; action() },
                            modifier = Modifier.height(44.dp),
                        )
                    }
                    item(R.string.edit_stack_desc, colors.text, onEdit)
                    item(if (pinned) R.string.unpin_stack else R.string.pin_stack, colors.text, onPin)
                    item(R.string.delete_stack, colors.danger, onDelete)
                }
            }
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
        containerColor = stackListColors().background,
        topBar = { StackAppBar(stackName = stringResource(id = R.string.loading), colors = stackListColors()) },
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
    onPinStack: (MemoStack) -> Unit = {},
    onDeleteStack: (MemoStack) -> Unit = {},
    searchQuery: String? = null,
    onOpenSearch: () -> Unit = {},
    onCloseSearch: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    duplicatesToReview: Int = 0,
    onReviewDuplicates: () -> Unit = {},
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

    val colors = stackListColors()
    Scaffold(
        containerColor = colors.background,
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
                    colors = colors,
                    wordCount = currentStack.words.size,
                    pinned = currentStack.pinnedTime != null,
                    onSearch = onOpenSearch,
                    onEdit = onEditStack,
                    onPin = { onPinStack(currentStack) },
                    onDelete = { onDeleteStack(currentStack) },
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            StackFabs(
                colors = colors,
                onAdd = { navigateToNewPair(currentStack) },
                // Not offered when nothing is due; the stack list has Study anyway for that.
                onStudy = if (currentStack.hasDue(practice)) {
                    { navigateToMemorization(currentStack.stackId) }
                } else null,
            )
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
                    // Above the list, not in it: an item added on top of a list already showing
                    // would be scrolled out of sight.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (searchQuery == null && duplicatesToReview > 0) {
                            DuplicatesStrip(
                                toReview = duplicatesToReview,
                                onReview = onReviewDuplicates,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            )
                        }
                        WordList(
                            colors = colors,
                            words = words,
                            navigateToEditPair = { navigateToEditPair(it, currentStack) },
                            deletePair = deletePair,
                            practice = practice,
                            listState = lazyListState,
                        )
                    }
                }
            }
        }
    )
}

/** Add a word and Study, stacked at the bottom right; Study only when [onStudy] is given. */
@Composable
fun StackFabs(colors: StackListColors, onAdd: () -> Unit, onStudy: (() -> Unit)?) {
    val shape = RoundedCornerShape(18.dp)
    val fab = Modifier
        .height(56.dp)
        .shadow(12.dp, shape, ambientColor = colors.fabShadow, spotColor = colors.fabShadow)
        .clip(shape)
    Column(
        modifier = Modifier.padding(bottom = 12.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = fab
                .size(56.dp)
                .background(colors.plusFill)
                .clickable(role = Role.Button, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, stringResource(R.string.add_new_card), tint = colors.plusIcon, modifier = Modifier.size(22.dp))
        }
        if (onStudy != null) {
            Row(
                modifier = fab
                    .background(colors.accent)
                    .clickable(role = Role.Button, onClick = onStudy)
                    .padding(start = 18.dp, end = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.study),
                    color = colors.onAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordList(
    colors: StackListColors,
    deletePair: (wordPair: WordPair) -> Unit,
    practice: PracticeSides,
    navigateToEditPair: (wordPair: WordPair) -> Unit,
    listState: LazyListState,
    words: List<WordPair>,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        // Room under the last row for the two buttons.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 168.dp),
        modifier = Modifier.fillMaxWidth(),
        state = listState,
    ) {
        items(words, key = { it.wordPairId }) { wordPair ->
            SwipeToReveal(
                onDelete = { deletePair(wordPair) },
                modifier = Modifier.fillMaxWidth()
            ) {
                WordPairListItem(
                    wordPair = wordPair,
                    practice = practice,
                    colors = colors,
                    onEditNavigate = navigateToEditPair,
                )
            }
        }
    }
}

/**
 * A word and its meaning on one surface, each with a dot for how well that side is known: the
 * word's dot is the side shown as the word, the meaning's the side shown as the meaning. A side
 * the practice setting never asks for has no dot, only the space for one.
 */
@Composable
fun WordPairListItem(
    wordPair: WordPair,
    practice: PracticeSides,
    colors: StackListColors,
    onEditNavigate: (wordPair: WordPair) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(colors.rowElevation, shape, ambientColor = colors.cardShadow, spotColor = colors.cardShadow)
            .clip(shape)
            .background(colors.card)
            .clickable { onEditNavigate(wordPair) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        SideLine(wordPair, Shown.WORD, practice, colors) {
            Text(
                wordPair.word1.checkLength(),
                color = colors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        wordPair.word2?.checkLength()?.let { meaning ->
            SideLine(wordPair, Shown.MEANING, practice, colors) {
                Text(meaning, color = colors.secondaryText, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SideLine(
    wordPair: WordPair,
    shown: Shown,
    practice: PracticeSides,
    colors: StackListColors,
    text: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val dot = if (practice.covers(shown)) wordPair.sides.firstOrNull { it.shown == shown }?.step ?: 0 else null
        Box(
            Modifier
                .size(8.dp)
                .then(if (dot != null) Modifier.background(colors.levels[dot], CircleShape) else Modifier)
        )
        text()
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
