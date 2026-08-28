package com.example.android.memoization.ui.features.addnewpair

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.extensions.findActivity
import com.example.android.memoization.ui.composables.components.CustomDoneFab
import com.example.android.memoization.ui.composables.components.RowIcon
import com.example.android.memoization.ui.composables.components.ShowToast
import com.example.android.memoization.ui.features.stackscreen.DisplayStackError
import com.example.android.memoization.ui.theme.AddTextFieldColors
import com.example.android.memoization.utils.Empty_string
import com.example.android.memoization.utils.LoadingState
import kotlinx.coroutines.launch

private const val TAG = "AddNewPairScreen"

@Composable
fun AddNewPairScreen(
    navController: NavController,
    viewModel: AddNewPairViewModel = hiltViewModel(),
) {
    val state by viewModel.getDataToDisplay()
        .collectAsStateWithLifecycle(initialValue = LoadingState.Loading)

    val toastMessage by viewModel.toastMessage.observeAsState()
    ShowToast(text = toastMessage)

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val onConfirm: () -> Unit = {
        viewModel.onConfirm()
        keyboardController?.hide()
        // Nothing to go back to means the pair came from a share: close, and the learner is
        // back in the app the word came from.
        if (!navController.popBackStack()) context.findActivity()?.finish()
    }

//    BackHandler(enabled = true) {
//        viewModel.onBackPressed(navController)
//    }

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
    Scaffold(
        floatingActionButton = {
            CustomDoneFab(
                isVisible = true,
                onClick = onConfirm
            )
        }
    ) { _ ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .padding(start = 40.dp, end = 40.dp, top = 35.dp)
        ) {
            val translation by viewModel.translation.collectAsStateWithLifecycle()

            UpperField(
                modifier = Modifier
                    .weight(1f),
                viewModel = viewModel,
                // A shared word is already the word of the pair; only an edit overrides it.
                editWord = wordPair?.word1 ?: viewModel.sharedWord,
                // The word is there, so the cursor belongs in the field that is still empty.
                autoFocus = viewModel.sharedWord == null,
            )

            if (viewModel.needsTranslation()) {
                if (translation is TranslationUiState.Translating) {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                } else {
                    RowIcon(
                        iconSource = Icons.Filled.Translate,
                        contentDesc = stringResource(R.string.translate_btn_desc),
                        onClick = onTranslate
                    )
                }
            }
            BottomField(
                modifier = Modifier.weight(1f),
                onClick = onConfirm,
                viewModel = viewModel,
                word2 = wordPair?.word2,
                translation = translation,
                autoFocus = viewModel.sharedWord != null,
            )
        }
    }
}

@Composable
fun UpperField(
    modifier: Modifier = Modifier,
    viewModel: AddNewPairViewModel,
    editWord: String?,
    autoFocus: Boolean = true,
) {
    // Keyed on editWord so the field picks up the pair once it is loaded from the db
    val text1 = rememberSaveable(editWord) { mutableStateOf(editWord ?: Empty_string) }
    viewModel.word1 = text1.value

    val focusRequester = remember { FocusRequester() }

    NewPairCard(
        modifier = modifier
    ) {
        NewPairTextField(
            text = text1.value,
            onTextChange = {
                text1.value = it
                viewModel.word1 = it
            },
            label = stringResource(R.string.word_to_learn),
            imeAction = ImeAction.Next,
            hintLocale = viewModel.wordKeyboardLocale,
            modifier = Modifier
                .focusRequester(focusRequester)
        )

    }
    LaunchedEffect(Unit) {
        if (autoFocus) focusRequester.requestFocus()
    }
}

@Composable
fun BottomField(
    modifier: Modifier,
    onClick: () -> Unit,
    viewModel: AddNewPairViewModel,
    word2: String?,
    translation: TranslationUiState = TranslationUiState.Idle,
    autoFocus: Boolean = false,
) {
    val textVal = word2 ?: Empty_string
    val text2 = rememberSaveable(textVal) { mutableStateOf(textVal) }
    val focusRequester = remember { FocusRequester() }

    // A finished translation drops into the field, then is cleared so it lands only once.
    LaunchedEffect(translation) {
        val translated = translation as? TranslationUiState.Translated ?: return@LaunchedEffect
        text2.value = translated.word
        viewModel.onTranslationApplied()
    }

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
            // Explanations run to several lines, so Enter inserts one instead of
            // confirming - the done button is the fab.
            imeAction = ImeAction.Default,
            hintLocale = viewModel.meaningKeyboardLocale,
            modifier = Modifier.focusRequester(focusRequester),
        )
    }
    LaunchedEffect(Unit) {
        if (autoFocus) focusRequester.requestFocus()
    }
}

@Composable
fun NewPairCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = MaterialTheme.colors.onSurface, width = Dp.Hairline),
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
    /** BCP 47 tag of the language this field is typed in; the keyboard is asked to switch. */
    hintLocale: String? = null,
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
            hintLocales = hintLocale?.let { LocaleList(Locale(it)) },
        ),
        keyboardActions = KeyboardActions(
            onDone = { onClick() }
        ),
        modifier = modifier
            .fillMaxSize()
    )
}