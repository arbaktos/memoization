package com.example.android.memoization.ui.composables.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.memoization.R
import com.example.android.memoization.ui.features.language.LanguagesViewModel
import com.example.android.memoization.utils.LoadingState
import ru.vasilisasycheva.translation.api.LanguageItem

/**
 * One picker used for both the from and the to language. 117 languages is too many to
 * scroll, so it opens on a search field.
 */
@Composable
fun ChooseLanguageDialog(
    title: String,
    onLanguagePicked: (LanguageItem) -> Unit,
    onDismissRequest: () -> Unit,
    viewModel: LanguagesViewModel = hiltViewModel(),
) {
    val state by viewModel.languages.collectAsStateWithLifecycle()
    // Local to this dialog: the view model is shared by both pickers, so a query kept
    // there would still be in the box when the second one opens.
    var query by rememberSaveable { mutableStateOf("") }

    PlainDialog(onDismissRequest = onDismissRequest) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            label = { Text(stringResource(R.string.search_language)) },
            modifier = Modifier.fillMaxWidth()
        )

        when (val languages = state) {
            is LoadingState.Loading -> LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            is LoadingState.Error -> RetryMessage(onRetry = viewModel::load)

            is LoadingState.Collected -> {
                val visible = viewModel.filter(languages.content, query)
                if (visible.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_languages_found),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        items(visible, key = { it.full_code }) { language ->
                            LanguageRow(language) { onLanguagePicked(language) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(language: LanguageItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(text = language.englishName)
        if (!language.codeName.equals(language.englishName, ignoreCase = true)) {
            Text(
                text = language.codeName,
                fontSize = 13.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RetryMessage(onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Text(text = stringResource(R.string.languages_load_failed))
        Text(
            text = stringResource(R.string.retry),
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable(onClick = onRetry)
        )
    }
}
