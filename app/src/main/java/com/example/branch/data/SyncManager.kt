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
                db.exerciseDao().upsert(
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
                db.workoutDao().upsert(
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
            val stepsList = mutableListOf<Step>()
            for (i in 0 until stepArray.length()) {
                val obj = stepArray.getJSONObject(i)
                stepsList.add(
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
            if (stepsList.isNotEmpty()) {
                db.stepDao().upsertAll(stepsList)
            }
            // 4. Fetch DoneLogs
            val doneJson = fetchFromSupabase("/rest/v1/done_log")
            val doneArray = JSONArray(doneJson)
            for (i in 0 until doneArray.length()) {
                val obj = doneArray.getJSONObject(i)
                db.doneDao().insert(
                    com.example.branch.data.model.DoneLog(
                        id = obj.getString("id"),
                        category = obj.getString("category"),
                        dateKey = obj.getString("date_key")
                    )
                )
            }

            // 5. Fetch PlanDays
            val planJson = fetchFromSupabase("/rest/v1/plan_days")
            val planArray = JSONArray(planJson)
            for (i in 0 until planArray.length()) {
                val obj = planArray.getJSONObject(i)
                db.planDao().upsert(
                    com.example.branch.data.model.PlanDay(
                        dateKey = obj.getString("date_key"),
                        hasGym = obj.optBoolean("has_gym", false),
                        hasFlow = obj.optBoolean("has_flow", false),
                        hasRest = obj.optBoolean("has_rest", false)
                    )
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncToCloud(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Push Exercises
            val exercises = db.exerciseDao().getAllSync().filter {
                try { java.util.UUID.fromString(it.id); true } catch (e: Exception) { false }
            }
            if (exercises.isNotEmpty()) {
                val jsonArray = JSONArray()
                exercises.forEach {
                    val obj = org.json.JSONObject()
                    obj.put("id", it.id)
                    obj.put("name", it.name)
                    obj.put("area", it.area)
                    obj.put("category", it.category)
                    obj.put("is_custom", it.isCustom)
                    jsonArray.put(obj)
                }
                pushToSupabase("/rest/v1/exercises", jsonArray.toString())
            }

            // 2. Push Workouts
            val workouts = db.workoutDao().getAllSync()
            if (workouts.isNotEmpty()) {
                val jsonArray = JSONArray()
                workouts.forEach {
                    val obj = org.json.JSONObject()
                    obj.put("id", it.id)
                    obj.put("name", it.name)
                    obj.put("category", it.category)
                    jsonArray.put(obj)
                }
                pushToSupabase("/rest/v1/workouts", jsonArray.toString())
            }

            // 3. Push Steps
            val steps = db.stepDao().getAllSync()
            if (steps.isNotEmpty()) {
                val jsonArray = JSONArray()
                steps.forEach {
                    val obj = org.json.JSONObject()
                    obj.put("id", it.id)
                    obj.put("workout_id", it.workoutId)
                    obj.put("exercise_id", it.exerciseId)
                    obj.put("exercise_name", it.exerciseName)
                    obj.put("sets", it.sets)
                    obj.put("work_sec", it.workSec)
                    obj.put("rest_sec", it.restSec)
                    obj.put("sides", it.sides)
                    obj.put("swap_sec", it.swapSec)
                    obj.put("sort_order", it.sortOrder)
                    jsonArray.put(obj)
                }
                pushToSupabase("/rest/v1/steps", jsonArray.toString())
            }

            // 4. Push DoneLogs
            val doneLogs = db.doneDao().getAllSync()
            if (doneLogs.isNotEmpty()) {
                val jsonArray = JSONArray()
                doneLogs.forEach {
                    val obj = org.json.JSONObject()
                    obj.put("id", it.id)
                    obj.put("category", it.category)
                    obj.put("date_key", it.dateKey)
                    jsonArray.put(obj)
                }
                pushToSupabase("/rest/v1/done_log", jsonArray.toString())
            }

            // 5. Push PlanDays
            val planDays = db.planDao().getAllSync()
            if (planDays.isNotEmpty()) {
                val jsonArray = JSONArray()
                planDays.forEach {
                    val obj = org.json.JSONObject()
                    obj.put("date_key", it.dateKey)
                    obj.put("has_gym", it.hasGym)
                    obj.put("has_flow", it.hasFlow)
                    obj.put("has_rest", it.hasRest)
                    jsonArray.put(obj)
                }
                pushToSupabase("/rest/v1/plan_days", jsonArray.toString())
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

    private fun pushToSupabase(endpoint: String, jsonBody: String) {
        val url = URL("$SUPABASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("apikey", SUPABASE_KEY)
        connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
        connection.doOutput = true
        
        connection.outputStream.use { os ->
            val input = jsonBody.toByteArray(Charsets.UTF_8)
            os.write(input, 0, input.size)
        }
        
        // Ensure request is sent
        connection.responseCode
    }
}
