package com.example.branch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.branch.data.dao.*
import com.example.branch.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Exercise::class, Workout::class, Step::class, PlanDay::class, DoneLog::class],
    version = 1,
    exportSchema = false
)
abstract class BranchDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun stepDao(): StepDao
    abstract fun planDao(): PlanDao
    abstract fun doneDao(): DoneDao

    companion object {
        @Volatile
        private var INSTANCE: BranchDatabase? = null

        fun getDatabase(context: Context): BranchDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BranchDatabase::class.java,
                    "branch_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.exerciseDao()?.insertIfAbsent(
                                    SeedData.GYM_EXERCISES + SeedData.FLOW_EXERCISES
                                )
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
