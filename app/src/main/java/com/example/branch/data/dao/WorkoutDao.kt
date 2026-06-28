package com.example.branch.data.dao

import androidx.room.*
import com.example.branch.data.model.Workout
import com.example.branch.data.model.WorkoutWithSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts")
    suspend fun getAllSync(): List<Workout>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWithSteps(workoutId: String): Flow<WorkoutWithSteps?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: Workout)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: String)
}
