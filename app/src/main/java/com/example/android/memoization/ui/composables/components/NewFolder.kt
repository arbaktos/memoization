package com.example.android.memoization.ui.composables.components

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
import com.example.android.memoization.ui.composables.AddStackTextField
import com.example.android.memoization.ui.composables.SubmitIcon


const val TAG = "debug"
@ExperimentalComposeUiApi
@Composable
fun NewFolderScreen(
    navController: NavController
) {
    Scaffold(
        topBar = { com.example.android.memoization.ui.composables.screens.AppBar(name = "New folder") }
    ) {
        EtField(
            navigate = { navController.navigate("folders") },
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun EtField(
    navigate: () -> Unit,
) {
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
            onFinish = onDone
        )
        SubmitIcon(
            inputName = text,
            onFinish = onDone
        )
    }
}



