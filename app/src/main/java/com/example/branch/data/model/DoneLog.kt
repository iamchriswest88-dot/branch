package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "done_log")
data class DoneLog(
    @PrimaryKey val id: String,
    val category: String,  // "gym" or "flow"
    val dateKey: String    // "YYYY-MM-DD"
)
