package com.example.branch.ui.plan

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.branch.BranchApplication
import com.example.branch.data.model.PlanDay
import com.example.branch.data.repository.DoneRepository
import com.example.branch.data.repository.PlanRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class PlanDayUiState(
    val dateKey:  String,
    val dayLabel: String,
    val hasGym:   Boolean,
    val hasFlow:  Boolean,
    val hasRest:  Boolean,
    val gymDone:  Boolean,
    val flowDone: Boolean,
    val isToday:  Boolean
)

class PlanViewModel(
    private val planRepo: PlanRepository,
    private val doneRepo: DoneRepository,
    private val calendarRepo: com.example.branch.data.repository.CalendarRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val fmt   = DateTimeFormatter.ISO_DATE

    private val _weekOffset = MutableStateFlow(0)
    
    val weekLabel: StateFlow<String> = _weekOffset.map { offset ->
        if (offset == 0) "This Week"
        else if (offset == -1) "Last Week"
        else if (offset == 1) "Next Week"
        else {
            val startOfWeek = today.plusWeeks(offset.toLong()).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val endOfWeek = startOfWeek.plusDays(6)
            "${startOfWeek.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${startOfWeek.dayOfMonth} - " +
            "${endOfWeek.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${endOfWeek.dayOfMonth}"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "This Week")

    val planDays: StateFlow<List<PlanDayUiState>> = combine(
        planRepo.getPlanDays(),
        doneRepo.getDoneDates("gym"),
        doneRepo.getDoneDates("flow"),
        _weekOffset
    ) { plan, gymDone, flowDone, offset ->
        val planMap     = plan.associateBy { it.dateKey }
        val gymDoneSet  = gymDone.toSet()
        val flowDoneSet = flowDone.toSet()
        
        val startOfWeek = today.plusWeeks(offset.toLong()).with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

        (0 until 7).map { dayOffset ->
            val date    = startOfWeek.plusDays(dayOffset.toLong())
            val dateKey = date.format(fmt)
            val day     = planMap[dateKey] ?: PlanDay(dateKey)
            
            val isToday = date == today
            val isTomorrow = date == today.plusDays(1)
            val isYesterday = date == today.minusDays(1)

            PlanDayUiState(
                dateKey  = dateKey,
                dayLabel = when {
                    isYesterday -> "YESTERDAY"
                    isToday     -> "TODAY"
                    isTomorrow  -> "TOMORROW"
                    else        -> date.dayOfWeek.name.take(3) + " " + date.dayOfMonth
                },
                hasGym   = day.hasGym,
                hasFlow  = day.hasFlow,
                hasRest  = day.hasRest,
                gymDone  = dateKey in gymDoneSet,
                flowDone = dateKey in flowDoneSet,
                isToday  = isToday
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun nextWeek() { _weekOffset.value += 1 }
    fun previousWeek() { _weekOffset.value -= 1 }
    fun resetWeek() { _weekOffset.value = 0 }

    private suspend fun currentDay(dateKey: String): PlanDay =
        planRepo.getPlanDays().first().find { it.dateKey == dateKey } ?: PlanDay(dateKey)

    fun toggleGym(dateKey: String, current: Boolean) = viewModelScope.launch {
        val pd = currentDay(dateKey).copy(hasGym = !current, hasRest = false)
        planRepo.upsertPlanDay(pd)
        try { calendarRepo.syncPlanDay(pd) } catch (e: Exception) {}
    }
    fun toggleFlow(dateKey: String, current: Boolean) = viewModelScope.launch {
        val pd = currentDay(dateKey).copy(hasFlow = !current, hasRest = false)
        planRepo.upsertPlanDay(pd)
        try { calendarRepo.syncPlanDay(pd) } catch (e: Exception) {}
    }
    fun toggleRest(dateKey: String, current: Boolean) = viewModelScope.launch {
        val pd = currentDay(dateKey).copy(hasRest = !current, hasGym = false, hasFlow = false)
        planRepo.upsertPlanDay(pd)
        try { calendarRepo.syncPlanDay(pd) } catch (e: Exception) {}
    }
    fun clearDay(dateKey: String) = viewModelScope.launch { 
        planRepo.deletePlanDay(dateKey) 
        try { calendarRepo.syncPlanDay(PlanDay(dateKey)) } catch (e: Exception) {}
    }
    fun toggleGymDone(dateKey: String, current: Boolean) = viewModelScope.launch {
        if (current) doneRepo.removeDone("gym", dateKey) else doneRepo.logDone("gym", dateKey)
    }
    fun toggleFlowDone(dateKey: String, current: Boolean) = viewModelScope.launch {
        if (current) doneRepo.removeDone("flow", dateKey) else doneRepo.logDone("flow", dateKey)
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BranchApplication
                val db  = app.database
                PlanViewModel(
                    planRepo = PlanRepository(db.planDao()),
                    doneRepo = DoneRepository(db.doneDao()),
                    calendarRepo = com.example.branch.data.repository.CalendarRepository(app)
                )
            }
        }
    }
}
