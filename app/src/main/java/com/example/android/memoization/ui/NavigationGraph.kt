package com.example.android.memoization.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.android.memoization.navigation.MemoDestination
import com.example.android.memoization.ui.features.addnewpair.AddNewPairScreen
import com.example.android.memoization.ui.features.folderscreen.FoldersScreen
import com.example.android.memoization.ui.features.memoizationscreen.MemorizationScreen
import com.example.android.memoization.ui.features.stackscreen.StackScreen
import kotlinx.serialization.Serializable

@Serializable
internal data class StackDestination(val id: Long) : MemoDestination

@Serializable
internal object FolderDestination : MemoDestination

/**
 * Adding a word pair and editing one are separate destinations so every argument
 * stays a primitive - that is what type-safe navigation understands out of the box.
 */
@Serializable
internal data class NewPairDestination(
    val stackId: Long,
    val fromLanguage: String? = null,
    val toLanguage: String? = null,
) : MemoDestination

@Serializable
internal data class EditPairDestination(
    val wordPairId: Long,
    val fromLanguage: String? = null,
    val toLanguage: String? = null,
) : MemoDestination

@Serializable
internal data class MemorizationDestination(val id: Long) : MemoDestination

fun NavGraphBuilder.stackScreenDestination(
    navController: NavController
) {
    composable<StackDestination> {
        StackScreen(navController = navController)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.folderScreenDestination(
    navController: NavController,
    preferenceStorage: DataStore<Preferences>
) {
    composable<FolderDestination> {
        FoldersScreen(navController = navController, preferenceStorage)
    }
}

fun NavGraphBuilder.memorizationDestination(
    navController: NavController
) {
    composable<MemorizationDestination> {
        MemorizationScreen(navController = navController)
    }
}

fun NavGraphBuilder.newPairDestination(
    navController: NavController
) {
    composable<NewPairDestination> {
        AddNewPairScreen(navController)
    }
    composable<EditPairDestination> {
        AddNewPairScreen(navController)
    }
}
