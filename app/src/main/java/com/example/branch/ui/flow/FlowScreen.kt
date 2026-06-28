package com.example.branch.ui.flow

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.theme.*
import com.example.branch.ui.emblem.EmblemStyle
import com.example.branch.ui.emblem.EmblemView
import com.example.branch.ui.gym.StatChip
import com.example.branch.ui.gym.WorkoutCard

@Composable
fun FlowScreen(
    onNewWorkout:  () -> Unit,
    onEditWorkout: (String) -> Unit,
    onRunWorkout:  (String) -> Unit,
    vm: FlowViewModel = viewModel(factory = FlowViewModel.factory())
) {
    val workouts      by vm.workouts.collectAsStateWithLifecycle()
    val flowStreak    by vm.flowStreak.collectAsStateWithLifecycle()
    val totalSessions by vm.totalFlowSessions.collectAsStateWithLifecycle()
    val showOnGlyph   by vm.showOnGlyph.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.example.branch.BranchApplication
    val db = androidx.compose.runtime.remember { app.database }
    val todayKey = androidx.compose.runtime.remember { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE) }
    val planDay by db.planDao().getByDateFlow(todayKey).collectAsStateWithLifecycle(initialValue = null)
    val flowDone by db.doneDao().isDoneFlow("flow", todayKey).collectAsStateWithLifecycle(initialValue = false)
    val flowPlanned = planDay?.hasFlow == true && !flowDone

    Scaffold(
        containerColor = NothingBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text  = { Text("New Flow", style = MaterialTheme.typography.labelLarge) },
                icon  = { Icon(Icons.Default.Add, null) },
                onClick = onNewWorkout,
                containerColor = NothingLeaf,
                contentColor   = NothingBg
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text("Branch", style = MaterialTheme.typography.displaySmall, color = NothingText)
                Text("Flow",   style = MaterialTheme.typography.titleLarge,   color = NothingMuted)
            }
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmblemView(filledSections = flowStreak, style = EmblemStyle.FLOW, isPlannedToday = flowPlanned)
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatChip("$flowStreak/6",  "SECTIONS")
                    StatChip("$totalSessions", "TOTAL")
                }
            }
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Show On Glyph", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
                        Switch(
                            checked = showOnGlyph,
                            onCheckedChange = { vm.toggleGlyph() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NothingLeaf,
                                checkedTrackColor = NothingLeafDim
                            )
                        )
                    }

                }
            }
            item {
                HorizontalDivider(color = NothingLine)
                Spacer(Modifier.height(8.dp))
                Text("Flows", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
            }
            if (workouts.isEmpty()) {
                item {
                    Text("No flows yet. Tap New Flow.", style = MaterialTheme.typography.bodySmall, color = NothingFaint)
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        name     = workout.name,
                        onRun    = { onRunWorkout(workout.id) },
                        onEdit   = { onEditWorkout(workout.id) },
                        onDelete = { vm.deleteWorkout(workout.id) }
                    )
                }
            }
        }
    }
}
