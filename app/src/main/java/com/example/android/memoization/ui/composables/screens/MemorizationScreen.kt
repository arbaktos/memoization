package com.example.android.memoization.ui.composables.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.model.WordPair
import com.example.android.memoization.ui.composables.FlipCard
import com.example.android.memoization.ui.composables.MemoIcon
import com.example.android.memoization.ui.composables.NoCardsCard
import com.example.android.memoization.ui.composables.components.StackCompleteDialog
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import com.example.android.memoization.ui.viewmodel.StackViewModel
import java.util.*

@Composable
fun MemorizationScreen(
    navController: NavController,
    viewModel: FolderViewModel,
    stackViewModel: StackViewModel
) {
    val wordListToLearn = viewModel.prepareStack().words.filter { it.toLearn }

    if (wordListToLearn.isNotEmpty()) {
        BodyContent(
            wordsToLearn = wordListToLearn,
            navController = navController,
            viewModel = viewModel,
            stackViewModel
        )
    } else {
        NoCardsCard(
            visible = true,
            onClick = { navController.popBackStack() })
    }
}

@Composable
fun BodyContent(
    wordsToLearn: List<WordPair>,
    navController: NavController,
    viewModel: FolderViewModel,
    stackViewModel: StackViewModel
) {
    var openFinishDialog by remember { mutableStateOf(false)}

    wordsToLearn.forEachIndexed { index, wordPair ->
        stackViewModel.updateCurrentWordPair(wordPair)


        if (openFinishDialog){
            StackCompleteDialog(viewModel = viewModel,
                navController = navController,
                onClick = { openFinishDialog = false}
            )
        }

        val onBottomButtonClick = {
            stackViewModel.updateCurrentWordPair(
                wordPair.copy(lastRep = Date())
            )
            stackViewModel.updateWordPairDateInDb()
            if(index == wordsToLearn.lastIndex) openFinishDialog = true
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

}


