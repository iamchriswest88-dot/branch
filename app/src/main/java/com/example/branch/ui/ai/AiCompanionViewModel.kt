package com.example.branch.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branch.data.AppDatabase
import com.example.branch.data.entities.Workout
import com.example.branch.data.entities.WorkoutExercise
import com.example.branch.data.entities.WorkoutSection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class AiCompanionViewModel(private val db: AppDatabase) : ViewModel() {

    fun generateWorkout(
        category: String,
        duration: Int,
        bodyParts: Set<String>,
        equipment: Set<String>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            // Mock AI logic: randomly pick 3-5 exercises that match the criteria
            val allExercises = db.exerciseDao().getExercisesByCategoryFlow(category).first()
            
            // Filter by equipment and body parts (mock filter)
            var filtered = allExercises.filter { ex ->
                val matchesBody = bodyParts.isEmpty() || bodyParts.any { bp -> ex.primaryMuscles.contains(bp, ignoreCase = true) }
                val matchesEq = equipment.isEmpty() || equipment.any { eq -> ex.equipment.contains(eq, ignoreCase = true) }
                matchesBody || matchesEq // loose matching for mock
            }
            if (filtered.isEmpty()) {
                filtered = allExercises // fallback to all
            }
            
            val selectedExercises = filtered.shuffled().take(minOf(5, filtered.size))
            
            // Construct Workout
            val workoutId = UUID.randomUUID().toString()
            val newWorkout = Workout(
                id = workoutId,
                name = "AI Generated ${if (category == "gym") "Strength" else "Flow"}",
                category = category
            )
            db.workoutDao().insertWorkout(newWorkout)

            // Construct 1 section
            val sectionId = UUID.randomUUID().toString()
            val section = WorkoutSection(
                id = sectionId,
                workoutId = workoutId,
                name = "Main Block",
                orderIndex = 0,
                repeats = if (category == "gym") 3 else 1
            )
            db.workoutSectionDao().insertSection(section)

            // Add exercises to section
            selectedExercises.forEachIndexed { index, ex ->
                val weId = UUID.randomUUID().toString()
                db.workoutExerciseDao().insertWorkoutExercise(
                    WorkoutExercise(
                        id = weId,
                        sectionId = sectionId,
                        exerciseId = ex.id,
                        orderIndex = index,
                        targetSets = if (category == "gym") 3 else 1,
                        targetReps = if (category == "gym") 10 else 0,
                        targetDuration = if (category == "flow") 60 else 0
                    )
                )
            }
            
            onSuccess(workoutId)
        }
    }
}
