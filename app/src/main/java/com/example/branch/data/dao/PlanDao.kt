package com.example.branch.data.dao

import androidx.room.*
import com.example.branch.data.model.PlanDay
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_days ORDER BY dateKey ASC")
    fun getAll(): Flow<List<PlanDay>>

    @Query("SELECT * FROM plan_days ORDER BY dateKey ASC")
    suspend fun getAllSync(): List<PlanDay>

    @Query("SELECT * FROM plan_days WHERE dateKey = :dateKey")
    suspend fun getByDate(dateKey: String): PlanDay?

    @Query("SELECT * FROM plan_days WHERE dateKey = :dateKey")
    fun getByDateFlow(dateKey: String): Flow<PlanDay?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(planDay: PlanDay)

    @Query("DELETE FROM plan_days WHERE dateKey = :dateKey")
    suspend fun deleteByDate(dateKey: String)
}
