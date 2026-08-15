package com.example.android.memoization.data.database.sidedb

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames
import com.example.android.memoization.data.model.Side
import com.example.android.memoization.data.model.SideState
import com.example.android.memoization.data.model.Shown

/** The stored schedule of one side; the words themselves live on the word pair. */
@Entity(tableName = TableNames.SIDE_TABLE, indices = [Index("wordPairId")])
data class SideEntity(
    @PrimaryKey(autoGenerate = true)
    val sideId: Long = 0,
    val wordPairId: Long,
    val shown: Int,
    val state: Int,
    val stability: Double? = null,
    val difficulty: Double? = null,
    val due: Long? = null,
    val lastReview: Long? = null,
    val reps: Int = 0,
    val lapses: Int = 0,
) {
    fun toSide(): Side = Side(
        sideId = sideId,
        wordPairId = wordPairId,
        shown = Shown.fromCode(shown),
        state = SideState.fromCode(state),
        stability = stability,
        difficulty = difficulty,
        due = due,
        lastReview = lastReview,
        reps = reps,
        lapses = lapses,
    )

    companion object {
        fun from(side: Side): SideEntity = SideEntity(
            sideId = side.sideId,
            wordPairId = side.wordPairId,
            shown = side.shown.code,
            state = side.state.code,
            stability = side.stability,
            difficulty = side.difficulty,
            due = side.due,
            lastReview = side.lastReview,
            reps = side.reps,
            lapses = side.lapses,
        )

        /** Both sides of a freshly added pair, neither of them practised yet. */
        fun newSidesFor(wordPairId: Long): List<SideEntity> = Shown.entries.map { shown ->
            SideEntity(
                wordPairId = wordPairId,
                shown = shown.code,
                state = SideState.New.code,
            )
        }
    }
}
