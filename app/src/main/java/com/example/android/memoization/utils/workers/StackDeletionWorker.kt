package com.example.android.memoization.utils.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.android.memoization.data.database.StackWithWords
import com.example.android.memoization.data.repository.StackRepository
import com.example.android.memoization.domain.model.Stack
import com.example.android.memoization.domain.usecases.DeleteStackUseCase
import com.example.android.memoization.domain.usecases.GetStackUseCase
import com.example.android.memoization.utils.STACK_ID
import com.example.android.memoization.utils.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject


@HiltWorker
class StackDeletionWorker @AssistedInject constructor(
    @Assisted context: Context,
    val deleteStackUseCase: DeleteStackUseCase,
    val getStackUseCase: GetStackUseCase,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val stackId = inputData.getLong(STACK_ID, 0)
            deleteStackUseCase(getStackUseCase(stackId))
            Result.success()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            Result.failure()
        }
    }
}

fun StackWithWords.toStack(): Stack {
    return Stack(
        name = this.stack.name,
        numRep = this.stack.numRep,
        stackId = this.stack.stackId,
        words = this.words.map { it.toWordPair() }.toMutableList(),
        hasWords = this.stack.hasWords
    )
}