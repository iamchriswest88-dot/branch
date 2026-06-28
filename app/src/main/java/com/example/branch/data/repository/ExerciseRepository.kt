package com.example.branch.data.repository

import com.example.branch.data.SeedData
import com.example.branch.data.dao.ExerciseDao
import com.example.branch.data.model.Exercise
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val dao: ExerciseDao,
    private val syncManager: com.example.branch.data.SyncManager
) {

    fun getExercises(category: String): Flow<List<Exercise>> = dao.getByCategory(category)

    fun getAllExercises(): Flow<List<Exercise>> = dao.getAll()

    /** Merge built-in exercises on every launch (idempotent — INSERT OR IGNORE). */
    suspend fun seedBuiltIns() {
        dao.insertIfAbsent(SeedData.GYM_EXERCISES + SeedData.FLOW_EXERCISES)
    }

    suspend fun addCustomExercise(exercise: Exercise) {
        dao.upsert(exercise.copy(isCustom = true))
        try {
            syncManager.pushExercise(exercise.copy(isCustom = true))
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteExercise(exercise: Exercise) = dao.delete(exercise)
}
