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

    Scaffold(
        modifier = Modifier.dotMatrixBackground(),
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Plan", 
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, letterSpacing = (-1.5).sp), 
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
            }
            items(days, key = { it.dateKey }) { day ->
                PlanDayCard(
                    day              = day,
                    onToggleGym      = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleGym(day.dateKey, day.hasGym) 
                    },
                    onToggleFlow     = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleFlow(day.dateKey, day.hasFlow) 
                    },
                    onToggleRest     = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleRest(day.dateKey, day.hasRest) 
                    },
                    onClear          = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.clearDay(day.dateKey) 
                    },
                    onToggleGymDone  = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleGymDone(day.dateKey, day.gymDone) 
                    },
                    onToggleFlowDone = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.toggleFlowDone(day.dateKey, day.flowDone) 
                    }
                )
            }
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PlanDayCard(
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
        shape    = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NothingLine, RoundedCornerShape(6.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = day.dayLabel.uppercase() + if (day.isToday) " · TODAY" else "",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = if (day.isToday) GymPurple else NothingText,
                    modifier = Modifier.weight(1f)
                )
                Text(day.dateKey, style = MaterialTheme.typography.labelSmall, color = NothingMuted)
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
        shape    = RoundedCornerShape(6.dp),
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
