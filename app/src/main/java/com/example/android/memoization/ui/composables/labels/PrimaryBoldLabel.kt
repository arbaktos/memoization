package com.example.android.memoization.ui.composables.labels

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryBoldLabel(modifier: Modifier = Modifier, text: String, size: TextUnit = 14.sp) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = size,
        color = MaterialTheme.colors.primaryVariant,
        modifier = modifier.padding(12.dp)
    )
}