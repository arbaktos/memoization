package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.memoization.domain.model.DismissableItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismiss(
    item: DismissableItem,
    dismissContent: @Composable () -> Unit,
    onDismiss: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                item.isVisible = false
                scope.launch {
                    onDismiss()
                }
            }
            true
        })

    SwipeToDismiss(
        directions = setOf(DismissDirection.EndToStart),
        state = dismissState,
        dismissThresholds = { direction ->
            FractionalThreshold(0.5f)
        },
        background = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(horizontal = 20.dp),

                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Localized description",
                    tint = Color.Red
                )
            }
        },
        dismissContent = {
                if (item.isVisible) {
                    dismissContent()
                }
            })
}