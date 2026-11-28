package com.example.android.memoization.ui.composables.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.data.repository.LearningSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The library's Practice sides, which a stack without its own choice follows. */
@HiltViewModel
class StackDialogViewModel @Inject constructor(learningSettings: LearningSettingsRepository) : ViewModel() {
    val libraryPractice: StateFlow<PracticeSides> = learningSettings.practiceSides
        .stateIn(viewModelScope, SharingStarted.Eagerly, PracticeSides.DEFAULT)
}
