package com.example.fflowapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fflowapp.data.AppDatabase
import com.example.fflowapp.data.TaskRepository
import com.example.fflowapp.ui.FFlowScreen
import com.example.fflowapp.ui.FFlowViewModel
import com.example.fflowapp.ui.theme.FFlowTheme
import com.example.fflowapp.worker.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * Main entry point for FFlowApp.
 *
 * Sets up:
 *  - Room database instance (singleton)
 *  - FFlowViewModel backed by TaskDao
 *  - Jetpack Compose content with [FFlowTheme] wrapping [FFlowScreen]
 *  - WorkManager periodic sync placeholder
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise Room database and ViewModel
        val database = AppDatabase.getInstance(this)
        val repository = TaskRepository(database.taskDao())
        val viewModel = FFlowViewModel(repository)

        // Enqueue the periodic sync worker if not already scheduled
        schedulePeriodicSync()

        setContent {
            FFlowTheme {
                FFlowScreen(viewModel = viewModel)
            }
        }
    }

    /**
     * Schedules a [SyncWorker] to run periodically every 15 minutes.
     *
     * Uses [ExistingPeriodicWorkPolicy.KEEP] so that the schedule
     * survives process restarts without creating duplicate workers.
     */
    private fun schedulePeriodicSync() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "fflow_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
