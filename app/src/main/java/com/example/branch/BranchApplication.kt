package com.example.branch

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.branch.data.BranchDatabase
import com.example.branch.worker.DailyStreakWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

class BranchApplication : Application() {

    val database by lazy { BranchDatabase.getDatabase(this) }
    val syncManager by lazy { com.example.branch.data.SyncManager(database) }

    override fun onCreate() {
        super.onCreate()
        scheduleDailyStreakWork()
        
        // Start Automatic Background Sync Polling
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            while (true) {
                try {
                    syncManager.syncToCloud()
                    syncManager.syncFromCloud()
                } catch (e: Exception) { e.printStackTrace() }
                kotlinx.coroutines.delay(5000) // Sync every 5 seconds
            }
        }
    }

    private fun scheduleDailyStreakWork() {
        val request = PeriodicWorkRequestBuilder<DailyStreakWorker>(
            1, TimeUnit.DAYS,
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "branch_daily_streak",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
