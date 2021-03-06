package com.example.android.memoization.model

import androidx.compose.runtime.mutableStateListOf
import com.example.android.memoization.database.FolderEntity
import com.example.android.memoization.database.WordPairEntity
import java.util.*

data class Folder(
    val name: String,
    var isOpen: Boolean = false,
    val stacks: MutableList<Stack> = mutableListOf(),
    var folderId: Long = 0
//    val language1: Language,
//    val language2: Language,
) {

    fun toFolderEntity(): FolderEntity {
        return FolderEntity(name = this.name, isOpen = this.isOpen, folderId = this.folderId)
    }
}

