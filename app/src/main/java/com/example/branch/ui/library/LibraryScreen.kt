package com.example.branch.ui.library

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val scope = rememberCoroutineScope()
    val syncManager = remember { com.example.branch.data.SyncManager(db) }
    var isSyncing by remember { mutableStateOf(false) }

    val allFilterAreas = listOf("All") + vm.allAreas
    val filteredGym  = if (selectedArea == "All") gymExercises else gymExercises.filter { it.area == selectedArea }
    val filteredFlow = if (selectedArea == "All") flowExercises else flowExercises.filter { it.area == selectedArea }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Library", style = MaterialTheme.typography.displaySmall, color = NothingText) 
                TextButton(
                    onClick = { 
                        isSyncing = true
                        scope.launch {
                            val pushSuccess = syncManager.syncToCloud()
                            val pullSuccess = syncManager.syncFromCloud()
                            val msg = if (pushSuccess && pullSuccess) "Synced with Cloud!" else "Sync failed. Check connection."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            isSyncing = false
                        }
                    },
                    enabled = !isSyncing
                ) {
                    Text(if (isSyncing) "Syncing..." else "Sync Cloud", color = NothingRed, style = MaterialTheme.typography.labelMedium)
                }
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NothingRed,
                        unfocusedBorderColor = NothingLine,
                        focusedTextColor     = NothingText,
                        unfocusedTextColor   = NothingText,
                        focusedLabelColor    = NothingRed,
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
                Text("Gym", style = MaterialTheme.typography.titleMedium, color = NothingMuted)
                Icon(if (gymExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Toggle Gym", tint = NothingMuted)
            }
            HorizontalDivider(color = NothingLine)
        }
        if (gymExpanded) {
            items(filteredGym, key = { "gym_${it.id}" }) { ex ->
                ExerciseRow(ex, allAreas = vm.allAreas, onUpdate = { n, a -> vm.updateExercise(ex, n, a) }, onDelete = { vm.deleteExercise(ex) })
            }
            item {
                Spacer(Modifier.height(8.dp))
                AddExerciseRow(areas = vm.allAreas, onAdd = { name, area ->
                    vm.addCustomExercise(name, area, "gym")
                })
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable { flowExpanded = !flowExpanded }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Flow", style = MaterialTheme.typography.titleMedium, color = NothingMuted)
                Icon(if (flowExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Toggle Flow", tint = NothingMuted)
            }
            HorizontalDivider(color = NothingLine)
        }
        if (flowExpanded) {
            items(filteredFlow, key = { "flow_${it.id}" }) { ex ->
                ExerciseRow(ex, allAreas = vm.allAreas, onUpdate = { n, a -> vm.updateExercise(ex, n, a) }, onDelete = { vm.deleteExercise(ex) })
            }
            item {
                Spacer(Modifier.height(8.dp))
                AddExerciseRow(areas = vm.allAreas, onAdd = { name, area ->
                    vm.addCustomExercise(name, area, "flow")
                })
            }
        }
    }
}

@Composable
fun ExerciseRow(exercise: Exercise, allAreas: List<String>, onUpdate: (String, String) -> Unit, onDelete: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        EditExerciseRow(
            initialName = exercise.name,
            initialArea = exercise.area,
            areas = allAreas,
            onSave = { n, a -> onUpdate(n, a); isEditing = false },
            onCancel = { isEditing = false }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { isEditing = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exercise.name, style = MaterialTheme.typography.bodyMedium, color = NothingText, modifier = Modifier.weight(1f))
            Surface(color = NothingSurface2, shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(exercise.area, style = MaterialTheme.typography.labelSmall, color = NothingMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
            if (exercise.isCustom) {
                Surface(
                    color    = NothingLeafDim,
                    shape    = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text     = "Custom",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = NothingLeaf,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = NothingFaint)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseRow(areas: List<String>, onAdd: (String, String) -> Unit) {
    var name     by remember { mutableStateOf("") }
    var area     by remember { mutableStateOf(areas.first()) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color    = NothingSurface,
        shape    = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().border(1.dp, NothingLine, MaterialTheme.shapes.small)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Name", style = MaterialTheme.typography.labelSmall) },
                textStyle     = MaterialTheme.typography.bodySmall,
                singleLine    = true,
                modifier      = Modifier.weight(1f),
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
            ExposedDropdownMenuBox(
                expanded          = expanded,
                onExpandedChange  = { expanded = !expanded },
                modifier          = Modifier.width(110.dp)
            ) {
                OutlinedTextField(
                    value         = area,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Area", style = MaterialTheme.typography.labelSmall) },
                    textStyle     = MaterialTheme.typography.bodySmall,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier      = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NothingRed,
                        unfocusedBorderColor = NothingLine,
                        focusedTextColor     = NothingText,
                        unfocusedTextColor   = NothingText,
                        focusedLabelColor    = NothingRed,
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
                            text    = { Text(a, color = NothingText, style = MaterialTheme.typography.bodySmall) },
                            onClick = { area = a; expanded = false }
                        )
                    }
                }
            }
            IconButton(onClick = { onAdd(name, area); name = "" }, enabled = name.isNotBlank()) {
                Icon(Icons.Default.Add, "Add", tint = if (name.isNotBlank()) NothingRed else NothingFaint)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseRow(initialName: String, initialArea: String, areas: List<String>, onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var name     by remember { mutableStateOf(initialName) }
    var area     by remember { mutableStateOf(initialArea) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color    = NothingSurface,
        shape    = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().border(1.dp, NothingRed, MaterialTheme.shapes.small)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Name", style = MaterialTheme.typography.labelSmall) },
                textStyle     = MaterialTheme.typography.bodySmall,
                singleLine    = true,
                modifier      = Modifier.weight(1f),
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
            ExposedDropdownMenuBox(
                expanded          = expanded,
                onExpandedChange  = { expanded = !expanded },
                modifier          = Modifier.width(110.dp)
            ) {
                OutlinedTextField(
                    value         = area,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Area", style = MaterialTheme.typography.labelSmall) },
                    textStyle     = MaterialTheme.typography.bodySmall,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier      = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NothingRed,
                        unfocusedBorderColor = NothingLine,
                        focusedTextColor     = NothingText,
                        unfocusedTextColor   = NothingText,
                        focusedLabelColor    = NothingRed,
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
                            text    = { Text(a, color = NothingText, style = MaterialTheme.typography.bodySmall) },
                            onClick = { area = a; expanded = false }
                        )
                    }
                }
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Delete, "Cancel", tint = NothingMuted)
            }
            IconButton(onClick = { onSave(name, area) }, enabled = name.isNotBlank()) {
                Icon(Icons.Default.Add, "Save", tint = if (name.isNotBlank()) NothingRed else NothingFaint)
            }
        }
    }
}
