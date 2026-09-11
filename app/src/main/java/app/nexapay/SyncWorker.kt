package app.nexapay

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Future sync operations resolve only against the local simulator; no funds leave the device. */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.success()
}
