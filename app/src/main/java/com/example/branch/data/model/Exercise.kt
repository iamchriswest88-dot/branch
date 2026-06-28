package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val area: String,
    val category: String,   // "gym" or "flow"
    val isCustom: Boolean = false
)
