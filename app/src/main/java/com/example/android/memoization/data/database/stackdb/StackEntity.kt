package com.example.android.memoization.data.database.stackdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.memoization.data.database.TableNames.STACK_TABLE
import com.example.android.memoization.data.model.BaseStack

@Entity(tableName = STACK_TABLE)
data class StackEntity(
    override val name: String,
    override var numRep: Int = 0, //to schedule check days
    @PrimaryKey(autoGenerate = true)
    override val stackId: Long = 0,
    override var hasWords: Boolean = false,
    override var isVisible: Boolean = true,
    override val fromLanguage: String? = null,
    override val toLanguage: String? = null,
    override var pinnedTime: Long? = null
) : BaseStack {

    companion object {
        fun create(stack: BaseStack): StackEntity {
            return StackEntity(
                name = stack.name,
                numRep = stack.numRep,
                stackId = stack.stackId,
                hasWords = stack.hasWords,
                pinnedTime = stack.pinnedTime,
                isVisible = stack.isVisible,
                fromLanguage = stack.fromLanguage,
                toLanguage = stack.toLanguage
            )
        }
    }

}