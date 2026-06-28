package com.example.branch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.branch.theme.*
import com.example.branch.ui.builder.BuilderScreen
import com.example.branch.ui.flow.FlowScreen
import com.example.branch.ui.gym.GymScreen
import com.example.branch.ui.hub.HubScreen
import com.example.branch.ui.library.LibraryScreen
import com.example.branch.ui.plan.PlanScreen
import com.example.branch.ui.runner.RunnerScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(MainScaffold)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<MainScaffold> {
                BranchScaffold(
                    onNewWorkout  = { cat -> backStack.add(BuilderNav(cat)) },
                    onEditWorkout = { cat, id -> backStack.add(BuilderNav(cat, id)) },
                    onRunWorkout  = { id -> backStack.add(RunnerNav(id)) }
                )
            }
            entry<BuilderNav> { key ->
                BuilderScreen(
                    category  = key.category,
                    workoutId = key.workoutId.ifEmpty { null },
                    onBack    = { backStack.removeLastOrNull() }
                )
            }
            entry<RunnerNav> { key ->
                RunnerScreen(
                    workoutId = key.workoutId,
                    onBack    = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

private val TAB_LABELS = listOf("Gym", "Flow", "Hub", "Plan", "Library")
private val TAB_ICONS = listOf(
    R.drawable.ic_tab_gym,
    R.drawable.ic_tab_flow,
    R.drawable.ic_tab_plan, // Placeholder for HUB, ignored in code
    R.drawable.ic_tab_plan,
    R.drawable.ic_tab_library
)

@Composable
fun BranchScaffold(
    onNewWorkout:  (String) -> Unit,
    onEditWorkout: (String, String) -> Unit,
    onRunWorkout:  (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = NothingBg,
        bottomBar = {
            NavigationBar(
                containerColor = NothingSurface,
                tonalElevation = 0.dp,
            ) {
                TAB_LABELS.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected  = selectedTab == index,
                        onClick   = {
                            if (selectedTab != index) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedTab = index
                            }
                        },
                        icon = {
                            val iconVector = if (index == 2) {
                                Icons.Default.Dashboard
                            } else {
                                ImageVector.vectorResource(TAB_ICONS[index])
                            }
                            Icon(
                                imageVector = iconVector,
                                contentDescription = label
                            )
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = NothingRed,
                            selectedTextColor   = NothingRed,
                            unselectedIconColor = NothingMuted,
                            unselectedTextColor = NothingMuted,
                            indicatorColor      = NothingSurface2
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> GymScreen(
                    onNewWorkout  = { onNewWorkout("gym") },
                    onEditWorkout = { id -> onEditWorkout("gym", id) },
                    onRunWorkout  = onRunWorkout
                )
                1 -> FlowScreen(
                    onNewWorkout  = { onNewWorkout("flow") },
                    onEditWorkout = { id -> onEditWorkout("flow", id) },
                    onRunWorkout  = onRunWorkout
                )
                2 -> HubScreen()
                3 -> PlanScreen()
                4 -> LibraryScreen()
            }
        }
    }
}
