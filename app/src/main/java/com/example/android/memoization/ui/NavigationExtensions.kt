package com.example.android.memoization.ui

import androidx.navigation.NavController

fun NavController.navigateToStackScreen(stackId: Long) {
    navigate(route = StackDestination(id = stackId))
}

fun NavController.navigateToFolderScreen() {
    popBackStack(route = FolderDestination, inclusive = false)
}

fun NavController.navigateToNewPair(
    stackId: Long,
    fromLanguage: String?,
    toLanguage: String?,
) {
    navigate(route = NewPairDestination(stackId, fromLanguage, toLanguage))
}

/**
 * The pair a shared word opens; the picker behind it is dropped, so a back press hands the
 * learner straight back to the app the word came from.
 */
fun NavController.navigateFromShareToNewPair(
    stackId: Long,
    fromLanguage: String?,
    toLanguage: String?,
    word: String,
) {
    navigate(route = NewPairDestination(stackId, fromLanguage, toLanguage, word)) {
        popUpTo<SharedWordDestination> { inclusive = true }
    }
}

fun NavController.navigateToEditPair(
    wordPairId: Long,
    fromLanguage: String?,
    toLanguage: String?,
) {
    navigate(route = EditPairDestination(wordPairId, fromLanguage, toLanguage))
}

fun NavController.navigateToMemorization(stackId: Long) {
    navigate(route = MemorizationDestination(stackId))
}
