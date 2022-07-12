package com.example.android.memoization.ui.composables.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Text
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable

@Composable
fun SettingsDrawerScreen() {
//    varl (drawerState, onDrawerStateChange) = state { DrawerValue.Closed }
    @Composable
    fun ModalDrawerLayout(
        drawerState: DrawerState,
        onStateChange: (DrawerState) -> Unit,
        gesturesEnabled: Boolean = true,
        drawerContent: @Composable() () -> Unit = {
            Text(text = "Settings")
        },
        bodyContent: @Composable() () -> Unit
    ) {
    }
}