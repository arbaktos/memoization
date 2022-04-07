package com.example.android.memoization.ui.composables.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.memoization.R
import com.example.android.memoization.model.Stack
import com.example.android.memoization.ui.composables.FlipCard
import com.example.android.memoization.ui.composables.MemoIcon
import com.example.android.memoization.ui.composables.NoCardsCard
import com.example.android.memoization.ui.viewmodel.FolderViewModel
import java.util.*

@Composable
fun MemorizationScreen(
    navController: NavController,
    viewModel: FolderViewModel
) {
    BodyContent(viewModel.prepareStack(), navController)
}

@Composable
fun BodyContent(stack: Stack, navController: NavController) {
    val wordList = stack.words.filter { it.toShow }
    if (wordList.isEmpty()) {
        NoCardsCard(
            visible = true,
            onClick = { navController.popBackStack() })
    }
    else {
        val x = remember { mutableStateOf(0) }
        if (x.value == wordList.size) x.value = 0

        val wordPair = wordList[x.value]

        val onClick = {
            wordPair.lastRep = Date()
            x.value++
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
                        onClick()
                        wordPair.harderLevel()
                    }
                )
                MemoIcon(
                    contentDesc = "Hard",
                    tint = colorResource(R.color.yellow),
                    onClick = {
                        onClick()
                        wordPair.easierLevel()
                    }
                )
                MemoIcon(
                    contentDesc = "Wrong",
                    tint = colorResource(R.color.red),
                    onClick = {
                        onClick()
                        wordPair.toLevel1()
                    }
                )
            }
        }
    }

}


