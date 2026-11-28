package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.memoization.data.model.DismissableItem
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.android.memoization.R

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

    // The swipe state is saved under the item's key, so an item back in the list - restored, or
    // undone - would come back still swiped away. Being in the list means it is not dismissed.
    LaunchedEffect(Unit) {
        if (dismissState.currentValue != DismissValue.Default) dismissState.snapTo(DismissValue.Default)
    }

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
                    contentDescription = stringResource(R.string.delete),
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