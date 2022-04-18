package com.example.android.memoization.ui.theme

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MemoTextFieldColors(): TextFieldColors {
    return textFieldColors(
        backgroundColor = Color.Transparent
    )
}

@Composable
fun MemoButtonColors(): ButtonColors {
    return buttonColors(
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.primary
    )
}
