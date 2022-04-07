package com.example.android.memoization.utils

const val IS_EDIT_MODE = "editMode"

sealed class NavScreens(val route: String) {
    object Folders: NavScreens("folders")
    object NewFolder: NavScreens("new folder")
    object Stack: NavScreens("stack")
    object NewPair: NavScreens("new_pair?editMode={editMode}") {
//        fun passEditMode(isEditMode: Boolean): String {
//            return "new pair?$IS_EDIT_MODE = {$isEditMode}"
//        }
    }
    object Memorization: NavScreens("memorization")
    object StartLearning: NavScreens("startLearning")
}