package com.example.android.memoization.ui.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.memoization.R
import com.example.android.memoization.model.Folder

@Composable
fun RowIcon(
    modifier: Modifier = Modifier,
    iconSource: ImageVector,
    contentDesc: String,
    onClick: () -> Unit = {},
    imageSize: Dp = 30.dp,
    tint: Color = colorResource(id = R.color.beige),

    ) {
    Icon(
        imageVector = iconSource,
        contentDescription = contentDesc,
        modifier = modifier
            .padding(4.dp)
            .clickable {
                onClick()
            }
            .size(imageSize),
        tint = tint
    )
}

@Composable
fun MemoIcon(
    contentDesc: String,
    tint: Color,
    onClick: () -> Unit
) {
    RowIcon(
        iconSource = Icons.Filled.Circle,
        contentDesc = contentDesc,
        imageSize = 70.dp,
        tint = tint,
        onClick = onClick
    )
}

@Composable
fun SubmitIcon(
    inputName: String,
    onAddFolder: (Folder) -> Unit = {},
    onFinish: () -> Unit
) {
    Icon(
        Icons.Filled.Done, "Add a folder",
        tint = MaterialTheme.colors.surface,
        modifier = Modifier
            .padding(8.dp)
            .size(40.dp)
            .padding(4.dp)
            .background(
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = true) {
                onAddFolder(Folder(inputName))
                onFinish()
            }
    )
}

@Composable
fun H5TextBox(text: String, modifier: Modifier) {
    Text(
        modifier = modifier.padding(4.dp),
        text = text,
        style = MaterialTheme.typography.h5
    )
}

@Composable
fun AddStackTextField(
    text: String,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit,
    label: String,
    onAddFolder: (Folder) -> Unit = {},
    onFinish: () -> Unit
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.h5,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                onAddFolder(Folder(text))
                onFinish()
            }),
//        label = { Label(label) },
        placeholder = { Label(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add a stack",
                tint = MaterialTheme.colors.onSurface
            )
        }
    )
}

@Composable
fun ShowToast(
    text: String
) {
    val context = LocalContext.current
    val toast = {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
    toast()
}

@Composable
fun Label(text: String) {
    Text(text = text, fontSize = 20.sp)
}

@Composable
fun OpenAndCloseIcon(
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Icon(
        painter = if (isOpen) painterResource(id = R.drawable.ic_up_24)
        else painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
        contentDescription = "arrow down",
        tint = MaterialTheme.colors.onSurface,
        modifier = Modifier
            .padding(start = 16.dp)
            .clickable { /*shows all the stacks in the folder */
                onClick()
            }
            .size(40.dp)
    )
}

@Composable
fun Fab(
    icon: ImageVector,
    contentDesc: String,
    onclick: () -> Unit

) {
    FloatingActionButton(
        onClick = { onclick() },
        modifier = Modifier
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = MaterialTheme.colors.surface
        )
    }
}

@Composable
fun NoCardsCard(visible: Boolean, onClick: () -> Unit) {
    val isVisible = remember {
        mutableStateOf(visible)
    }
    if (isVisible.value) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlertDialog(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(0.75f)
//                    .fillMaxHeight(0.7f),
                buttons = {
                    Button(
                        modifier = Modifier.padding(16.dp),
                        onClick = {
                            isVisible.value = false
                            onClick()
                        },
                        content = { Text("Ok") }
                    )
                },
                title = { Text("Nothing to learn now") },
                text = { Text("You've learned everything for today, try tomorrow!") },
                onDismissRequest = onClick
            )


        }
    }


}