package com.example.android.memoization.ui.composables.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.domain.model.WordPair
import com.example.android.memoization.ui.composables.FlipCard
import com.example.android.memoization.ui.composables.MemoIcon
import com.example.android.memoization.ui.composables.components.StackCompleteDialog
import com.example.android.memoization.ui.features.folderscreen.FolderViewModel
import com.example.android.memoization.ui.features.stackscreen.StackViewModel
import com.example.android.memoization.utils.NavScreens
import java.util.*

@Composable
fun MemorizationScreen(
    navController: NavController,
    viewModel: FolderViewModel,
    stackViewModel: StackViewModel
) {
//    val appState = viewModel.publicAppState.collectAsState()
    val wordListToLearn = emptyList<WordPair>() //appState.value.currentStack?.prepareStack()?.words?.filter { it.toLearn } ?: emptyList()

    FolderScreenBodyContent(
        wordsToLearn = wordListToLearn,
        navController = navController,
        stackViewModel
    )
}

@Composable
fun FolderScreenBodyContent(
    wordsToLearn: List<WordPair>,
    navController: NavController,
    stackViewModel: StackViewModel
) {
    var openFinishDialog by remember { mutableStateOf(false) }
    var wordPairNum by remember { mutableStateOf(0) }
    val wordPair = wordsToLearn.get(wordPairNum)

    stackViewModel.updateCurrentWordPair(wordPair)

    if (openFinishDialog) {
        StackCompleteDialog(
            onClick = {
                openFinishDialog = false
                navController.navigate(NavScreens.Folders.route)
            }
        )
    }

    val onBottomButtonClick = {
        stackViewModel.updateCurrentWordPair(
            wordPair.copy(lastRep = Date())
        )
        stackViewModel.updateWordPairDateInDb()
        if (wordPairNum == wordsToLearn.lastIndex) openFinishDialog = true
        else wordPairNum++
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            FlipCard(wordPair)

        }
        Spacer(Modifier.height(100.dp))
        Row(
            modifier = Modifier
                .weight(0.3f)

        ) {
            MemoIcon(
                contentDesc = "Easy",
                tint = colorResource(R.color.teal_700),
                onClick = {
                    onBottomButtonClick()
                    wordPair.harderLevel()
                }
            )
            MemoIcon(
                contentDesc = "Hard",
                tint = colorResource(R.color.yellow),
                onClick = {
                    onBottomButtonClick()
                    wordPair.easierLevel()
                }
            )
            MemoIcon(
                contentDesc = "Wrong",
                tint = colorResource(R.color.red),
                onClick = {
                    onBottomButtonClick()
                    wordPair.toLevel1()
                }
            )
        }
    }
}




