package com.example.android.memoization.ui.icons

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.android.memoization.R


@Composable
fun ClickableVectorIcon(modifier: Modifier = Modifier, imageVector: ImageVector, onClick: () -> Unit) {
    Icon(
        imageVector = imageVector,
        contentDescription = stringResource(R.string.app_menu),
        modifier = modifier
            .clickable { onClick() },
        tint = MaterialTheme.colors.primaryVariant)
}