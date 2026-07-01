package com.example.branch.ui.plan

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.theme.*

@Composable
fun PlanScreen(vm: PlanViewModel = viewModel(factory = PlanViewModel.factory())) {
    val days by vm.planDays.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val weekLabel by vm.weekLabel.collectAsStateWithLifecycle()

    var selectedDateKey by remember { mutableStateOf("") }

    // Update selectedDateKey when days changes, ensuring we have a valid selection.
    // If the currently selected date is not in the visible week, default to the first day of that week.
    LaunchedEffect(days) {
        if (days.isNotEmpty() && days.none { it.dateKey == selectedDateKey }) {
            selectedDateKey = days.firstOrNull { it.isToday }?.dateKey ?: days.first().dateKey
        }
    }

    val selectedDay = days.find { it.dateKey == selectedDateKey }

    Scaffold(
        containerColor = NothingBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 64.dp, bottom = 120.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = NothingText
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = vm::previousWeek) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = NothingMuted)
                    }
                    Text(
                        text = weekLabel.uppercase(), 
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 2.sp), 
                        color = NothingMuted
                    )
                    IconButton(onClick = vm::nextWeek) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = NothingMuted)
                    }
                }
            }

            // Horizontal Picker
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    val isSelected = day.dateKey == selectedDateKey
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NothingText else Color.Transparent,
                            contentColor = if (isSelected) NothingBg else NothingText,
                            onClick = { selectedDateKey = day.dateKey },
                            modifier = Modifier.fillMaxWidth().aspectRatio(0.7f)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.shortDayName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) NothingBg else NothingMuted
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = day.dayOfMonth,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) NothingBg else NothingText
                                )
                            }
                        }
                        // Indicator dot underneath if something is scheduled
                        Spacer(Modifier.height(8.dp))
                        if (day.hasGym || day.hasFlow) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .androidx.compose.foundation.background(
                                        color = if (day.hasGym) GymPurple else FlowBlue, 
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        } else {
                            Box(modifier = Modifier.size(4.dp)) // Placeholder
                        }
                    }
                }
            }

            // Selected Day Details
            if (selectedDay != null) {
                PlanDayDetails(
                    day = selectedDay,
                    onToggleGym      = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleGym(selectedDay.dateKey, selectedDay.hasGym) 
                    },
                    onToggleFlow     = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleFlow(selectedDay.dateKey, selectedDay.hasFlow) 
                    },
                    onToggleRest     = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleRest(selectedDay.dateKey, selectedDay.hasRest) 
                    },
                    onClear          = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.clearDay(selectedDay.dateKey) 
                    },
                    onToggleGymDone  = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleGymDone(selectedDay.dateKey, selectedDay.gymDone) 
                    },
                    onToggleFlowDone = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleFlowDone(selectedDay.dateKey, selectedDay.flowDone) 
                    }
                )
            }
        }
    }
}

@Composable
fun PlanDayDetails(
    day:              PlanDayUiState,
    onToggleGym:      () -> Unit,
    onToggleFlow:     () -> Unit,
    onToggleRest:     () -> Unit,
    onClear:          () -> Unit,
    onToggleGymDone:  () -> Unit,
    onToggleFlowDone: () -> Unit,
) {
    Surface(
        color    = Color.Transparent,
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .border(1.dp, NothingLine, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = day.dayLabel.uppercase() + if (day.isToday && day.dayLabel != "TODAY") " · TODAY" else "",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = if (day.isToday) GymPurple else NothingText,
                    modifier = Modifier.weight(1f)
                )
                Text(day.dateKey, style = MaterialTheme.typography.labelSmall, color = NothingMuted)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Clear", tint = NothingFaint)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanChip("GYM",  day.hasGym,  onToggleGym,  GymPurple)
                PlanChip("FLOW", day.hasFlow, onToggleFlow, FlowBlue)
                PlanChip("REST", day.hasRest, onToggleRest, NothingFaint)
            }
            if (day.hasGym || day.hasFlow) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (day.hasGym) {
                        PlanChip("GYM DONE", day.gymDone, onToggleGymDone, GymPurple)
                    }
                    if (day.hasFlow) {
                        PlanChip("FLOW DONE", day.flowDone, onToggleFlowDone, FlowBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanChip(label: String, selected: Boolean, onClick: () -> Unit, selectedColor: Color) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
        shape    = RoundedCornerShape(8.dp),
        border   = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = NothingLine,
            selectedBorderColor = selectedColor
        ),
        colors   = FilterChipDefaults.filterChipColors(
            containerColor           = Color.Transparent,
            selectedContainerColor   = Color.Transparent,
            labelColor               = NothingFaint,
            selectedLabelColor       = selectedColor
        )
    )
}
