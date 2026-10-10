package pl.dawid.dzienniktreningowy.data

import android.content.Context
import androidx.room.*

@Database(entities = [Plan::class, PlanExercise::class, Workout::class, WorkoutExercise::class], version = 1, exportSchema = true)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun dao(): TrainingDao
    companion object {
        @Volatile private var instance: TrainingDatabase? = null
        fun get(context: Context): TrainingDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, TrainingDatabase::class.java, "training.db")
                .build().also { instance = it }
        }
    }
}
