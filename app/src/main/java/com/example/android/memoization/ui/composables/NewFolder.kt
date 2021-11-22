package com.example.android.memoization.ui.composables

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import com.example.android.memoization.model.Folder


const val TAG = "debug"
@ExperimentalComposeUiApi
@Composable
fun NewFolderScreen(
    navController: NavController,
    onAddFolder: (Folder) -> Unit
) {
    Scaffold(
        topBar = { AppBar(name = "New folder") }
    ) {
        EtField(
            navigate = { navController.navigate("folders") },
            onAddFolder = onAddFolder
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun EtField(
    navigate: () -> Unit,
    onAddFolder: (Folder) -> Unit,
) {
    Log.d("newfolder", "etfield")
    val (text, onTextChange) = rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val onDone: () -> Unit = {
        onTextChange("")
        navigate()
        keyboardController?.hide()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AddStackTextField(
            text = text,
            modifier = Modifier.weight(1f),
            onTextChange = onTextChange,
            label = "Folder name",
            onAddFolder = onAddFolder,
            onFinish = onDone
        )
        SubmitIcon(
            inputName = text,
            onAddFolder = onAddFolder,
            onFinish = onDone
        )
    }
}



