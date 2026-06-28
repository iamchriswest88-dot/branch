package com.example.branch

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.branch.data.BranchDatabase
import com.example.branch.worker.DailyStreakWorker
import java.util.concurrent.TimeUnit

class BranchApplication : Application() {

    val database by lazy { BranchDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        scheduleDailyStreakWork()
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
