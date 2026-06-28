package com.example.branch.data

import com.example.branch.data.model.Exercise
import com.example.branch.data.model.Step
import com.example.branch.data.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class SyncManager(private val db: BranchDatabase) {
    private val SUPABASE_URL = "https://mzgfgzojgfjeivlxtflg.supabase.co"
    private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im16Z2Znem9qZ2ZqZWl2bHh0ZmxnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI2NjY5MjEsImV4cCI6MjA5ODI0MjkyMX0.0lWv-cK0orLWTuiVC5rzGcDtQADhxFthqZKPuFl1uzI"

    suspend fun syncFromCloud(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch Exercises
            val exJson = fetchFromSupabase("/rest/v1/exercises")
            val exArray = JSONArray(exJson)
            for (i in 0 until exArray.length()) {
                val obj = exArray.getJSONObject(i)
                db.exerciseDao().insert(
                    Exercise(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        area = obj.getString("area"),
                        category = obj.getString("category"),
                        isCustom = obj.optBoolean("is_custom", true) // Note: Supabase uses is_custom
                    )
                )
            }

            // 2. Fetch Workouts
            val woJson = fetchFromSupabase("/rest/v1/workouts")
            val woArray = JSONArray(woJson)
            for (i in 0 until woArray.length()) {
                val obj = woArray.getJSONObject(i)
                db.workoutDao().insert(
                    Workout(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = obj.getString("category")
                    )
                )
            }

            // 3. Fetch Steps
            val stepJson = fetchFromSupabase("/rest/v1/steps")
            val stepArray = JSONArray(stepJson)
            for (i in 0 until stepArray.length()) {
                val obj = stepArray.getJSONObject(i)
                db.stepDao().insert(
                    Step(
                        id = obj.getString("id"),
                        workoutId = obj.getString("workout_id"),
                        exerciseId = obj.getString("exercise_id"),
                        exerciseName = obj.getString("exercise_name"),
                        sets = obj.getInt("sets"),
                        workSec = obj.getInt("work_sec"),
                        restSec = obj.getInt("rest_sec"),
                        sides = obj.optBoolean("sides", false),
                        swapSec = obj.optInt("swap_sec", 5),
                        sortOrder = obj.getInt("sort_order")
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun fetchFromSupabase(endpoint: String): String {
        val url = URL("$SUPABASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("apikey", SUPABASE_KEY)
        connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
        connection.setRequestProperty("Content-Type", "application/json")
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}
