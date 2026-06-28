package com.example.branch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_days")
data class PlanDay(
    @PrimaryKey val dateKey: String,  // "YYYY-MM-DD"
    val hasGym: Boolean = false,
    val hasFlow: Boolean = false,
    val hasRest: Boolean = false
)
