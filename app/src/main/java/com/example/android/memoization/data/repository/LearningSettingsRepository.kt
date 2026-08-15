package com.example.android.memoization.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.android.memoization.data.model.PracticeSides
import com.example.android.memoization.utils.DatastoreKey
import com.example.android.memoization.utils.getValue
import com.example.android.memoization.utils.putValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** The learner's choice of which sides to practise, kept next to the notification settings. */
class LearningSettingsRepository @Inject constructor(
    private val preferenceStorage: DataStore<Preferences>,
) {
    val practiceSides: Flow<PracticeSides> =
        preferenceStorage.getValue(DatastoreKey.PRACTICE_SIDES, PracticeSides.DEFAULT.name)
            .map { PracticeSides.fromName(it) }

    suspend fun setPracticeSides(practiceSides: PracticeSides) {
        preferenceStorage.putValue(DatastoreKey.PRACTICE_SIDES, practiceSides.name)
    }
}
