package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = Workout::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workoutId")]
)
data class Step(
    @PrimaryKey val id: String,
    @SerialName("workout_id") val workoutId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("exercise_name") val exerciseName: String,  // denormalised: avoids JOIN in Runner
    val sets: Int = 3,
    @SerialName("work_sec") val workSec: Int = 30,
    @SerialName("rest_sec") val restSec: Int = 15,
    val sides: Boolean = false,
    @SerialName("swap_sec") val swapSec: Int = 5,
    @SerialName("sort_order") val sortOrder: Int = 0
)
