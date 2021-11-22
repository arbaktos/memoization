package com.example.android.memoization.ui.composables

import android.net.ConnectivityManager
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.android.memoization.ui.theme.MemoizationTheme
import androidx.navigation.NavController
import com.example.android.memoization.api.WordTranslationRequest
import com.example.android.memoization.extensions.ConnectionState
import com.example.android.memoization.extensions.currentConnectivityState
import com.example.android.memoization.ui.viewmodel.MemoizationViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddNewPairScreen(navController: NavController, viewModel: MemoizationViewModel) {
    val word1 by viewModel.word1.observeAsState()
    val word2 by viewModel.word2.observeAsState()
    val toastMessage by viewModel.toastMessage.observeAsState()
//    val currentStack = viewModel.currentStack.value!!
    val keyboardController = LocalSoftwareKeyboardController.current

    val onTranslate = {
        val request = WordTranslationRequest("en_EN", "ru_RU", word1!!)
        viewModel.getTranslation(request)
    }
    toastMessage?.let {
        ShowToast(text = toastMessage!!)
    }

    val onAdd: () -> Unit = {
        viewModel.composeWordPairFromWords()
        viewModel.addWordPairToDb()
        keyboardController?.hide()
        navController.popBackStack()
        navController.navigate("stack") {
            popUpTo("stack") {
                inclusive = true
            }
            anim { }
        }
    }

    MemoizationTheme {
        Scaffold(
            floatingActionButton = {
                Fab(
                    icon = Icons.Filled.Done,
                    contentDesc = "Done adding word pair",
                    onclick = onAdd
                )
            }
        ) {
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
                    viewModel = viewModel
                )
                RowIcon(iconSource = Icons.Filled.Translate,
                    contentDesc = "translate the word",
                    onClick = {
                        coroutineScope.launch {
                            onTranslate()
                        }
                    })
                BottomField(
                    modifier = Modifier.weight(1f), onAdd , viewModel, word2
                )

            }

        }
    }
}

@Composable
fun ShowNoConnectionToast() {
    val context = LocalContext.current
    val toast = {
        Toast.makeText(context, "No Internet connection", Toast.LENGTH_SHORT).show()
    }
    when (context.currentConnectivityState) {
        is ConnectionState.Unavailable -> toast()
    }
}

@Composable
fun UpperField(modifier: Modifier, viewModel: MemoizationViewModel) {
    val (text1, onTextChange1) = rememberSaveable { mutableStateOf("") }
    viewModel.word1.value = text1
    NewPairCard(
        modifier = modifier
            .padding(bottom = 8.dp),
    ) {
        NewPairTextField(
            text = text1,
            onTextChange = onTextChange1,
            label = "Word to learn"
        )
    }
}

@Composable
fun BottomField(
    modifier: Modifier,
    onClick: () -> Unit,
    viewModel: MemoizationViewModel,
    translation: String?
) {
    var text2 = rememberSaveable { mutableStateOf("") }
    translation?.let {
        text2.value = translation
    }

    NewPairCard(
        modifier = modifier
            .padding(top = 8.dp)
    ) {
        NewPairTextField(
            text = text2.value,
            onTextChange = {
                text2.value = it
                viewModel.word2.value = it
                           },
            label = "Explanation",
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
    imeAction: ImeAction = ImeAction.Default
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text(label) },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onClick() }
        ),
        modifier = Modifier.fillMaxSize()
    )
}
