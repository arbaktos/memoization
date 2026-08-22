package com.example.android.memoization.data.database.logdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames

/**
 * One sitting with a stack, as recorded for the statistics. [finishedAt] is set when the queue
 * drained; a session the learner walked out of keeps null, and its answers still count.
 */
@Entity(tableName = TableNames.SESSION_TABLE)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val stackId: Long,
    val startedAt: Long,
    val finishedAt: Long? = null,
    /** Sides the session was given on entry. */
    val sidesOffered: Int,
    /** Due sides of the stack that did not fit this session. */
    val sidesWaiting: Int,
)
