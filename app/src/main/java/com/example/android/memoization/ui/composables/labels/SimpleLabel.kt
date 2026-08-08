package com.example.android.memoization.ui.composables.labels

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun SimpleLabel(modifier: Modifier = Modifier, fontSize: TextUnit = 14.sp, stringId: Int) {
    Text(
        fontSize = fontSize,
        text = stringResource(id = stringId),
        modifier = modifier
    )
}