package com.example.branch.ui.builder

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.branch.BranchApplication
import com.example.branch.data.model.Exercise
import com.example.branch.data.model.Step
import com.example.branch.data.model.Workout
import com.example.branch.data.repository.ExerciseRepository
import com.example.branch.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class StepDraft(
    val id:       String   = UUID.randomUUID().toString(),
    val exercise: Exercise? = null,
    val sets:     Int      = 3,
    val workSec:  Int      = 30,
    val restSec:  Int      = 15,
    val sides:    Boolean  = false,
    val swapSec:  Int      = 5,
) {
    fun toStep(workoutId: String, order: Int): Step? {
        val ex = exercise ?: return null
        return Step(
            id           = id,
            workoutId    = workoutId,
            exerciseId   = ex.id,
            exerciseName = ex.name,
            sets         = sets,
            workSec      = workSec,
            restSec      = restSec,
            sides        = sides,
            swapSec      = swapSec,
            sortOrder    = order
        )
    }
    fun summary(isFlow: Boolean): String {
        val w = if (isFlow) "Hold" else "Timed"
        val s = if (isFlow) "rounds" else "sets"
        val side = if (sides) " · both sides" else ""
        return "$w ${workSec}s · $sets $s$side · ${restSec}s rest"
    }
}

class BuilderViewModel(
    val category: String,
    private val existingWorkoutId: String?,
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
) : ViewModel() {

    var workoutName by mutableStateOf("")
        private set
    val isFlow = category == "flow"

    private val _steps = mutableStateListOf<StepDraft>()
    val steps: List<StepDraft> = _steps

    val exercises: StateFlow<List<Exercise>> =
        exerciseRepo.getExercises(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        existingWorkoutId?.let { id ->
            viewModelScope.launch {
                workoutRepo.getWorkoutWithSteps(id).first()?.let { wws ->
                    workoutName = wws.workout.name
                    val exList  = exercises.first()
                    _steps.clear()
                    _steps.addAll(wws.steps.sortedBy { it.sortOrder }.map { s ->
                        StepDraft(
                            id       = s.id,
                            exercise = exList.find { it.id == s.exerciseId },
                            sets     = s.sets,
                            workSec  = s.workSec,
                            restSec  = s.restSec,
                            sides    = s.sides,
                            swapSec  = s.swapSec,
                        )
                    })
                }
            }
        }
    }

    fun setName(name: String) { workoutName = name }
    fun addStep() { _steps += StepDraft() }
    fun updateStep(index: Int, step: StepDraft) { if (index in _steps.indices) _steps[index] = step }
    fun removeStep(index: Int) { if (index in _steps.indices) _steps.removeAt(index) }

    fun save(onDone: () -> Unit) {
        if (workoutName.isBlank()) return
        viewModelScope.launch {
            val workoutId = existingWorkoutId ?: UUID.randomUUID().toString()
            val workout   = Workout(workoutId, workoutName.trim(), category)
            val steps     = _steps.mapIndexedNotNull { i, d -> d.toStep(workoutId, i) }
            workoutRepo.saveWorkout(workout, steps)
            onDone()
        }
    }

    fun deleteWorkout(onDone: () -> Unit) {
        val id = existingWorkoutId ?: return
        viewModelScope.launch { workoutRepo.deleteWorkout(id); onDone() }
    }

    fun addCustomExercise(name: String, area: String, onAdded: (Exercise) -> Unit) {
        viewModelScope.launch {
            val ex = Exercise(UUID.randomUUID().toString(), name.trim(), area, category, isCustom = true)
            exerciseRepo.addCustomExercise(ex)
            onAdded(ex)
        }
    }

    companion object {
        fun factory(category: String, workoutId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BranchApplication
                val db  = app.database
                BuilderViewModel(
                    category          = category,
                    existingWorkoutId = workoutId,
                    workoutRepo       = WorkoutRepository(db.workoutDao(), db.stepDao()),
                    exerciseRepo      = ExerciseRepository(db.exerciseDao()),
                )
            }
        }
    }
}
