package com.example.android.memoization.domain.usecases

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.stackdb.toMemoStack
import com.example.android.memoization.ui.features.settings.ConstantsSettings
import com.example.android.memoization.utils.Datastore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface NotifThresholdCalcUseCase {
    operator suspend fun invoke(): Flow<Pair<Boolean, Long>>
}

class NotifThresholdCalcUseCaseImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val memoDao: MemoDao,
    private val dataStore: DataStore<Preferences>
) : NotifThresholdCalcUseCase {
    override suspend fun invoke(): Flow<Pair<Boolean, Long>> {
        Log.d(TAG, "invoke: ")
        var notifThreshold = ConstantsSettings.MIN_PERCENT
        dataStore.data.map { prefs ->
            prefs[Datastore.NOTIF_THRESHOLD_PERCENT]?.let { notifThreshold = it }
        }
        Log.d(TAG, "invoke: get threshold $notifThreshold")
        var result: Pair<Boolean, Long>? = false to 0
        return memoDao.getStacksWithWords().map {
            for (stack in it) {
                val percent = stack.toMemoStack().getStackUnrepeatedPercent()
                Log.d(TAG, "invoke: $percent")
                if (percent >= notifThreshold) {
                    result = true to stack.stack.stackId
                }
            }
            result!!
        }
    }

    companion object {
        const val TAG = "NotifThresholdCalcUse"
    }

}