package com.example.android.memoization.ui.theme

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MemoTextFieldColors(): TextFieldColors {
    return textFieldColors(
        textColor = Color.DarkGray,
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent

    )
}

@Composable
fun AddTextFieldColors():TextFieldColors {
    return textFieldColors(
        backgroundColor = Color.White,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
}

@Composable
fun MemoButtonColors(): ButtonColors {
    return buttonColors(
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.primary,
        disabledBackgroundColor = Color.Transparent,
        disabledContentColor = Color.Gray
    )
}
enum class PlayColors(val hex: String) {
    itsok("#a4a2a8"),
    itsstillok("#df8879"),
    shoulddosomework("#c86558"),
    actionneeded("#b04238"),
    timetolearn("#991f17");

    fun getColor(): Color {
        return Color(android.graphics.Color.parseColor(hex))
    }
}
