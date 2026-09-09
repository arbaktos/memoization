package com.example.android.memoization.data.database.logdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames
import com.example.android.memoization.domain.session.SessionEnding

/**
 * One sitting with a stack, as recorded for the statistics. [finishedAt] and [ending] are set
 * however the session ended - see [SessionEnding] - along with how far it got. Only a session
 * the process was killed under keeps them null; its answers still count.
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
    /**
     * Sides not got to when the session ended: 0 when it was answered to the end, what was left
     * after "Enough for today" or leaving. Written at the end; 0 while the session is on. (Up
     * to schema version 4 sessions had a size, and this was the due sides that did not fit;
     * the meaning - sides not got to this sitting - is the same.)
     */
    val sidesWaiting: Int = 0,
    /** Sides answered to a close by the end; written at the end, 0 while the session is on. */
    @ColumnInfo(defaultValue = "0")
    val sidesDone: Int = 0,
    /** [SessionEnding.code]; null while the session is on. */
    val ending: Int? = null,
)
