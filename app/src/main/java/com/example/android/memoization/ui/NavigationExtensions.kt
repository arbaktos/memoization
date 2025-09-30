package com.example.android.memoization.ui

import androidx.navigation.NavController

fun NavController.navigateToStackScreen(stackId: Long) {
    navigate(route = StackScreen(id = stackId))
}