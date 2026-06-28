package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey val id: String,
    val name: String,
    val category: String   // "gym" or "flow"
)
