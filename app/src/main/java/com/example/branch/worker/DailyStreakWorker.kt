package com.example.branch.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.branch.BranchApplication
import com.example.branch.data.prefs.BranchPrefs
import com.example.branch.data.repository.DoneRepository
import com.example.branch.data.repository.PlanRepository
import com.example.branch.domain.StreakCalculator
import com.example.branch.glyph.GlyphAppController
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyStreakWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db     = (applicationContext as BranchApplication).database
            val prefs  = BranchPrefs(applicationContext)
            val planRepo = PlanRepository(db.planDao())
            val doneRepo = DoneRepository(db.doneDao())

            val today     = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val planDays  = planRepo.getPlanDays().first()
            val gymDone   = doneRepo.getDoneDates("gym").first()
            val flowDone  = doneRepo.getDoneDates("flow").first()

            val gymStreak  = StreakCalculator.deriveStreak("gym",  planDays, gymDone,  today)
            val flowStreak = StreakCalculator.deriveStreak("flow", planDays, flowDone, today)

            prefs.setGymStreak(gymStreak)
            prefs.setFlowStreak(flowStreak)

            // Refresh the Glyph emblem
            val lastCat = prefs.lastGlyphCategory.first()
            GlyphAppController.showEmblem(lastCat, gymStreak, flowStreak)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
