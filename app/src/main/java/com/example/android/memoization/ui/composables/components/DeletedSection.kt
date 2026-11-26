package com.example.android.memoization.ui.composables.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.folderscreen.StackListColors

/**
 * Whether each list's Deleted section is open, for as long as the app runs: one key for the
 * stack list, one per stack. Closed until opened (handoff "Удалённые", 12c).
 */
object DeletedSectionState {
    private val expanded = mutableStateMapOf<String, Boolean>()

    fun isExpanded(key: String): Boolean = expanded[key] ?: false

    fun toggle(key: String) {
        expanded[key] = !isExpanded(key)
    }
}

/**
 * The Deleted section at the end of a list (handoff "Удалённые", 12a/12b): a header that opens
 * and closes it, then one dashed row per deleted item, each with Restore. Nothing at all when
 * nothing is deleted. A tap on a row does nothing; only Restore brings it back.
 */
fun <T> LazyListScope.deletedSection(
    key: String,
    deleted: List<T>,
    itemKey: (T) -> Any,
    colors: StackListColors,
    row: @Composable LazyItemScope.(T) -> Unit,
) {
    if (deleted.isEmpty()) return
    item(key = "deleted-header") {
        DeletedHeader(
            count = deleted.size,
            expanded = DeletedSectionState.isExpanded(key),
            colors = colors,
            onToggle = { DeletedSectionState.toggle(key) },
        )
    }
    if (DeletedSectionState.isExpanded(key)) {
        items(deleted, key = { "deleted-${itemKey(it)}" }) { row(it) }
    }
}

@Composable
private fun DeletedHeader(count: Int, expanded: Boolean, colors: StackListColors, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onToggle)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.deleted_count, count),
            color = colors.deletedHeader,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.deletedChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** A deleted stack or word: no fill, a dashed outline, its name and a caption, and Restore. */
@Composable
fun DeletedRow(
    title: String,
    caption: String?,
    colors: StackListColors,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawRoundRect(
                    color = colors.deletedOutline,
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                    ),
                )
            }
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                color = colors.deletedName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!caption.isNullOrBlank()) {
                Text(caption, color = colors.deletedCaption, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        val shape = RoundedCornerShape(12.dp)
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(shape)
                .border(BorderStroke(1.5.dp, colors.restoreOutline), shape)
                .clickable(role = Role.Button, onClick = onRestore)
                .padding(start = 10.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Filled.Restore, null, tint = colors.restoreContent, modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(R.string.restore),
                color = colors.restoreContent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
