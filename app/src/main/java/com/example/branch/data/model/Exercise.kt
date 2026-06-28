package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val area: String,
    val category: String,   // "gym" or "flow"
    @SerialName("is_custom") val isCustom: Boolean = false
)
