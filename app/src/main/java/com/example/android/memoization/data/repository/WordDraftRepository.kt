package com.example.android.memoization.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** A word being added, left unsaved when the screen was closed, waiting for the next visit. */
data class WordDraft(val word: String, val meaning: String)

/**
 * One draft per stack, next to the settings: closing the add screen with something typed keeps
 * it here rather than in the database, where it would count as a word to learn.
 */
class WordDraftRepository @Inject constructor(
    private val preferenceStorage: DataStore<Preferences>,
) {
    suspend fun draft(stackId: Long): WordDraft? {
        val preferences = preferenceStorage.data.first()
        val word = preferences[wordKey(stackId)].orEmpty()
        val meaning = preferences[meaningKey(stackId)].orEmpty()
        return if (word.isBlank() && meaning.isBlank()) null else WordDraft(word, meaning)
    }

    /** A draft with nothing in it is no draft: it clears what was kept. */
    suspend fun keep(stackId: Long, draft: WordDraft) {
        if (draft.word.isBlank() && draft.meaning.isBlank()) return clear(stackId)
        preferenceStorage.edit {
            it[wordKey(stackId)] = draft.word
            it[meaningKey(stackId)] = draft.meaning
        }
    }

    suspend fun clear(stackId: Long) {
        preferenceStorage.edit {
            it.remove(wordKey(stackId))
            it.remove(meaningKey(stackId))
        }
    }

    private fun wordKey(stackId: Long) = stringPreferencesKey("draft_word_$stackId")
    private fun meaningKey(stackId: Long) = stringPreferencesKey("draft_meaning_$stackId")
}
