package com.example.android.memoization.data.database.wordpairdb

import androidx.room.Embedded
import androidx.room.Relation
import com.example.android.memoization.data.database.sidedb.SideEntity
import com.example.android.memoization.data.model.WordPair

data class WordPairWithSides(
    @Embedded val pair: WordPairEntity,
    @Relation(parentColumn = "wordPairId", entityColumn = "wordPairId")
    val sides: List<SideEntity>,
) {
    fun toWordPair(): WordPair =
        pair.toWordPair(sides.map { it.toSide() }.sortedBy { it.shown.code })
}
