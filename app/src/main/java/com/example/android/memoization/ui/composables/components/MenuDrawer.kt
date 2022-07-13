package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.DrawerState
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch

@Composable
fun MenuDrawer(state: DrawerState) {
//    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl ) {
//        ModalDrawer(
//            drawerState = state,
//            drawerContent = { Text("Hello y'all drawer content") },
//            content = { Text("Hello y'all content") }
//        )
//    }
    val scope = rememberCoroutineScope()
    ModalDrawer(
        drawerState = state,
        drawerContent = {
            Column {
                Text("Text in Drawer")
                Button(onClick = {
                    scope.launch {
                        state.close()
                    }
                }) {
                    Text("Close Drawer")
                }
            }
        },
        content = {
            Column {
                Text("Text in Bodycontext")
                Button(onClick = {

                    scope.launch {
                        state.open()
                    }

                }) {
                    Text("Click to open")
                }
            }
        }
    )
}