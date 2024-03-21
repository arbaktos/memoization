package com.example.android.memoization.ui.theme

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.android.memoization.data.model.WordStatus

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
fun AddTextFieldColors(): TextFieldColors {
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


fun indicatorColors(status: WordStatus): Color {
    return when (status) {
        is WordStatus.Level1 -> Color(0xFF93B7BE)
        is WordStatus.Level2 -> Color(0xFF274029)
        is WordStatus.Level3 -> Color(0xFF315C2B)
        is WordStatus.Level4 -> Color(0xFF63C132)
        is WordStatus.Learned -> Color(0xFF40F99B)
    }
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