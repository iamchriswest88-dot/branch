package com.example.branch.data.repository

import com.example.branch.data.dao.StepDao
import com.example.branch.data.dao.WorkoutDao
import com.example.branch.data.model.Step
import com.example.branch.data.model.Workout
import com.example.branch.data.model.WorkoutWithSteps
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val stepDao: StepDao,
    private val syncManager: com.example.branch.data.SyncManager
) {
    fun getWorkouts(category: String): Flow<List<Workout>> = workoutDao.getByCategory(category)

    fun getWorkoutWithSteps(workoutId: String): Flow<WorkoutWithSteps?> =
        workoutDao.getWithSteps(workoutId)

    suspend fun saveWorkout(workout: Workout, steps: List<Step>) {
        workoutDao.upsert(workout)
        stepDao.deleteByWorkout(workout.id)
        stepDao.upsertAll(steps)
        try {
            syncManager.pushWorkout(workout)
            syncManager.pushSteps(steps)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteWorkout(workoutId: String) {
        workoutDao.deleteById(workoutId)
        // Note: Supabase deletion cascading requires a delete endpoint if needed, but not strictly asked for yet
    }
}
