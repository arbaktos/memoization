package com.example.android.memoization.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.memoization.model.WordPair

@Composable
fun FlipCard(wordPair: WordPair) {

    var rotated by remember { mutableStateOf(false) }
    val elevation = 8.dp
    val shape = RoundedCornerShape(15.dp)

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(500)
    )

    val animateFront by animateFloatAsState(
        targetValue = if (!rotated) 1f else 0f,
        animationSpec = tween(500)
    )

    val animateBack by animateFloatAsState(
        targetValue = if (rotated) 1f else 0f,
        animationSpec = tween(500)
    )

    Box(
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clickable {
                    rotated = !rotated
                },
            elevation = elevation,
        )
        {
            Column(
                Modifier
                    .fillMaxWidth(0.75f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = if (rotated) wordPair.word1 else wordPair.word2 ?: "",
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (rotated) animateBack else animateFront
                            rotationY = rotation
                        },
                    fontSize = 30.sp)
            }

        }
    }
}
