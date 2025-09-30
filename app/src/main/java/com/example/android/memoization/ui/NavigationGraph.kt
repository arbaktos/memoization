package com.example.android.memoization.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
internal data class StackScreen(val id: Long)

fun NavGraphBuilder.stackScreen() {
    composable<StackScreen> { navBackStackEntry ->
        StackScreen(stackId = navBackStackEntry.toRoute())

    }
}