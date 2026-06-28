package com.example.branch

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object MainScaffold : NavKey

@Serializable data class BuilderNav(
    val category: String,       // "gym" or "flow"
    val workoutId: String = ""  // empty = new workout
) : NavKey

@Serializable data class RunnerNav(val workoutId: String) : NavKey
