package com.example.branch.ui.runner

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.branch.BranchApplication
import com.example.branch.data.model.WorkoutWithSteps
import com.example.branch.data.prefs.BranchPrefs
import com.example.branch.data.repository.DoneRepository
import com.example.branch.data.repository.PlanRepository
import com.example.branch.data.repository.WorkoutRepository
import com.example.branch.domain.StreakCalculator
import com.example.branch.glyph.GlyphAppController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class PhaseType { WORK, SWAP, REST }

data class Phase(
    val type:         PhaseType,
    val exerciseName: String,
    val side:         String,       // "LEFT", "RIGHT", or ""
    val durationSec:  Int,
    val setNumber:    Int,
    val totalSets:    Int
)

data class RunnerState(
    val exerciseName: String  = "",
    val phaseLabel:   String  = "",
    val sideLabel:    String  = "",
    val secondsLeft:  Int     = 0,
    val totalSeconds: Int     = 0,
    val currentSet:   Int     = 1,
    val totalSets:    Int     = 1,
    val isPaused:     Boolean = false,
    val isLoading:    Boolean = true,
    val isFinished:   Boolean = false,
    val gymStreak:    Int     = 0,
    val flowStreak:   Int     = 0,
    val category:     String  = ""
)

class RunnerViewModel(
    private val workoutId:   String,
    private val workoutRepo: WorkoutRepository,
    private val doneRepo:    DoneRepository,
    private val planRepo:    PlanRepository,
    private val prefs:       BranchPrefs,
    application:             Application
) : AndroidViewModel(application) {

    private val audio = AudioCueManager(application)
    private var phases: List<Phase> = emptyList()
    private var phaseIndex = 0
    private var timer: CountDownTimer? = null
    private var pausedSecondsLeft = 0
    private var workoutData: WorkoutWithSteps? = null
    private val fmt = DateTimeFormatter.ISO_DATE

    private val _state = MutableStateFlow(RunnerState())
    val state: StateFlow<RunnerState> = _state

    var audioMuted: Boolean
        get()  = audio.muted
        set(v) { audio.muted = v }

    init {
        viewModelScope.launch {
            val wws = workoutRepo.getWorkoutWithSteps(workoutId).first()
            workoutData = wws
            if (wws != null) {
                phases = buildQueue(wws)
                if (phases.isNotEmpty()) startPhase() else finishWorkout()
            } else {
                _state.update { it.copy(isLoading = false, isFinished = true) }
            }
        }
    }

    private fun buildQueue(wws: WorkoutWithSteps): List<Phase> {
        val result = mutableListOf<Phase>()
        for (step in wws.steps.sortedBy { it.sortOrder }) {
            for (set in 1..step.sets) {
                if (step.sides) {
                    result += Phase(PhaseType.WORK, step.exerciseName, "LEFT",  step.workSec, set, step.sets)
                    result += Phase(PhaseType.SWAP, step.exerciseName, "",      step.swapSec, set, step.sets)
                    result += Phase(PhaseType.WORK, step.exerciseName, "RIGHT", step.workSec, set, step.sets)
                } else {
                    result += Phase(PhaseType.WORK, step.exerciseName, "",      step.workSec, set, step.sets)
                }
                result += Phase(PhaseType.REST, step.exerciseName, "", step.restSec, set, step.sets)
            }
        }
        // Drop trailing REST(s)
        while (result.isNotEmpty() && result.last().type == PhaseType.REST) result.removeLast()
        return result
    }

    private fun startPhase() {
        val phase = phases[phaseIndex]
        _state.update {
            it.copy(
                isLoading    = false,
                exerciseName = phase.exerciseName,
                phaseLabel   = phase.type.name,
                sideLabel    = phase.side,
                secondsLeft  = phase.durationSec,
                totalSeconds = phase.durationSec,
                currentSet   = phase.setNumber,
                totalSets    = phase.totalSets,
                isPaused     = false
            )
        }
        if (phase.type == PhaseType.WORK) audio.playWorkStart()
        GlyphAppController.showCountdown(phase.durationSec, phase.durationSec)

        timer = object : CountDownTimer(phase.durationSec.toLong() * 1000L, 1000L) {
            override fun onTick(ms: Long) {
                val secs = ((ms + 500L) / 1000L).toInt()
                _state.update { it.copy(secondsLeft = secs) }
                GlyphAppController.showCountdown(secs, phase.durationSec)
                if (secs in 1..3 && phase.type != PhaseType.REST) audio.playTick()
            }
            override fun onFinish() {
                _state.update { it.copy(secondsLeft = 0) }
                when (phase.type) {
                    PhaseType.WORK -> audio.playWorkEnd()
                    PhaseType.REST -> audio.playRestEnd()
                    PhaseType.SWAP -> Unit
                }
                advance()
            }
        }.start()
    }

    private fun advance() {
        phaseIndex++
        if (phaseIndex >= phases.size) finishWorkout() else startPhase()
    }

    fun pause() {
        if (_state.value.isPaused) return
        pausedSecondsLeft = _state.value.secondsLeft
        timer?.cancel()
        _state.update { it.copy(isPaused = true) }
    }

    fun resume() {
        if (!_state.value.isPaused) return
        val phase = phases[phaseIndex]
        _state.update { it.copy(isPaused = false) }
        timer = object : CountDownTimer(pausedSecondsLeft.toLong() * 1000L, 1000L) {
            override fun onTick(ms: Long) {
                val secs = ((ms + 500L) / 1000L).toInt()
                _state.update { it.copy(secondsLeft = secs) }
                GlyphAppController.showCountdown(secs, phase.durationSec)
                if (secs in 1..3 && phase.type != PhaseType.REST) audio.playTick()
            }
            override fun onFinish() {
                _state.update { it.copy(secondsLeft = 0) }
                when (phase.type) {
                    PhaseType.WORK -> audio.playWorkEnd()
                    PhaseType.REST -> audio.playRestEnd()
                    PhaseType.SWAP -> Unit
                }
                advance()
            }
        }.start()
    }

    fun skip() { timer?.cancel(); advance() }

    private fun finishWorkout() {
        _state.update { it.copy(isLoading = false) }
        viewModelScope.launch {
            val today    = LocalDate.now().format(fmt)
            val category = workoutData?.workout?.category ?: return@launch
            doneRepo.logDone(category, today)

            val planDays  = planRepo.getPlanDaysSync()
            val gymDone   = doneRepo.getDoneDatesSync("gym")
            val flowDone  = doneRepo.getDoneDatesSync("flow")
            val gymStreak  = StreakCalculator.deriveStreak("gym",  planDays, gymDone,  today)
            val flowStreak = StreakCalculator.deriveStreak("flow", planDays, flowDone, today)

            prefs.setGymStreak(gymStreak)
            prefs.setFlowStreak(flowStreak)
            prefs.setLastGlyphCategory(category)
            GlyphAppController.showEmblem(category, gymStreak, flowStreak)

            _state.update {
                it.copy(isFinished = true, isLoading = false, gymStreak = gymStreak, flowStreak = flowStreak, category = category)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        audio.release()
        if (!_state.value.isFinished) {
            GlyphAppController.turnOff()
        }
    }

    companion object {
        fun factory(workoutId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BranchApplication
                val db  = app.database
                RunnerViewModel(
                    workoutId   = workoutId,
                    workoutRepo = WorkoutRepository(db.workoutDao(), db.stepDao()),
                    doneRepo    = DoneRepository(db.doneDao()),
                    planRepo    = PlanRepository(db.planDao()),
                    prefs       = BranchPrefs(app),
                    application = app
                )
            }
        }
    }
}
