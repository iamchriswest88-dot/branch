package com.example.branch.ui.builder

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.data.model.Exercise
import com.example.branch.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    category:  String,
    workoutId: String?,
    onBack:    () -> Unit,
    vm: BuilderViewModel = viewModel(
        key     = "builder_${category}_$workoutId",
        factory = BuilderViewModel.factory(category, workoutId)
    )
) {
    val exercises by vm.exercises.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NothingBg,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = NothingText)
                    }
                },
                title = {
                    Text(
                        (if (vm.isFlow) "FLOW" else "GYM") + if (workoutId == null) " BUILDER" else " EDIT",
                        style = MaterialTheme.typography.titleMedium,
                        color = NothingText
                    )
                },
                actions = {
                    if (workoutId != null) {
                        IconButton(onClick = { vm.deleteWorkout(onBack) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = NothingFaint)
                        }
                    }
                    IconButton(onClick = { vm.save(onBack) }) {
                        Icon(Icons.Default.Check, "Save", tint = NothingRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NothingSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value         = vm.workoutName,
                    onValueChange = vm::setName,
                    label         = { Text(if (vm.isFlow) "FLOW NAME" else "WORKOUT NAME", style = MaterialTheme.typography.labelSmall) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NothingRed,
                        unfocusedBorderColor = NothingLine,
                        focusedTextColor     = NothingText,
                        unfocusedTextColor   = NothingText,
                        cursorColor          = NothingRed,
                        focusedLabelColor    = NothingRed,
                        unfocusedLabelColor  = NothingMuted
                    )
                )
            }
            itemsIndexed(vm.steps, key = { _, s -> s.id }) { index, step ->
                StepCard(
                    step        = step,
                    isFlow      = vm.isFlow,
                    exercises   = exercises,
                    onUpdate    = { vm.updateStep(index, it) },
                    onDelete    = { vm.removeStep(index) },
                    onAddCustom = { name, area, cb -> vm.addCustomExercise(name, area, cb) }
                )
            }
            item {
                OutlinedButton(
                    onClick   = vm::addStep,
                    modifier  = Modifier.fillMaxWidth(),
                    colors    = ButtonDefaults.outlinedButtonColors(contentColor = NothingMuted)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ADD STEP", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun StepCard(
    step:        StepDraft,
    isFlow:      Boolean,
    exercises:   List<Exercise>,
    onUpdate:    (StepDraft) -> Unit,
    onDelete:    () -> Unit,
    onAddCustom: (String, String, (Exercise) -> Unit) -> Unit,
) {
    var showPicker   by remember { mutableStateOf(false) }
    var pickerSearch by remember { mutableStateOf("") }

    if (showPicker) {
        ExercisePickerDialog(
            exercises      = exercises,
            search         = pickerSearch,
            onSearchChange = { pickerSearch = it },
            onSelect       = { ex -> onUpdate(step.copy(exercise = ex)); showPicker = false; pickerSearch = "" },
            onDismiss      = { showPicker = false; pickerSearch = "" }
        )
    }

    Surface(
        color    = NothingSurface,
        shape    = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().border(1.dp, NothingLine, MaterialTheme.shapes.small)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showPicker = true }, modifier = Modifier.weight(1f)) {
                    Text(
                        step.exercise?.name?.uppercase() ?: "TAP TO SELECT EXERCISE",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (step.exercise != null) NothingText else NothingFaint
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Remove", tint = NothingFaint)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepCounter(
                    label   = if (isFlow) "ROUNDS" else "SETS",
                    value   = step.sets,
                    onMinus = { if (step.sets > 1) onUpdate(step.copy(sets = step.sets - 1)) },
                    onPlus  = { onUpdate(step.copy(sets = step.sets + 1)) },
                    modifier = Modifier.weight(1f)
                )
                StepCounter(
                    label   = if (isFlow) "HOLD S" else "WORK S",
                    value   = step.workSec,
                    onMinus = { if (step.workSec > 5) onUpdate(step.copy(workSec = step.workSec - 5)) },
                    onPlus  = { onUpdate(step.copy(workSec = step.workSec + 5)) },
                    modifier = Modifier.weight(1f)
                )
                StepCounter(
                    label   = "REST S",
                    value   = step.restSec,
                    onMinus = { if (step.restSec >= 5) onUpdate(step.copy(restSec = step.restSec - 5)) },
                    onPlus  = { onUpdate(step.copy(restSec = step.restSec + 5)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("BOTH SIDES", style = MaterialTheme.typography.labelSmall, color = NothingMuted)
                Switch(
                    checked         = step.sides,
                    onCheckedChange = { onUpdate(step.copy(sides = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = NothingRed, checkedTrackColor = NothingLeafDim)
                )
            }
            if (step.sides) {
                StepCounter(
                    label    = "SWAP S",
                    value    = step.swapSec,
                    onMinus  = { if (step.swapSec >= 5) onUpdate(step.copy(swapSec = step.swapSec - 5)) },
                    onPlus   = { onUpdate(step.copy(swapSec = step.swapSec + 5)) },
                    modifier = Modifier.fillMaxWidth(0.38f)
                )
            }
            Text(step.summary(isFlow), style = MaterialTheme.typography.bodySmall, color = NothingMuted)
        }
    }
}

@Composable
fun StepCounter(label: String, value: Int, onMinus: () -> Unit, onPlus: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NothingMuted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMinus, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, "-", tint = NothingMuted)
            }
            Text(
                "$value",
                style    = MaterialTheme.typography.titleSmall,
                color    = NothingText,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onPlus, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, "+", tint = NothingText)
            }
        }
    }
}

@Composable
fun ExercisePickerDialog(
    exercises:      List<Exercise>,
    search:         String,
    onSearchChange: (String) -> Unit,
    onSelect:       (Exercise) -> Unit,
    onDismiss:      () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = NothingSurface,
        title = {
            OutlinedTextField(
                value         = search,
                onValueChange = onSearchChange,
                label         = { Text("Search", style = MaterialTheme.typography.labelSmall) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NothingRed,
                    unfocusedBorderColor = NothingLine,
                    focusedTextColor     = NothingText,
                    unfocusedTextColor   = NothingText,
                    cursorColor          = NothingRed,
                    focusedLabelColor    = NothingRed,
                    unfocusedLabelColor  = NothingMuted
                )
            )
        },
        text = {
            val filtered = exercises.filter { it.name.contains(search, ignoreCase = true) }
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filtered) { ex ->
                    TextButton(onClick = { onSelect(ex) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ex.name, style = MaterialTheme.typography.bodyMedium, color = NothingText)
                            Text(ex.area, style = MaterialTheme.typography.labelSmall, color = NothingMuted)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = NothingMuted)
            }
        }
    )
}
