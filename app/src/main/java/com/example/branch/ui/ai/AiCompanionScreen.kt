package com.example.branch.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.data.AppDatabase
import com.example.branch.theme.*
import com.example.branch.ui.components.BranchButton

@Composable
fun AiCompanionScreen(
    category: String,
    onBack: () -> Unit,
    onWorkoutGenerated: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { (context.applicationContext as com.example.branch.BranchApplication).database }
    val viewModel: AiCompanionViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AiCompanionViewModel(db) as T
        }
    })

    var duration by remember { mutableStateOf(30f) }
    val bodyParts = remember { mutableStateListOf<String>() }
    val equipment = remember { mutableStateListOf<String>() }

    val allBodyParts = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body")
    val allEquipment = listOf("Bodyweight", "Dumbbells", "Barbell", "Kettlebell", "Cables", "Machine")

    val accentColor = if (category == "gym") GymPurple else FlowBlue

    Scaffold(
        containerColor = NothingBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingText)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "AI Coach",
                        style = MaterialTheme.typography.displayMedium,
                        color = NothingText
                    )
                    Text(
                        "Create a custom ${if (category == "gym") "Strength" else "Flow"} routine",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Spacer(Modifier.height(24.dp))
                    
                    // Duration
                    Text("How long do you have?", style = MaterialTheme.typography.labelMedium, color = NothingText)
                    Spacer(Modifier.height(16.dp))
                    Text("${duration.toInt()} minutes", style = MaterialTheme.typography.displayLarge, color = accentColor)
                    Slider(
                        value = duration,
                        onValueChange = { duration = it },
                        valueRange = 5f..120f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = NothingSurface
                        )
                    )
                    
                    Spacer(Modifier.height(32.dp))
                }

                item {
                    // Body Parts
                    Text("What are we targeting?", style = MaterialTheme.typography.labelMedium, color = NothingText)
                    Spacer(Modifier.height(16.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allBodyParts.forEach { part ->
                            val isSelected = bodyParts.contains(part)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) accentColor else NothingSurface)
                                    .clickable {
                                        if (isSelected) bodyParts.remove(part) else bodyParts.add(part)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = part,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NothingBg else NothingText
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }

                item {
                    // Equipment
                    Text("What equipment do you have?", style = MaterialTheme.typography.labelMedium, color = NothingText)
                    Spacer(Modifier.height(16.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allEquipment.forEach { eq ->
                            val isSelected = equipment.contains(eq)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) accentColor else NothingSurface)
                                    .clickable {
                                        if (isSelected) equipment.remove(eq) else equipment.add(eq)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = eq,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NothingBg else NothingText
                                )
                            }
                        }
                    }
                }
            }

            // Generate Button
            BranchButton(
                text = "GENERATE WORKOUT",
                color = accentColor,
                onClick = {
                    viewModel.generateWorkout(
                        category = category,
                        duration = duration.toInt(),
                        bodyParts = bodyParts.toSet(),
                        equipment = equipment.toSet(),
                        onSuccess = onWorkoutGenerated
                    )
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
