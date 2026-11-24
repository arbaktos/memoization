package com.example.android.memoization.ui.features.stackscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.android.memoization.R
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.model.WordPair
import com.example.android.memoization.domain.duplicates.CopyGroup
import com.example.android.memoization.domain.duplicates.MergeProposal
import com.example.android.memoization.domain.duplicates.SideChoice
import com.example.android.memoization.ui.theme.indicatorColors
import kotlin.math.roundToInt

/** Above the list while the stack has duplicates left to resolve. */
@Composable
fun DuplicatesStrip(toReview: Int, onReview: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier.padding(top = 4.dp, bottom = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
        ) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.duplicates_to_review, toReview),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
            TextButton(onClick = onReview) {
                Text(stringResource(R.string.duplicates_review) + " ›")
            }
        }
    }
}

@Composable
fun RemoveCopiesDialog(
    groups: List<CopyGroup>,
    busy: Boolean,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duplicates_copies_title)) },
        text = {
            Column {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(groups, key = { it.keep.wordPairId }) { group ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = pairText(group.keep),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = stringResource(R.string.duplicates_copies_count, group.copies.size + 1),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
                Note(stringResource(R.string.duplicates_copies_note))
            }
        },
        confirmButton = {
            TextButton(onClick = onRemove, enabled = !busy) { Text(stringResource(R.string.duplicates_remove)) }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !busy) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun MergePairsDialog(
    proposal: MergeProposal,
    practice: PracticeSides,
    busy: Boolean,
    onMerge: (word1: String, word2: String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    val key = proposal.pairs.map { it.wordPairId }.toString()
    var wordPicked by rememberSaveable(key) { mutableIntStateOf(0) }
    var wordText by rememberSaveable(key) { mutableStateOf(draftOf(proposal.word)) }
    var meaningPicked by rememberSaveable(key) { mutableIntStateOf(0) }
    var meaningText by rememberSaveable(key) { mutableStateOf(draftOf(proposal.meaning)) }
    val word1 = valueOf(proposal.word, wordPicked, wordText)
    val word2 = valueOf(proposal.meaning, meaningPicked, meaningText)
    // Only a side the learner edits can be emptied; a side that had nothing stays as it was.
    val filled = word1.isNotBlank() && (proposal.meaning !is SideChoice.Edit || word2.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duplicates_merge_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                proposal.pairs.forEach { SourcePair(it, practice) }
                SideField(
                    label = stringResource(R.string.duplicates_word),
                    choice = proposal.word,
                    picked = wordPicked,
                    text = wordText,
                    onPick = { wordPicked = it },
                    onText = { wordText = it },
                )
                SideField(
                    label = stringResource(R.string.duplicates_meaning),
                    choice = proposal.meaning,
                    picked = meaningPicked,
                    text = meaningText,
                    onPick = { meaningPicked = it },
                    onText = { meaningText = it },
                )
                Note(stringResource(R.string.duplicates_merge_note))
            }
        },
        confirmButton = {
            TextButton(onClick = { onMerge(word1, word2) }, enabled = !busy && filled) {
                Text(stringResource(R.string.duplicates_merge))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !busy) { Text(stringResource(R.string.cancel)) }
        },
    )
}

private fun draftOf(choice: SideChoice): String = (choice as? SideChoice.Edit)?.draft.orEmpty()

private fun valueOf(choice: SideChoice, picked: Int, text: String): String = when (choice) {
    is SideChoice.Fixed -> choice.text
    is SideChoice.Spelling -> choice.options[picked.coerceIn(choice.options.indices)]
    is SideChoice.Edit -> text
}

@Composable
private fun SideField(
    label: String,
    choice: SideChoice,
    picked: Int,
    text: String,
    onPick: (Int) -> Unit,
    onText: (String) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
    when (choice) {
        is SideChoice.Fixed -> Text(choice.text, style = MaterialTheme.typography.bodyLarge)
        is SideChoice.Spelling -> Column {
            choice.options.forEachIndexed { index, option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = picked == index, onClick = { onPick(index) }, role = Role.RadioButton),
                ) {
                    RadioButton(selected = picked == index, onClick = null)
                    Text(option, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        is SideChoice.Edit -> OutlinedTextField(value = text, onValueChange = onText, modifier = Modifier.fillMaxWidth())
    }
}

/** One of the pairs being merged, with the level it is at now, as in the list. */
@Composable
private fun SourcePair(pair: WordPair, practice: PracticeSides) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = null,
            tint = indicatorColors(pair.level(practice)),
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = pairText(pair),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        val held = pair.sides.filterNot { it.isNew }.mapNotNull { it.stability }.minOrNull()
        Text(
            text = if (held == null) {
                stringResource(R.string.duplicates_new)
            } else {
                val days = held.roundToInt().coerceAtLeast(1)
                pluralStringResource(R.plurals.duplicates_days, days, days)
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 16.dp),
    )
}

private fun pairText(pair: WordPair): String =
    if (pair.word2.isNullOrBlank()) pair.word1 else "${pair.word1} — ${pair.word2}"
