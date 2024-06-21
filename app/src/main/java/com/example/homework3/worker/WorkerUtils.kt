package com.example.homework3.worker

import android.content.Context
import androidx.work.*
import com.example.homework3.data.db.AppDatabase
import com.example.homework3.data.download.NewsDownloader
import com.example.homework3.data.repository.NewsRepository
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

// Utility class for enqueueing different types of background tasks related to news updates.
object WorkerUtils {

    // Enqueues an initial load task if the database is empty.
    fun enqueueInitialLoadTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val repository = NewsRepository(NewsDownloader(), AppDatabase.getDatabase(context).newsItemDao())
        val isDatabaseEmpty = runBlocking { repository.isDatabaseEmpty() }

        if (isDatabaseEmpty) {
            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf("TASK_TYPE" to "INITIAL_LOAD"))
                .setConstraints(getNetworkConstraints())
                .build()
            workManager.enqueue(request)
        }
    }

    // Enqueues a reload task to fetch news items from the server.
    fun enqueueReloadTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("TASK_TYPE" to "RELOAD"))
            .setConstraints(getNetworkConstraints())
            .build()
        workManager.enqueue(request)
    }

    // Enqueues a URL change task to update news items based on a new URL.
    fun enqueueUrlChangeTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("TASK_TYPE" to "URL_CHANGE"))
            .setConstraints(getNetworkConstraints())
            .build()
        workManager.enqueue(request)
    }

    // Enqueues a periodic update task to fetch news items at regular intervals.
    fun enqueuePeriodicUpdateTask(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val periodicRequest = PeriodicWorkRequestBuilder<DownloadWorker>(30, TimeUnit.MINUTES)
            .setInputData(workDataOf("TASK_TYPE" to "PERIODIC_UPDATE"))
            .setConstraints(getNetworkConstraints())
            .build()
        workManager.enqueueUniquePeriodicWork(
            "PERIODIC_UPDATE_WORKER",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    // Returns network constraints for the tasks.
    private fun getNetworkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    }
}
