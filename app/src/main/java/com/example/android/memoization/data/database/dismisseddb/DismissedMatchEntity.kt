package com.example.android.memoization.data.database.dismisseddb

import androidx.room.Entity
import com.example.android.memoization.data.database.TableNames
import com.example.android.memoization.domain.duplicates.DismissedMatch

/**
 * A merge the learner cancelled: these two pairs are not the same word, so they are never
 * proposed together again. Stored lower id first, so a match has exactly one row.
 */
@Entity(tableName = TableNames.DISMISSED_MATCH_TABLE, primaryKeys = ["lowPairId", "highPairId"])
data class DismissedMatchEntity(
    val lowPairId: Long,
    val highPairId: Long,
) {
    fun toDismissedMatch() = DismissedMatch(lowPairId, highPairId)

    companion object {
        fun from(match: DismissedMatch) = DismissedMatchEntity(match.lowId, match.highId)
    }
}
