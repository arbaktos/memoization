package com.example.android.memoization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.memoization.support.NavScreens
import com.example.android.memoization.ui.composables.*
import com.example.android.memoization.ui.viewmodel.MemoizationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var viewModel: MemoizationViewModel

    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getFoldersWithStackFromDb()

        setContent {
            val navControllerObj = rememberNavController()
            NavHost(
                navController = navControllerObj,
                startDestination = NavScreens.Folders.name)
            {
                composable(NavScreens.Folders.name) {
                    FoldersScreen(
                        navController = navControllerObj,
                        viewModel = viewModel
                    ) }
                composable(NavScreens.NewFolder.name) {
                    NewFolderScreen(
                        navController = navControllerObj,
                        onAddFolder = viewModel::addFolder) }
                composable(NavScreens.Stack.name) {
                    StackScreen(
                        navContoller = navControllerObj,
                        viewModel = viewModel) }
                composable(NavScreens.NewPair.name) {
                    AddNewPairScreen(
                        navController = navControllerObj,
                        viewModel = viewModel) }
                composable(NavScreens.Memorization.name) {
                    MemorizationScreen(
                        navController = navControllerObj,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

