package com.example.branch.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "branch_prefs")

class BranchPrefs(private val context: Context) {

    companion object {
        val GYM_STREAK           = intPreferencesKey("gym_streak")
        val FLOW_STREAK          = intPreferencesKey("flow_streak")
        val LAST_GLYPH_CATEGORY  = stringPreferencesKey("last_glyph_category")
        val REMINDER_ENABLED     = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME        = stringPreferencesKey("reminder_time")
    }

    val gymStreak: Flow<Int> = context.dataStore.data
        .map { it[GYM_STREAK] ?: 0 }

    val flowStreak: Flow<Int> = context.dataStore.data
        .map { it[FLOW_STREAK] ?: 0 }

    val lastGlyphCategory: Flow<String> = context.dataStore.data
        .map { it[LAST_GLYPH_CATEGORY] ?: "gym" }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[REMINDER_ENABLED] ?: false }

    val reminderTime: Flow<String> = context.dataStore.data
        .map { it[REMINDER_TIME] ?: "17:00" }

    suspend fun setGymStreak(value: Int) {
        context.dataStore.edit { it[GYM_STREAK] = value }
    }

    suspend fun setFlowStreak(value: Int) {
        context.dataStore.edit { it[FLOW_STREAK] = value }
    }

    suspend fun setLastGlyphCategory(category: String) {
        context.dataStore.edit { it[LAST_GLYPH_CATEGORY] = category }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { it[REMINDER_TIME] = time }
    }
}
