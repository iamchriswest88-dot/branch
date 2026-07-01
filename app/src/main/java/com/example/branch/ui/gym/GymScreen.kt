package com.example.branch.ui.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    text = "Branch", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = NothingText
                )
                Text(
                    text = "GYM",    
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 4.sp),   
                    color = NothingMuted
                )
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    EmblemView(filledSections = gymStreak, style = EmblemStyle.GYM, isPlannedToday = gymPlanned)
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
                    StatChip("$gymStreak/6",   "SECTIONS")
                    StatChip("$totalSessions", "TOTAL")
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SHOW ON GLYPH", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
                    Switch(
                        checked = showOnGlyph,
                        onCheckedChange = { vm.toggleGlyph() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GymPurple,
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
                Text("WORKOUTS", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
            }
            if (workouts.isEmpty()) {
                item {
                    Text(
                        text  = "No workouts yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NothingFaint
                    )
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        name        = workout.name,
                        accentColor = GymPurple,
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
                        text = "+ NEW WORKOUT", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = GymPurple
                    )
                }
            }
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val split = value.split("/")
        if (split.size == 2) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(split[0], style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp), color = NothingText)
                Text("/${split[1]}", style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp), color = NothingFaint)
            }
        } else {
            Text(value, style = MaterialTheme.typography.labelLarge.copy(fontSize = 24.sp), color = NothingText)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = NothingMuted)
    }
}

@Composable
fun WorkoutCard(
    name:        String,
    accentColor: Color,
    onRun:       () -> Unit,
    onEdit:      () -> Unit,
    onDelete:    () -> Unit,
) {
    Surface(
        color    = Color.Transparent,
        shape    = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NothingLine, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = name,
                style    = MaterialTheme.typography.labelMedium,
                color    = NothingText,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = NothingFaint)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = NothingMuted)
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .clickable { onRun() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Run", tint = NothingBg, modifier = Modifier.size(16.dp))
            }
        }
    }
}
