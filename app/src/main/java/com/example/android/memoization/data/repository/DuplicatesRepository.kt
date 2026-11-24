package com.example.android.memoization.data.repository

import com.example.android.memoization.data.database.MemoDao
import com.example.android.memoization.data.database.dismisseddb.DismissedMatchEntity
import com.example.android.memoization.domain.duplicates.DismissedMatch
import com.example.android.memoization.domain.duplicates.MergePlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Merging duplicate pairs and remembering the merges the learner turned down. ADR 0009. */
class DuplicatesRepository @Inject constructor(private val memoDao: MemoDao) {

    val dismissedMatches: Flow<Set<DismissedMatch>> =
        memoDao.getDismissedMatches().map { rows -> rows.map { it.toDismissedMatch() }.toSet() }

    suspend fun dismiss(matches: Set<DismissedMatch>) {
        memoDao.insertDismissedMatches(matches.map(DismissedMatchEntity::from))
    }

    suspend fun apply(plans: List<MergePlan>) {
        memoDao.applyMerges(plans)
    }
}
