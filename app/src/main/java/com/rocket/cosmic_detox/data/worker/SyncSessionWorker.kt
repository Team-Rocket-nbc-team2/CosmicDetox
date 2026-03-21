package com.rocket.cosmic_detox.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rocket.cosmic_detox.data.datasource.local.allowedapp.AllowedAppLocalDataSource
import com.rocket.cosmic_detox.data.datasource.local.model.AllowedAppSessionLocal
import com.rocket.cosmic_detox.domain.usecase.timer.UpdateLimitedTimeAppUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@HiltWorker
class SyncSessionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val allowedAppLocalDataSource: AllowedAppLocalDataSource,
    private val updateLimitedTimeAppUseCase: UpdateLimitedTimeAppUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) return Result.failure()

        val unsyncedSessions = allowedAppLocalDataSource.getUnsyncedSessions()
        if (unsyncedSessions.isEmpty()) return Result.success()

        var hasFailure = false

        for (session in unsyncedSessions) {
            val success = syncToFirestore(session)
            if (success) {
                allowedAppLocalDataSource.markAsSynced(session.packageId)
            } else {
                hasFailure = true
            }
        }

        return if (hasFailure) Result.retry() else Result.success()
    }

    private suspend fun syncToFirestore(session: AllowedAppSessionLocal): Boolean =
        suspendCancellableCoroutine { continuation ->
            updateLimitedTimeAppUseCase(
                packageId = session.packageId,
                remainTime = session.remainTime.toInt(),
                failCallback = {
                    if (continuation.isActive) continuation.resume(false)
                }
            )
            if (continuation.isActive) continuation.resume(true)
        }
}