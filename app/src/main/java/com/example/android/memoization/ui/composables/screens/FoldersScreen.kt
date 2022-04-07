package com.example.android.memoization.ui.composables.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.model.Folder
import com.example.android.memoization.model.Stack
import com.example.android.memoization.notifications.Notification
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.ID_NO_FOLDER
import com.example.android.memoization.utils.NavScreens

//TODO make function shorter and more readable
const val TDEBUG = "memoization_debug"

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    folderViewModel: FolderViewModel,
    stackViewModel: StackViewModel,
) {
    val context = LocalContext.current
    MemoizationTheme {
        Scaffold(
            topBar = { AppBar(name = "Memoization") }
        ) {
            BodyContent(
                viewModel = folderViewModel,
                navController = navController,
                stackViewModel = stackViewModel
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun BodyContent(
    viewModel: FolderViewModel,
    navController: NavController,
    stackViewModel: StackViewModel,
) {
    val appState by viewModel.publicAppState.collectAsState()
    val currentFolder = appState.folders[0]

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(currentFolder.stacks) {
            ShowStack(
                stack = it,
                viewModel = viewModel,
                navController = navController,
                stackViewModel = stackViewModel
            )

        }
        item {
            AddStack(appState.currentFolder, viewModel)
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
    StackRow(
        stack = stack,
        onClickPlay = {
            viewModel.changeCurrentStack(stack)
            navController.navigate(NavScreens.Memorization.route)
        },
        onClickRow = {
            viewModel.changeCurrentStack(stack)
            stackViewModel.setCurrentStack(stack)
            navController.navigate(NavScreens.Stack.route)
        },
        onDelete = {
            viewModel.deleteStackFromDb(stack)
        }
    )
}

@ExperimentalComposeUiApi
@Composable
fun AddStack(
    folder: Folder,
    viewModel: FolderViewModel,
    onClickSup: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") }
    var isChosen = text.isNotBlank()

    val onClick: () -> Unit = {
        onTextChange("")
        viewModel.addStackToFolder(folder = folder, stack = Stack(text))
        keyboardController?.hide()
        focusManager.clearFocus()
        onClickSup()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = { isChosen = true })
            .focusRequester(focusRequester)
    ) {
        if (folder != null) {
            Spacer(modifier = Modifier.width(30.dp))
        }
        AddStackTextField(
            text = text,
            onTextChange = {
                onTextChange(it)
                isChosen = true
            },
            label = "Add a stack",
            onFinish = onClick,
            modifier = Modifier.weight(1f)
        )
        if (isChosen) {
            SubmitIcon(
                inputName = text,
                onFinish = onClick
            )
        }
    }
}

@Composable
fun StackRow(
    stack: Stack,
    onClickRow: () -> Unit,
    onClickPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
//            .padding(top = 4.dp, end = 15.dp, bottom = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_stack),
            contentDescription = "stack symbol",
            modifier = Modifier
                .size(50.dp)
                .padding(top = 15.dp)
        )
        H5TextBox(
            text = stack.name,
            modifier = Modifier
                .weight(1f)
                .clickable { onClickRow() })
        if (stack.hasWords) {
            Row {
                H6TextBox(
                    text = stack.words.size.toString(),
                    modifier = Modifier
                )
                RowIcon(
                    iconSource = Icons.Filled.PlayCircle,
                    contentDesc = "Start stack memorization",
                    onClick = { onClickPlay() }
                )
            }

        }
        RowIcon(
            iconSource = Icons.Filled.Delete,
            contentDesc = "Delete wordpair",
            onClick = onDelete,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}


@Composable
fun AppBar(name: String) {
    TopAppBar(
        title = { Text(name) },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Settings, null)
            }
        })
}


