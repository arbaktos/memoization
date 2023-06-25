package com.example.android.memoization.ui.features.memoizationscreen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.ui.composables.FlipCard
import com.example.android.memoization.ui.composables.components.MemoIcon
import com.example.android.memoization.ui.composables.components.StackCompleteDialog

@Composable
fun MemorizationScreen(
    navController: NavController,
    stackId: Long,
) {
    val viewModel: MemoizationViewModel = hiltViewModel()
    val wordListToLearn = viewModel.onStackIdReceived(stackId).collectAsState(initial = emptyList())

    FolderScreenBodyContent(
        wordsToLearn = wordListToLearn,
        navController = navController,
        viewModel = viewModel
    )
}

@Composable
fun FolderScreenBodyContent(
    wordsToLearn: State<List<WordPair>>,
    navController: NavController,
    viewModel: MemoizationViewModel
) {
    wordsToLearn.value.forEach { wordPair ->
        viewModel.clicked = false
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
            Row(modifier = Modifier.weight(0.3f)) {

                EasyIcon {
                    viewModel.onBottomButtonClick(
                        wordPair = wordPair,
                        icon = com.example.android.memoization.ui.features.memoizationscreen.Icon.Easy
                    )
                }
                HardIcon {
                    viewModel.onBottomButtonClick(
                        wordPair = wordPair,
                        icon = com.example.android.memoization.ui.features.memoizationscreen.Icon.Hard
                    )
                }
                WrongIcon {
                    viewModel.onBottomButtonClick(
                        wordPair = wordPair,
                        icon = com.example.android.memoization.ui.features.memoizationscreen.Icon.Wrong
                    )
                }
            }
        }
    }

    if (viewModel.clicked) {
        StackCompleteDialog(
            onClick = {
                navController.navigate(MemorizationFragmentDirections.toFolderScreenFragment())
            }
        )

    }

}


@Composable
fun EasyIcon(onClick: () -> Unit) {
    MemoIcon(
        contentDesc = stringResource(id = R.string.easy),
        tint = colorResource(R.color.teal_700),
        onClick = onClick
    )
}

@Composable
fun HardIcon(onClick: () -> Unit) {
    MemoIcon(
        contentDesc = stringResource(R.string.hard),
        tint = colorResource(R.color.yellow),
        onClick = onClick
    )
}

@Composable
fun WrongIcon(onClick: () -> Unit) {
    MemoIcon(
        contentDesc = stringResource(id = R.string.wrong),
        tint = colorResource(R.color.red),
        onClick = onClick
    )
}

enum class Icon {
    Easy, Hard, Wrong
}




