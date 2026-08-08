package com.example.android.memoization.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph

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

    NavHost(navController = navController, graph = navGraph)
}