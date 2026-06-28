package com.example.branch.ui.library

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.branch.BranchApplication
import com.example.branch.data.SeedData
import com.example.branch.data.model.Exercise
import com.example.branch.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class LibraryViewModel(
    private val exerciseRepo: ExerciseRepository
) : ViewModel() {

    val gymExercises: StateFlow<List<Exercise>> =
        exerciseRepo.getExercises("gym")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val flowExercises: StateFlow<List<Exercise>> =
        exerciseRepo.getExercises("flow")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allAreas: List<String> = SeedData.ALL_AREAS

    fun addCustomExercise(name: String, area: String, category: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            exerciseRepo.addCustomExercise(
                Exercise(
                    id       = UUID.randomUUID().toString(),
                    name     = name.trim(),
                    area     = area,
                    category = category,
                    isCustom = true
                )
            )
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch { exerciseRepo.deleteExercise(exercise) }
    }

    fun updateExercise(exercise: Exercise, newName: String, newArea: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            exerciseRepo.addCustomExercise(exercise.copy(name = newName.trim(), area = newArea))
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BranchApplication
                LibraryViewModel(ExerciseRepository(app.database.exerciseDao()))
            }
        }
    }
}
