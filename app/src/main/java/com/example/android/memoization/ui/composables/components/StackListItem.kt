package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.android.memoization.R
import com.example.android.memoization.data.model.MemoStack
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.ui.features.folderscreen.getPlayIconColor
import com.example.android.memoization.ui.theme.MemoButtonColors


@Composable
fun StackListItem(
    stack: MemoStack,
    modifier: Modifier = Modifier,
    onPin: () -> Unit = {},
    onAdd: () -> Unit = {},
    onPlay: () -> Unit = {},
    onClickRow: () -> Unit = {}
) {

    val wordsToLearn = stack.words.filter { (it as WordPair).toLearn }
    val unRepeatedPercent = remember {
        wordsToLearn.size.toFloat() / stack.words.size.toFloat() * 100
    }
    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(5.dp)
            .height(120.dp)
            .clickable { onClickRow() }

    ) {
        Column(modifier = Modifier.padding(5.dp)) {
            Row(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 8.dp)
                ) {
                    StackNameText(text = stack.name)
                    if (stack.words.isNotEmpty()) Text(
                        text = stringResource(R.string.wrods_to_learn) + "${wordsToLearn.size}/${stack.words.size}",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier
                    )
                }
                PinPushIcon(isPinned = stack.pinnedTime != null) { onPin() }
            }
            Row(modifier = Modifier.padding(start = 8.dp)) {
                AddIconBtn { onAdd() }
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.height(IntrinsicSize.Max)
        ) {
            if (wordsToLearn.isNotEmpty()) PlayIcon(tint = getPlayIconColor(unRepeatedPercent)) { onPlay() }
        }
    }
}

@Composable
fun StackNameText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontFamily = FontFamily.Serif,
        style = MaterialTheme.typography.h6,
        modifier = modifier
    )
}

@Composable
fun PinPushIcon(
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onPin: () -> Unit
) {
    val _isPinned = remember { mutableStateOf(isPinned) }
    Icon(
        Icons.Outlined.PushPin, stringResource(R.string.pin_stack),
        tint = if (_isPinned.value) MaterialTheme.colors.primary else Color.LightGray,
        modifier = modifier
            .size(30.dp)
            .padding(8.dp)
            .toggleable(value = _isPinned.value, onValueChange = {
                _isPinned.value = !_isPinned.value
                onPin()
            })
    )
}

@Composable
fun AddIconBtn(modifier: Modifier = Modifier, onAdd: () -> Unit) {

    OutlinedButton(
        onClick = onAdd,
        shape = RoundedCornerShape(16.dp),
        colors = MemoButtonColors(),
        modifier = modifier
    ) {
        Icon(Icons.Outlined.Add, stringResource(R.string.add_wordpair), tint = Color.Gray)
        Spacer(modifier = Modifier.width(5.dp))
        Text(stringResource(R.string.add), color = Color.Gray)

    }
}

@Composable
fun PlayIcon(
    tint: Color = MaterialTheme.colors.primary,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit
) {
    Icon(Icons.Outlined.PlayCircleFilled, stringResource(R.string.add_wordpair),
        tint = tint, modifier = modifier
            .size(60.dp)
//            .padding(8.dp)
            .clickable {
                onPlay()
            }
    )
}