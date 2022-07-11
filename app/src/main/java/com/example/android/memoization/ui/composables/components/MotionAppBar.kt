package com.example.android.memoization.ui.composables.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.constraintlayout.compose.layoutId
import com.example.android.memoization.R

@OptIn(ExperimentalMotionApi::class)
@Composable
fun MotionAppBar(lazyScrollState: LazyListState) {

    val context = LocalContext.current
    val motionScene = remember {
        context.resources.openRawResource(R.raw.motion_scene).readBytes().decodeToString()
    }

    val progress by animateFloatAsState(
        targetValue = if (lazyScrollState.firstVisibleItemIndex in 0..8) 1f else 0f,
        tween(500)
    )
    val motionHeight by animateDpAsState(
        targetValue = if (lazyScrollState.firstVisibleItemIndex in 0..8) 56.dp else 250.dp,
        tween(500)
    )

    // handle composables within motion layout just like xml, first is always on bottom

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        MotionLayout(
            motionScene = MotionScene(content = motionScene),
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .height(motionHeight)
        ) {

            val boxProperties = motionProperties(id = "box")
            val roundedShape = RoundedCornerShape(
                bottomStart = boxProperties.value.int("roundValue").dp,
                bottomEnd = boxProperties.value.int("roundValue").dp
            )

            Box(
                modifier = Modifier
                    .layoutId("box")
                    .clip(roundedShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color.Black),
                            endY = 250f
                        )
                    )
            )

            Text(
                text = "Astro",
                fontSize = 24.sp,
                fontWeight = if (progress == 1f) FontWeight.Light else FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.layoutId("user_name")
            )

            Image(
                painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .layoutId("user_image")
            )
        }
    }
}