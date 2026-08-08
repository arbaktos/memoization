package com.example.android.memoization.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.android.memoization.ui.theme.MemoizationTheme

@Composable
fun AppComposable(preferenceStorage: DataStore<Preferences>) {

    val navController = rememberNavController()

    val navGraph = remember(navController) {
        navController.createGraph(startDestination = FolderDestination) {
            stackScreenDestination(navController)
            folderScreenDestination(navController, preferenceStorage)
            memorizationDestination(navController)
            newPairDestination(navController)
        }
    }

    MemoizationTheme {
        // The activity draws edge to edge, so keep content clear of the status
        // bar, navigation bar, display cutout and keyboard.
        Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            NavHost(navController = navController, graph = navGraph)
        }
    }
}
