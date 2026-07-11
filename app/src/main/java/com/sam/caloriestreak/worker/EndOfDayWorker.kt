package com.sam.caloriestreak.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class EndOfDayWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // TODO: Reconcile all unprocessed previous days through a domain use case.
        // The use case must be idempotent because WorkManager timing is not exact.
        return Result.success()
    }
}
