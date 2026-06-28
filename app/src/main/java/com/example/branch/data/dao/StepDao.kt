package com.example.branch.data.dao

import androidx.room.*
import com.example.branch.data.model.Step

@Dao
interface StepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(steps: List<Step>)

    @Query("DELETE FROM steps WHERE workoutId = :workoutId")
    suspend fun deleteByWorkout(workoutId: String)

    @Delete
    suspend fun delete(step: Step)
}
