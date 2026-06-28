package com.example.branch.domain

import com.example.branch.data.model.PlanDay

object StreakCalculator {

    /**
     * Derives the filled-section count (0–6) for one discipline.
     *
     * Algorithm (from spec §6.5):
     * - Walk planned dates in chronological order, stop at future dates.
     * - Today unmet = pending; no penalty.
     * - Past done = +1 (clamped to max).
     * - Past missed = −1 (floor 0).
     */
    fun deriveStreak(
        kind: String,           // "gym" or "flow"
        planDays: List<PlanDay>,
        doneDates: List<String>,
        today: String,          // "YYYY-MM-DD"
        max: Int = 6
    ): Int {
        val plannedDates = planDays
            .filter { day ->
                when (kind) {
                    "gym"  -> day.hasGym
                    "flow" -> day.hasFlow
                    else   -> false
                }
            }
            .map { it.dateKey }
            .sorted()

        val doneSet = doneDates.toSet()
        var streak = 0

        for (dateKey in plannedDates) {
            when {
                dateKey > today ->
                    break  // future — not yet due
                dateKey == today && dateKey !in doneSet ->
                    Unit   // today unmet = pending, no penalty
                dateKey in doneSet ->
                    streak = minOf(max, streak + 1)
                else ->
                    streak = maxOf(0, streak - 1)  // missed
            }
        }
        return streak
    }
}
