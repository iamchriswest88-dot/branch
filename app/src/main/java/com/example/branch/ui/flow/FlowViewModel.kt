package com.example.branch.ui.flow

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.branch.BranchApplication
import com.example.branch.data.model.Workout
import com.example.branch.data.prefs.BranchPrefs
import com.example.branch.data.repository.*
import com.example.branch.domain.StreakCalculator
import com.example.branch.glyph.GlyphAppController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FlowViewModel(
    private val workoutRepo: WorkoutRepository,
    private val doneRepo: DoneRepository,
    private val planRepo: PlanRepository,
    private val prefs: BranchPrefs,
) : ViewModel() {

    private val fmt = DateTimeFormatter.ISO_DATE

    val workouts: StateFlow<List<Workout>> = workoutRepo.getWorkouts("flow")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val planDays = planRepo.getPlanDays()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val flowDone = doneRepo.getDoneDates("flow")
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val gymDone  = doneRepo.getDoneDates("gym")
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val flowStreak: StateFlow<Int> = combine(planDays, flowDone) { plan, done ->
        StreakCalculator.deriveStreak("flow", plan, done, LocalDate.now().format(fmt))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val gymStreak: StateFlow<Int> = combine(planDays, gymDone) { plan, done ->
        StreakCalculator.deriveStreak("gym", plan, done, LocalDate.now().format(fmt))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalFlowSessions: StateFlow<Int> = flowDone
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val showOnGlyph: StateFlow<Boolean> = prefs.lastGlyphCategory
        .map { it == "flow" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleGlyph() {
        viewModelScope.launch {
            val current = prefs.lastGlyphCategory.first()
            val next = if (current == "flow") "gym" else "flow"
            prefs.setLastGlyphCategory(next)
            GlyphAppController.showEmblem(next, gymStreak.value, flowStreak.value)
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch { workoutRepo.deleteWorkout(workoutId) }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BranchApplication
                val db  = app.database
                FlowViewModel(
                    workoutRepo = WorkoutRepository(db.workoutDao(), db.stepDao()),
                    doneRepo    = DoneRepository(db.doneDao()),
                    planRepo    = PlanRepository(db.planDao()),
                    prefs       = BranchPrefs(app),
                )
            }
        }
    }
}
