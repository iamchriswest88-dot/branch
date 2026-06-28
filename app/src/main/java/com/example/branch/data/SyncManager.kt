package com.example.branch.data

import com.example.branch.data.model.DoneLog
import com.example.branch.data.model.Exercise
import com.example.branch.data.model.Step
import com.example.branch.data.model.Workout
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SyncManager(private val db: BranchDatabase) {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://mzgfgzojgfjeivlxtflg.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im16Z2Znem9qZ2ZqZWl2bHh0ZmxnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI2NjY5MjEsImV4cCI6MjA5ODI0MjkyMX0.0lWv-cK0orLWTuiVC5rzGcDtQADhxFthqZKPuFl1uzI"
    ) {
        install(Postgrest)
        install(Realtime)
    }

    fun startRealtimeSync(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val channel = supabase.channel("public-db-changes")
            
            // Listen to all inserts in public schema
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public")
                .onEach { action ->
                    when (action.table) {
                        "exercises" -> db.exerciseDao().upsert(action.decodeRecord<Exercise>())
                        "workouts" -> db.workoutDao().upsert(action.decodeRecord<Workout>())
                        "steps" -> db.stepDao().upsertAll(listOf(action.decodeRecord<Step>()))
                        "done_log" -> db.doneDao().insert(action.decodeRecord<DoneLog>())
                    }
                }
                .launchIn(scope)

            channel.subscribe()
        }
    }

    suspend fun pushExercise(exercise: Exercise) {
        supabase.postgrest["exercises"].upsert(exercise)
    }

    suspend fun pushWorkout(workout: Workout) {
        supabase.postgrest["workouts"].upsert(workout)
    }

    suspend fun pushSteps(steps: List<Step>) {
        supabase.postgrest["steps"].upsert(steps)
    }

    suspend fun pushDoneLog(doneLog: DoneLog) {
        supabase.postgrest["done_log"].upsert(doneLog)
    }
}
