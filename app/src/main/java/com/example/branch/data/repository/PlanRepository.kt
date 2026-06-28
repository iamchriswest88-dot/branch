package com.example.branch.data.repository

import com.example.branch.data.dao.PlanDao
import com.example.branch.data.model.PlanDay
import kotlinx.coroutines.flow.Flow

class PlanRepository(private val dao: PlanDao) {
    fun getPlanDays(): Flow<List<PlanDay>> = dao.getAll()
    suspend fun getPlanDaysSync(): List<PlanDay> = dao.getAllSync()

    suspend fun upsertPlanDay(planDay: PlanDay) = dao.upsert(planDay)

    suspend fun deletePlanDay(dateKey: String) = dao.deleteByDate(dateKey)
}
