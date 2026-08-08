package com.example.android.memoization.ui.composables.buttons

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.android.memoization.R
import com.example.android.memoization.ui.theme.memoButtonColors

@Composable
fun CancelButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick,
        content = { Text(stringResource(R.string.cancel)) },
        colors = memoButtonColors(),
        elevation = null
    )
}