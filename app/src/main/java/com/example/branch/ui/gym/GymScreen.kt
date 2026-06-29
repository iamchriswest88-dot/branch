package com.example.branch.ui.gym

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

@Composable
fun GymScreen(
    onNewWorkout:  () -> Unit,
    onEditWorkout: (String) -> Unit,
    onRunWorkout:  (String) -> Unit,
    vm: GymViewModel = viewModel(factory = GymViewModel.factory())
) {
    val workouts      by vm.workouts.collectAsStateWithLifecycle()
    val gymStreak     by vm.gymStreak.collectAsStateWithLifecycle()
    val totalSessions by vm.totalGymSessions.collectAsStateWithLifecycle()
    val showOnGlyph   by vm.showOnGlyph.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.example.branch.BranchApplication
    val db = androidx.compose.runtime.remember { app.database }
    val todayKey = androidx.compose.runtime.remember { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE) }
    val planDay by db.planDao().getByDateFlow(todayKey).collectAsStateWithLifecycle(initialValue = null)
    val gymDone by db.doneDao().isDoneFlow("gym", todayKey).collectAsStateWithLifecycle(initialValue = false)
    val gymPlanned = planDay?.hasGym == true && !gymDone

    Scaffold(
        containerColor = NothingBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New Workout", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.Add, null) },
                onClick = onNewWorkout,
                containerColor = NothingRed,
                contentColor   = NothingText
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text("Branch", style = MaterialTheme.typography.displaySmall, color = NothingText)
                Text("Gym",    style = MaterialTheme.typography.titleLarge,   color = NothingMuted)
            }
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmblemView(filledSections = gymStreak, style = EmblemStyle.GYM, isPlannedToday = gymPlanned)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip("$gymStreak/6",   "SECTIONS")
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
                                checkedThumbColor = NothingRed,
                                checkedTrackColor = NothingLeafDim
                            )
                        )
                    }

                }
            }
            item {
                HorizontalDivider(color = NothingLine)
                Spacer(Modifier.height(8.dp))
                Text("Workouts", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
            }
            if (workouts.isEmpty()) {
                item {
                    Text(
                        text  = "No workouts yet. Tap New Workout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NothingFaint
                    )
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

@Composable
fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = NothingText)
        Text(label, style = MaterialTheme.typography.labelSmall,     color = NothingMuted)
    }
}

@Composable
fun WorkoutCard(
    name:     String,
    onRun:    () -> Unit,
    onEdit:   () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        color    = NothingSurface,
        shape    = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NothingLine, MaterialTheme.shapes.small)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = name.uppercase(),
                style    = MaterialTheme.typography.titleSmall,
                color    = NothingText,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRun) {
                Icon(Icons.Default.PlayArrow, "Run",    tint = NothingRed)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit,      "Edit",   tint = NothingMuted)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete,    "Delete", tint = NothingFaint)
            }
        }
    }
}
