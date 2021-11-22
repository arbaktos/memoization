package com.example.android.memoization.ui.composables

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.MemoizationTheme
import com.example.android.memoization.ui.viewmodel.MemoizationViewModel
import com.example.android.memoization.model.Folder
import com.example.android.memoization.model.Stack
import com.example.android.memoization.support.NavScreens
import com.example.android.memoization.utils.ID_NO_FOLDER
import kotlin.math.roundToInt


//TODO make new folder as a pop up fragment in folders
//TODO mqke function shorter and more readable

@ExperimentalComposeUiApi
@Composable
fun FoldersScreen(
    navController: NavController,
    viewModel: MemoizationViewModel
) {
    MemoizationTheme {
        Scaffold(
            floatingActionButton = {
                Fab(
                    icon = Icons.Filled.Add,
                    contentDesc = "Add folder"
                ) {
                    navController.navigate(NavScreens.NewFolder.name)
                }
            }, //choose languages here??
            topBar = { AppBar(name = "Memoization") }
        ) {
            BodyContent(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun BodyContent(
    viewModel: MemoizationViewModel,
    navController: NavController
) {
    val stacks = viewModel.currentFolder.value.stacks
    val foldersAll by viewModel.folders.collectAsState()
    val folders = foldersAll.filter { it.folderId != ID_NO_FOLDER }
    val noFolder = foldersAll.filter { it.folderId == ID_NO_FOLDER }.firstOrNull()


    Column(modifier = Modifier.padding(8.dp)) {
        if (folders.isNotEmpty()) {
            folders.forEach {
                Column {
                    var addStackToFolder by remember { mutableStateOf(false) }
                    Folder(
                        folder = it,
                        navContoller = navController,
                        viewModel = viewModel,
                        onAddStack = { addStackToFolder = true },
                    )
                    if (addStackToFolder) {
                        AddStack(
                            it,
                            viewModel = viewModel,
                            onClickSup = { addStackToFolder = false }
                        )
                    }
                }
            }

        } else {
            /*TODO make a plug with a motivation to create first stack*/
        }
        if (stacks.isNotEmpty()) {
            stacks.forEach {
                ShowStack(
                    stack = it,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
        noFolder?.stacks?.forEach {
            ShowStack(
                stack = it,
                viewModel = viewModel,
                navController = navController
            )
        }
        AddStack(null, viewModel)
//        var offsetX by remember { mutableStateOf(0f) }
//        Box(modifier = Modifier.fillMaxSize()) {
//            var offsetX by remember { mutableStateOf(0f) }
//            var offsetY by remember { mutableStateOf(0f) }
//
//            Box(
//                Modifier
//                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
//                    .background(Color.Blue)
//                    .size(50.dp)
//                    .pointerInput(Unit) {
//                        detectDragGestures { change, dragAmount ->
//                            change.consumeAllChanges()
//                            offsetX += dragAmount.x
//                            offsetY += dragAmount.y
//                        }
//                    }
//            )
//        }
    }


}

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalComposeUiApi
@Composable
fun Folder(
    folder: Folder,
    navContoller: NavController,
    viewModel: MemoizationViewModel,
    onAddStack: () -> Unit
) {
    Column {
        var isOpen by remember { mutableStateOf(folder.isOpen) }
        folder.isOpen = isOpen

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
        ) {
            val iconSrouce = if (isOpen) Icons.Filled.FolderOpen else Icons.Filled.Folder
            RowIcon(
                iconSource = iconSrouce,
                contentDesc = "Folder icon",
                onClick = { isOpen = !isOpen }
            )

            H5TextBox(
                text = folder.name,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isOpen = !isOpen }
            )
            if (folder.stacks.isNotEmpty()) {
                RowIcon(
                    iconSource = Icons.Filled.PlayCircle,
                    contentDesc = "Start folder memorization",
                )
            }

            RowIcon(
                iconSource = Icons.Filled.Add,
                contentDesc = "Add stack to this folder",
                onClick = onAddStack
            )

            RowIcon(
                iconSource = Icons.Filled.Delete,
                contentDesc = "Delete wordpair",
                onClick = { viewModel.deleteFolderFromDb(folder) },
                modifier = Modifier.padding(end = 8.dp)
            )
//            OpenAndCloseIcon(
//                isOpen = isOpen,
//                onClick = switchOpen
//            )
        }
        if (isOpen) {
            folder.stacks.forEach {
                Row {
                    Spacer(modifier = Modifier.width(30.dp))
                    ShowStack(
                        stack = it,
                        viewModel = viewModel,
                        navController = navContoller
                    )
                }
            }
        }
    }
}

@Composable
fun ShowStack(stack: Stack, viewModel: MemoizationViewModel, navController: NavController) {
    StackRow(
        stack = stack,
        onClickPlay = {
            viewModel.changeCurrentStack(stack)
            navController.navigate(NavScreens.Memorization.name)
        },
        onClickRow = {
            viewModel.changeCurrentStack(stack)
            navController.navigate(NavScreens.Stack.name)
        },
        onDelete = {
            viewModel.deleteStackFromDb(stack)
        }
    )
}

@ExperimentalComposeUiApi
@Composable
fun AddStack(
    folder: Folder?,
    viewModel: MemoizationViewModel,
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
            .padding(top = 4.dp, end = 15.dp, bottom = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_stack),
            contentDescription = "stack symbol",
            modifier = Modifier.size(50.dp).padding(top = 15.dp)
        )
        H5TextBox(
            text = stack.name,
            modifier = Modifier
                .weight(1f)
                .clickable { onClickRow() })
        if (stack.hasWords) {
            RowIcon(
                iconSource = Icons.Filled.PlayCircle,
                contentDesc = "Start stack memorization",
                onClick = { onClickPlay() }
            )
        }
        RowIcon(
            iconSource = Icons.Filled.Delete,
            contentDesc = "Delete wordpair",
            onClick = onDelete,
            modifier = Modifier.padding(end = 8.dp)
        )
//        RowIcon(
//            iconSource = Icons.Filled.Edit,
//            contentDesc = "Edit folder",
//            onClick = {}
//        )
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


