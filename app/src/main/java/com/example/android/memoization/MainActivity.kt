package com.example.android.memoization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.android.memoization.notifications.StartLearning
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.composables.components.NewFolderScreen
import com.example.android.memoization.ui.composables.screens.FoldersScreen
import com.example.android.memoization.ui.composables.screens.MemorizationScreen
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.ui.viewmodel.StackViewModel
import com.example.android.memoization.utils.IS_EDIT_MODE
import com.example.android.memoization.utils.NavScreens
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var folderViewModel: FolderViewModel
    @Inject lateinit var stackViewModel: StackViewModel

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folderViewModel.getFoldersWithStackFromDb()

        setContent {
            val navControllerObj = rememberNavController()
            NavHost(
                navController = navControllerObj,
                startDestination = NavScreens.Folders.route)
            {
                composable(NavScreens.Folders.route) {
                    FoldersScreen(
                        navController = navControllerObj,
                        folderViewModel = folderViewModel,
                        stackViewModel = stackViewModel
                    ) }

                composable(NavScreens.NewFolder.route) {
                    NewFolderScreen(
                        navController = navControllerObj) }

                composable(NavScreens.Stack.route) {
                    StackScreen(
                        navContoller = navControllerObj,
                        viewModel = stackViewModel) }

                composable(NavScreens.NewPair.route,
                arguments = listOf(navArgument(IS_EDIT_MODE) {
                    type = NavType.BoolType
                    defaultValue = false
                })) {
                    AddNewPairScreen(
                        navController = navControllerObj,
                        viewModel = stackViewModel,
                        editMode = it.arguments?.getBoolean(IS_EDIT_MODE) ?: false
                    ) }

                composable(NavScreens.Memorization.route) {
                    MemorizationScreen(
                        navController = navControllerObj,
                        viewModel = folderViewModel
                    )
                }

                composable(NavScreens.StartLearning.route) {
                    StartLearning(navController = navControllerObj)
                }
            }
        }
    }
}

