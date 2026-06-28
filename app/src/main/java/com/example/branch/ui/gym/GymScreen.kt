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
    android.util.Log.d("BranchApp", "GymScreen composed!")
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("HELLO WORLD FROM GYMSCREEN!", color = androidx.compose.ui.graphics.Color.Red)
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
