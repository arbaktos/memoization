package com.example.android.memoization.ui.composables.components

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.constraintlayout.compose.layoutId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android.memoization.R
import com.example.android.memoization.domain.model.MemoStack
import com.example.android.memoization.ui.features.stackscreen.StackViewModel

@OptIn(ExperimentalMotionApi::class)
@Composable
fun MotionAppBar(lazyScrollState: LazyListState, stackName: String, viewmodel: StackViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val motionScene = remember {
        context.resources.openRawResource(R.raw.motion_scene).readBytes().decodeToString()
    }

    val progress by animateFloatAsState(
        targetValue = if (lazyScrollState.firstVisibleItemIndex == 0) 0f else 1f,
        tween(500)
    )
    val motionHeight by animateDpAsState(
        targetValue = if (lazyScrollState.firstVisibleItemIndex == 0) 130.dp else 56.dp,
        tween(500)
    )

    val motionTextSize by animateIntAsState(
        targetValue = if (lazyScrollState.firstVisibleItemIndex == 0) 36 else 20,
        tween(500)
    )
//    var showEditStackDialog = remember { mutableStateOf(false) }

        MotionLayout(
            motionScene = MotionScene(content = motionScene),
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(motionHeight)
        ) {

            Row(
                modifier = Modifier.padding(top = 30.dp),
            ) {
                Text(
                    text = stackName,
                    fontSize = motionTextSize.sp,
                    fontWeight = if (progress == 1f) FontWeight.Light else FontWeight.Bold,
                    color = colors.onSecondary,
                    modifier = Modifier
                        .padding(start = 30.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = context.getString(R.string.edit_stack_desc),
                    modifier = Modifier
                        .clickable {
                            viewmodel.showEditStackDialog(true)
                        }
                        .padding(start = 16.dp)
                )
            }
        }
}