package com.example.branch.ui.plan

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.theme.*

@Composable
fun PlanScreen(vm: PlanViewModel = viewModel(factory = PlanViewModel.factory())) {
    val days by vm.planDays.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val weekLabel by vm.weekLabel.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Plan", style = MaterialTheme.typography.displaySmall, color = NothingText)
                    if (weekLabel != "This Week") {
                        Spacer(Modifier.width(12.dp))
                        TextButton(
                            onClick = vm::resetWeek,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("TODAY", style = MaterialTheme.typography.labelSmall, color = NothingRed)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = vm::previousWeek) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = NothingMuted)
                    }
                    Text(weekLabel, style = MaterialTheme.typography.labelMedium, color = NothingMuted)
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
        color    = NothingSurface,
        shape    = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().border(1.dp, NothingLine, MaterialTheme.shapes.small)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = day.dayLabel,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = if (day.isToday) NothingRed else NothingText,
                    modifier = Modifier.weight(1f)
                )
                Text(day.dateKey, style = MaterialTheme.typography.labelSmall, color = NothingMuted)
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Clear", tint = NothingFaint)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanChip("Gym",  day.hasGym,  onToggleGym,  NothingRed)
                PlanChip("Flow", day.hasFlow, onToggleFlow, NothingLeaf)
                PlanChip("Rest", day.hasRest, onToggleRest, NothingMuted)
            }
            if (day.hasGym || day.hasFlow) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (day.hasGym) {
                        FilterChip(
                            selected    = day.gymDone,
                            onClick     = onToggleGymDone,
                            label       = { Text("Gym Done", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (day.gymDone) ({ Icon(Icons.Default.Check, null, tint = NothingRed, modifier = Modifier.size(16.dp)) }) else null,
                            colors      = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NothingSurface2,
                                selectedLabelColor     = NothingRed,
                                labelColor             = NothingFaint
                            )
                        )
                    }
                    if (day.hasFlow) {
                        FilterChip(
                            selected    = day.flowDone,
                            onClick     = onToggleFlowDone,
                            label       = { Text("Flow Done", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (day.flowDone) ({ Icon(Icons.Default.Check, null, tint = NothingLeaf, modifier = Modifier.size(16.dp)) }) else null,
                            colors      = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NothingLeafDim,
                                selectedLabelColor     = NothingLeaf,
                                labelColor             = NothingFaint
                            )
                        )
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
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor   = NothingSurface2,
            selectedLabelColor       = selectedColor,
            labelColor               = NothingFaint
        )
    )
}
