package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@Entity(tableName = "done_log")
data class DoneLog(
    @PrimaryKey val id: String,
    val category: String,  // "gym" or "flow"
    @SerialName("date_key") val dateKey: String    // "YYYY-MM-DD"
)
