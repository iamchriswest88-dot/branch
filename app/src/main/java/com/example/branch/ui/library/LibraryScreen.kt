package com.example.branch.ui.library

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.branch.data.model.Exercise
import com.example.branch.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel(factory = LibraryViewModel.factory())) {
    val gymExercises  by vm.gymExercises.collectAsStateWithLifecycle()
    val flowExercises by vm.flowExercises.collectAsStateWithLifecycle()
    
    var gymExpanded  by rememberSaveable { mutableStateOf(false) }
    var flowExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedArea by rememberSaveable { mutableStateOf("All") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.example.branch.BranchApplication
    val db = remember { app.database }

    val allFilterAreas = listOf("All") + vm.allAreas
    val filteredGym  = if (selectedArea == "All") gymExercises else gymExercises.filter { it.area == selectedArea }
    val filteredFlow = if (selectedArea == "All") flowExercises else flowExercises.filter { it.area == selectedArea }

    Scaffold(
        
        containerColor = NothingBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 64.dp, bottom = 24.dp)
        ) {
            item { 
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Library", 
                        style = MaterialTheme.typography.displayMedium, 
                        color = NothingText
                    ) 
                }
            }

            item {
                var filterExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = filterExpanded,
                    onExpandedChange = { filterExpanded = !filterExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter by Area", style = MaterialTheme.typography.labelSmall) },
                        textStyle = MaterialTheme.typography.bodySmall,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GymPurple,
                            unfocusedBorderColor = NothingLine,
                            focusedTextColor     = NothingText,
                            unfocusedTextColor   = NothingText,
                            focusedLabelColor    = GymPurple,
                            unfocusedLabelColor  = NothingMuted
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false },
                        containerColor = NothingSurface2
                    ) {
                        allFilterAreas.forEach { a ->
                            DropdownMenuItem(
                                text = { Text(a, color = NothingText, style = MaterialTheme.typography.bodySmall) },
                                onClick = { selectedArea = a; filterExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { gymExpanded = !gymExpanded }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GYM", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
                    Icon(if (gymExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Toggle Gym", tint = NothingMuted)
                }
                HorizontalDivider(color = NothingLine)
            }
            if (gymExpanded) {
                items(filteredGym, key = { "gym_${it.id}" }) { ex ->
                    ExerciseRow(
                        exercise = ex, 
                        allAreas = vm.allAreas, 
                        accentColor = GymPurple,
                        onUpdate = { n, a -> vm.updateExercise(ex, n, a) }, 
                        onDelete = { vm.deleteExercise(ex) }
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    AddExerciseRow(
                        areas = vm.allAreas, 
                        accentColor = GymPurple,
                        onAdd = { name, area -> vm.addCustomExercise(name, area, "gym") }
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { flowExpanded = !flowExpanded }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FLOW", style = MaterialTheme.typography.labelMedium, color = NothingMuted)
                    Icon(if (flowExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Toggle Flow", tint = NothingMuted)
                }
                HorizontalDivider(color = NothingLine)
            }
            if (flowExpanded) {
                items(filteredFlow, key = { "flow_${it.id}" }) { ex ->
                    ExerciseRow(
                        exercise = ex, 
                        allAreas = vm.allAreas, 
                        accentColor = FlowBlue,
                        onUpdate = { n, a -> vm.updateExercise(ex, n, a) }, 
                        onDelete = { vm.deleteExercise(ex) }
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    AddExerciseRow(
                        areas = vm.allAreas, 
                        accentColor = FlowBlue,
                        onAdd = { name, area -> vm.addCustomExercise(name, area, "flow") }
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
fun ExerciseRow(exercise: Exercise, allAreas: List<String>, accentColor: Color, onUpdate: (String, String) -> Unit, onDelete: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        EditExerciseRow(
            initialName = exercise.name,
            initialArea = exercise.area,
            areas = allAreas,
            accentColor = accentColor,
            onSave = { n, a -> onUpdate(n, a); isEditing = false },
            onCancel = { isEditing = false }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { isEditing = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exercise.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = NothingText, modifier = Modifier.weight(1f))
            Surface(color = NothingSurface2, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(exercise.area.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = NothingMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
            if (exercise.isCustom) {
                Surface(
                    color    = Color.Transparent,
                    shape    = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp).border(1.dp, accentColor, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text     = "CUSTOM",
                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color    = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = NothingFaint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseRow(areas: List<String>, accentColor: Color, onAdd: (String, String) -> Unit) {
    var name     by remember { mutableStateOf("") }
    var area     by remember { mutableStateOf(areas.first()) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color    = Color.Transparent,
        shape    = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, NothingLine, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("NAME", style = MaterialTheme.typography.labelSmall) },
                textStyle     = MaterialTheme.typography.labelSmall,
                singleLine    = true,
                shape         = RoundedCornerShape(4.dp),
                modifier      = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = accentColor,
                    unfocusedBorderColor = NothingLine,
                    focusedTextColor     = NothingText,
                    unfocusedTextColor   = NothingText,
                    cursorColor          = accentColor,
                    focusedLabelColor    = accentColor,
                    unfocusedLabelColor  = NothingMuted
                )
            )
            ExposedDropdownMenuBox(
                expanded          = expanded,
                onExpandedChange  = { expanded = !expanded },
                modifier          = Modifier.width(110.dp)
            ) {
                OutlinedTextField(
                    value         = area,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("AREA", style = MaterialTheme.typography.labelSmall) },
                    textStyle     = MaterialTheme.typography.labelSmall,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape         = RoundedCornerShape(4.dp),
                    modifier      = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = accentColor,
                        unfocusedBorderColor = NothingLine,
                        focusedTextColor     = NothingText,
                        unfocusedTextColor   = NothingText,
                        focusedLabelColor    = accentColor,
                        unfocusedLabelColor  = NothingMuted
                    )
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor   = NothingSurface2
                ) {
                    areas.forEach { a ->
                        DropdownMenuItem(
                            text    = { Text(a.uppercase(), color = NothingText, style = MaterialTheme.typography.labelSmall) },
                            onClick = { area = a; expanded = false }
                        )
                    }
                }
            }
            IconButton(onClick = { onAdd(name, area); name = "" }, enabled = name.isNotBlank()) {
                Icon(Icons.Default.Add, "Add", tint = if (name.isNotBlank()) accentColor else NothingFaint)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseRow(initialName: String, initialArea: String, areas: List<String>, accentColor: Color, onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var name     by remember { mutableStateOf(initialName) }
    var area     by remember { mutableStateOf(initialArea) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color    = Color.Transparent,
        shape    = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, accentColor, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("NAME", style = MaterialTheme.typography.labelSmall) },
                textStyle     = MaterialTheme.typography.labelSmall,
                singleLine    = true,
                shape         = RoundedCornerShape(4.dp),
                modifier      = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = accentColor,
                    unfocusedBorderColor = NothingLine,
                    focusedTextColor     = NothingText,
                    unfocusedTextColor   = NothingText,
                    cursorColor          = accentColor,
                    focusedLabelColor    = accentColor,
                    unfocusedLabelColor  = NothingMuted
                )
            )
            ExposedDropdownMenuBox(
                expanded          = expanded,
                onExpandedChange  = { expanded = !expanded },
                modifier          = Modifier.width(110.dp)
            ) {
                OutlinedTextField(
                    value         = area,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("AREA", style = MaterialTheme.typography.labelSmall) },
                    textStyle     = MaterialTheme.typography.labelSmall,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape         = RoundedCornerShape(4.dp),
                    modifier      = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = accentColor,
                        unfocusedBorderColor = NothingLine,
                        focusedTextColor     = NothingText,
                        unfocusedTextColor   = NothingText,
                        focusedLabelColor    = accentColor,
                        unfocusedLabelColor  = NothingMuted
                    )
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor   = NothingSurface2
                ) {
                    areas.forEach { a ->
                        DropdownMenuItem(
                            text    = { Text(a.uppercase(), color = NothingText, style = MaterialTheme.typography.labelSmall) },
                            onClick = { area = a; expanded = false }
                        )
                    }
                }
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Clear, "Cancel", tint = NothingMuted)
            }
            IconButton(onClick = { onSave(name, area) }, enabled = name.isNotBlank()) {
                Icon(Icons.Default.Check, "Save", tint = if (name.isNotBlank()) accentColor else NothingFaint)
            }
        }
    }
}
