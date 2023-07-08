package com.example.android.memoization.ui.features.addnewpair

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.memoization.ui.theme.MemoizationTheme
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.Fab
import com.example.android.memoization.ui.composables.components.RowIcon
import com.example.android.memoization.ui.composables.components.ShowToast
import com.example.android.memoization.ui.features.stackscreen.DisplayStackError
import com.example.android.memoization.ui.theme.AddTextFieldColors
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import com.example.android.memoization.utils.NewPairNavArgs
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "AddNewPairScreen"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddNewPairScreen(
    navController: NavController,
    viewModel: AddNewPairViewModel = hiltViewModel(),
    args: NewPairNavArgs
) {
    viewModel.setArgs(args)
    var state by remember { mutableStateOf<LoadingState<WordPair>>(LoadingState.Loading) }

    LaunchedEffect(key1 = state, block = {
        state = viewModel.getDataToDisplay().stateIn(this).value
    })

    val toastMessage by viewModel.toastMessage.observeAsState()
    ShowToast(text = toastMessage)

    val keyboardController = LocalSoftwareKeyboardController.current

    val onConfirm: () -> Unit = {
        viewModel.onConfirm()
        keyboardController?.hide()
        navController.popBackStack()
    }

    BackHandler(enabled = true) {
        viewModel.onBackPressed(navController)
    }

    ShowNewPairScreenState(
        state = state,
        viewModel = viewModel,
        onAdd = onConfirm,
        onTranslate = { viewModel.onTranslate() }
    )
}

@Composable
fun ShowNewPairScreenState(
    state: LoadingState<WordPair>,
    viewModel: AddNewPairViewModel,
    onTranslate: () -> Unit,
    onAdd: () -> Unit
) {
    when (state) {
        is LoadingState.Loading -> DisplayWordPair(
            wordPair = null,
            viewModel = viewModel,
            onConfirm = onAdd,
            onTranslate = onTranslate
        )
        is LoadingState.Collected -> DisplayWordPair(
            wordPair = state.content,
            viewModel = viewModel,
            onConfirm = onAdd,
            onTranslate = onTranslate
        )
        is LoadingState.Error -> DisplayStackError()
    }
}

@Composable
fun DisplayWordPair(
    wordPair: WordPair?,
    viewModel: AddNewPairViewModel,
    onTranslate: () -> Unit,
    onConfirm: () -> Unit
) {
    MemoizationTheme {
        Scaffold(
            floatingActionButton = {
                Fab(
                    icon = Icons.Filled.Done,
                    contentDesc = "",
                    onClick = onConfirm
                )
            }
        ) { _ ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .padding(start = 40.dp, end = 40.dp)
            ) {
                val coroutineScope = rememberCoroutineScope()

                UpperField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 35.dp),
                    viewModel = viewModel,
                    editWord = wordPair?.word1
                )
                RowIcon(iconSource = Icons.Filled.Translate,
                    contentDesc = stringResource(R.string.translate_btn_desc),
                    onClick = {
                        coroutineScope.launch {
                            onTranslate()
                        }
                    })
                BottomField(
                    modifier = Modifier.weight(1f), onConfirm, viewModel, word2 =  wordPair?.word2
                )
            }
        }
    }
}

@Composable
fun UpperField(
    modifier: Modifier,
    viewModel: AddNewPairViewModel,
    editWord: String?
) {
    val text1 = rememberSaveable { mutableStateOf(editWord ?: Empty_string) }
    viewModel.word1 = text1.value

    val focusRequester = remember { FocusRequester() }

    NewPairCard(
        modifier = modifier
            .padding(bottom = 8.dp),
    ) {
        NewPairTextField(
            text = text1.value,
            onTextChange = {
                text1.value = it
                viewModel.word1 = it
            },
            label = stringResource(R.string.word_to_learn),
            modifier = Modifier.focusRequester(focusRequester)
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun BottomField(
    modifier: Modifier,
    onClick: () -> Unit,
    viewModel: AddNewPairViewModel,
    translation: String? = null,
    word2: String?
) {
    val textVal = translation ?: word2 ?: Empty_string
    val text2 = rememberSaveable { mutableStateOf(textVal) }
    viewModel.word2 = text2.value

    NewPairCard(
        modifier = modifier
            .padding(top = 8.dp)
    ) {
        NewPairTextField(
            text = text2.value,
            onTextChange = {
                text2.value = it
                viewModel.word2 = it
            },
            label = stringResource(R.string.word2_label),
            onClick = onClick,
            imeAction = ImeAction.Done
        )
    }
}

@Composable
fun NewPairCard(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        content()
    }
}

@Composable
fun NewPairTextField(
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    onClick: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text(label) },
        colors = AddTextFieldColors(),
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onClick() }
        ),
        modifier = modifier.fillMaxSize()
    )
}
