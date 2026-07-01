package com.example.branch.ui.flow

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        
        containerColor = NothingBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 64.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Flow", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = NothingText
                )
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    EmblemView(filledSections = flowStreak, style = EmblemStyle.FLOW, isPlannedToday = flowPlanned)
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NothingLine, RoundedCornerShape(6.dp))
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip("$flowStreak/6",  "Sections")
                    StatChip("$totalSessions", "Total")
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show On Glyph", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
                    Switch(
                        checked = showOnGlyph,
                        onCheckedChange = { vm.toggleGlyph() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FlowBlue,
                            checkedTrackColor = NothingSurface2,
                            uncheckedThumbColor = NothingMuted,
                            uncheckedTrackColor = NothingSurface2,
                            uncheckedBorderColor = Color.Transparent,
                            checkedBorderColor = Color.Transparent
                        )
                    )
                }
                HorizontalDivider(color = NothingLine)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Flows", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
            }
            if (workouts.isEmpty()) {
                item {
                    Text(
                        text  = "No flows yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NothingFaint
                    )
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        name        = workout.name,
                        accentColor = FlowBlue,
                        onRun       = { onRunWorkout(workout.id) },
                        onEdit      = { onEditWorkout(workout.id) },
                        onDelete    = { vm.deleteWorkout(workout.id) }
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNewWorkout() }
                        .border(
                            width = 1.dp, 
                            color = NothingLine2, 
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ NEW FLOW", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = FlowBlue
                    )
                }
            }
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
