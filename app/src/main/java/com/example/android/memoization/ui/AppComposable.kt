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
import com.example.android.memoization.utils.NO_STACK_ID

/**
 * [sharedWord] is a word another app shared into Memoization; the app then opens on the stack
 * picker for it instead of the library, and closes back into that app when the pair is saved.
 * [stackId] is the stack the way in already named - a share straight to one of the stacks the
 * share sheet offers, or a shortcut tapped in the launcher, which opens that stack.
 */
@Composable
fun AppComposable(
    preferenceStorage: DataStore<Preferences>,
    sharedWord: String? = null,
    stackId: Long? = null,
) {

    val navController = rememberNavController()

    val navGraph = remember(navController, sharedWord, stackId) {
        val start = when {
            sharedWord != null -> SharedWordDestination(sharedWord, stackId ?: NO_STACK_ID)
            stackId != null -> StackDestination(stackId)
            else -> FolderDestination
        }
        navController.createGraph(startDestination = start) {
            stackScreenDestination(navController)
            folderScreenDestination(navController, preferenceStorage)
            memorizationDestination(navController)
            newPairDestination(navController)
            sharedWordDestination(navController)
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
