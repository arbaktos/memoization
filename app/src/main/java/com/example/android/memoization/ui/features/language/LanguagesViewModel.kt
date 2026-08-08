package com.example.android.memoization.ui.features.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.utils.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.vasilisasycheva.translation.api.LanguageItem
import ru.vasilisasycheva.translation.data.TranslationState
import ru.vasilisasycheva.translation.domain.TranslationRepo
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val translationRepo: TranslationRepo
) : ViewModel() {

    private val _languages =
        MutableStateFlow<LoadingState<List<LanguageItem>>>(LoadingState.Loading)
    val languages: StateFlow<LoadingState<List<LanguageItem>>> = _languages.asStateFlow()

    init {
        load()
    }

    fun load() {
        _languages.value = LoadingState.Loading
        viewModelScope.launch {
            _languages.value = when (val state = translationRepo.getLanguages()) {
                is TranslationState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    LoadingState.Collected(state.content as List<LanguageItem>)
                }

                is TranslationState.Error -> LoadingState.Error
                is TranslationState.Loading -> LoadingState.Loading
            }
        }
    }

    /** Matches on what the learner is likely to type: the name, or the code like "es_ES". */
    fun filter(all: List<LanguageItem>, query: String): List<LanguageItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return all
        return all.filter {
            it.englishName.contains(trimmed, ignoreCase = true) ||
                    it.codeName.contains(trimmed, ignoreCase = true) ||
                    it.full_code.contains(trimmed, ignoreCase = true)
        }
    }
}
