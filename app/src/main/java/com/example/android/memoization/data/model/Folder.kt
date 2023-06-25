package com.example.android.memoization.data.model

import com.example.android.memoization.data.database.FolderEntity

data class Folder(
    val name: String,
    var isOpen: Boolean = false,
    val stacks: MutableList<MemoStack> = mutableListOf(),
    var folderId: Long = 0
//    val language1: Language,
//    val language2: Language,
) {

    fun toFolderEntity(): FolderEntity {
        return FolderEntity(name = this.name, isOpen = this.isOpen, folderId = this.folderId)
    }
}

