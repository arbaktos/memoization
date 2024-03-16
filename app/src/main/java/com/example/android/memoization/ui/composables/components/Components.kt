package com.example.android.memoization.ui.composables.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.folderscreen.TDEBUG
import com.example.android.memoization.utils.getValue
import com.example.android.memoization.utils.putValue
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DatastoreChip(modifier: Modifier = Modifier, label: Int, dataStoreKey: Preferences.Key<Boolean>, dataStore: DataStore<Preferences>, initialSelected: Boolean = true) {
    val savedSelected = dataStore.getValue(dataStoreKey, false).collectAsState(initial = initialSelected).value
    var selected by remember { mutableStateOf(savedSelected) }
    selected = savedSelected
    val scope = rememberCoroutineScope()
    val onClick = {
        selected = !selected
        scope.launch {
            dataStore.putValue(dataStoreKey, selected)
        }
    }
    FilterChip(selected = selected, onClick = { onClick() }, modifier = modifier.padding(0.dp), colors = ChipDefaults.filterChipColors(backgroundColor = Color.LightGray, selectedBackgroundColor = MaterialTheme.colors.primary)) {
        androidx.compose.material3.Text(stringResource(id = label), color = if (selected) Color.White else Color.Black)
    }

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
    onFinish: () -> Unit
) {
    Icon(
        imageVector = Icons.Outlined.Done,
        contentDescription = inputName,
        tint = MaterialTheme.colors.surface,
        modifier = Modifier
            .padding(8.dp)
            .size(30.dp)
            .padding(4.dp)
            .background(
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(enabled = true) {
                onFinish()
            }
    )
}

@Composable
fun AddStackTextField(
    text: String,
    modifier: Modifier = Modifier,
    onTextChange: (String) -> Unit,
    label: String,
    onFinish: () -> Unit,
    showTrailingIcon: Boolean
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.h5,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                onFinish()
            }),
        placeholder = { Label(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add a stack",
                tint = MaterialTheme.colors.onSurface
            )
        },
        trailingIcon = {
            if (showTrailingIcon) {
                Log.d(TDEBUG, "showTrailingIcon % $showTrailingIcon")
                SubmitIcon(inputName = text, onFinish = onFinish)
            }
        }
    )
}

@Composable
fun ShowToast(
    text: Any?
) {
    text?.let {
        val context = LocalContext.current
        when (text) {
            is String -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            is Int -> Toast.makeText(context, context.getString(text), Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }
}

@Composable
fun Label(text: String) {
    Text(text = text, fontSize = 20.sp)
}

@Composable
fun Fab(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = { onClick() },
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
fun CustomFab(modifier: Modifier = Modifier, isVisible: Boolean = true, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    if (isVisible) {
        Image(
            painter = painterResource(id = R.drawable.noun_create_1202533),
            contentDescription = stringResource(R.string.add_new_stack),
            modifier = modifier.clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            }
        )
    }

}

@Composable
fun AddNewCardFab(onAdd: () -> Unit) {
    Fab(
        icon = Icons.Filled.Add,
        contentDesc = stringResource(R.string.add_new_card),
        onClick = onAdd
    )
}