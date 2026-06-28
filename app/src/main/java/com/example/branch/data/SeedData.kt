package com.example.branch.data

import com.example.branch.data.model.Exercise

object SeedData {

    val GYM_EXERCISES: List<Exercise> = listOf(
        e("sg01", "Push-ups",            "Chest",     "gym"),
        e("sg02", "Squats",              "Quads",     "gym"),
        e("sg03", "Lunges",              "Quads",     "gym"),
        e("sg04", "Plank",               "Core",      "gym"),
        e("sg05", "Glute Bridge",        "Glutes",    "gym"),
        e("sg06", "Bird Dog",            "Core",      "gym"),
        e("sg07", "Calf Raise",          "Calves",    "gym"),
        e("sg08", "Dead Bug",            "Core",      "gym"),
        e("sg09", "DB Bicep Curl",       "Biceps",    "gym"),
        e("sg10", "DB Hammer Curl",      "Biceps",    "gym"),
        e("sg11", "DB Shoulder Press",   "Shoulders", "gym"),
        e("sg12", "DB Lateral Raise",    "Shoulders", "gym"),
        e("sg13", "DB Front Raise",      "Shoulders", "gym"),
        e("sg14", "DB Bent-over Row",    "Back",      "gym"),
        e("sg15", "DB Single-arm Row",   "Back",      "gym"),
        e("sg16", "DB Floor Chest Press","Chest",     "gym"),
        e("sg17", "DB Chest Fly",        "Chest",     "gym"),
        e("sg18", "DB Pullover",         "Back",      "gym"),
        e("sg19", "DB Goblet Squat",     "Quads",     "gym"),
        e("sg20", "DB Romanian Deadlift","Hamstrings","gym"),
        e("sg21", "DB Lunge",            "Quads",     "gym"),
        e("sg22", "DB Step-up",          "Glutes",    "gym"),
        e("sg23", "DB Overhead Triceps", "Triceps",   "gym"),
        e("sg24", "DB Triceps Kickback", "Triceps",   "gym"),
        e("sg25", "DB Shrug",            "Traps",     "gym"),
        e("sg26", "DB Renegade Row",     "Back",      "gym"),
        e("sg27", "DB Thruster",         "Full body", "gym"),
        e("sg28", "DB Russian Twist",    "Core",      "gym"),
    )

    val FLOW_EXERCISES: List<Exercise> = listOf(
        e("sf01", "Downward Dog",       "Spine",      "flow"),
        e("sf02", "Child's Pose",       "Hips",       "flow"),
        e("sf03", "Cobra",              "Spine",      "flow"),
        e("sf04", "Cat\u2013Cow",       "Spine",      "flow"),
        e("sf05", "Forward Fold",       "Hamstrings", "flow"),
        e("sf06", "Low Lunge",          "Hips",       "flow"),
        e("sf07", "Warrior I",          "Legs",       "flow"),
        e("sf08", "Warrior II",         "Legs",       "flow"),
        e("sf09", "Triangle",           "Hips",       "flow"),
        e("sf10", "Pigeon",             "Hips",       "flow"),
        e("sf11", "Seated Twist",       "Spine",      "flow"),
        e("sf12", "Bridge Pose",        "Spine",      "flow"),
        e("sf13", "Tree Pose",          "Balance",    "flow"),
        e("sf14", "Boat Pose",          "Core",       "flow"),
        e("sf15", "Happy Baby",         "Hips",       "flow"),
        e("sf16", "Savasana",           "Full body",  "flow"),
        e("sf17", "Hamstring Stretch",  "Hamstrings", "flow"),
        e("sf18", "Quad Stretch",       "Quads",      "flow"),
        e("sf19", "Hip Flexor Stretch", "Hips",       "flow"),
        e("sf20", "Chest Opener",       "Chest",      "flow"),
        e("sf21", "Shoulder Stretch",   "Shoulders",  "flow"),
        e("sf22", "Neck Stretch",       "Neck",       "flow"),
        e("sf23", "Calf Stretch",       "Calves",     "flow"),
        e("sf24", "Butterfly",          "Hips",       "flow"),
        e("sf25", "Figure-4",           "Hips",       "flow"),
        e("sf26", "Side Bend",          "Spine",      "flow"),
        e("sf27", "Supine Twist",       "Spine",      "flow"),
    )

    val ALL_AREAS = listOf(
        "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Traps",
        "Core", "Legs", "Glutes", "Hamstrings", "Quads", "Calves",
        "Hips", "Spine", "Neck", "Balance", "Full body", "Other"
    )

    private fun e(id: String, name: String, area: String, category: String) =
        Exercise(id = id, name = name, area = area, category = category, isCustom = false)
}
