package pl.dawid.dzienniktreningowy.data

import androidx.room.*

@Entity(tableName = "plans")
data class Plan(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

@Entity(tableName = "plan_exercises", foreignKeys = [ForeignKey(entity = Plan::class,
    parentColumns = ["id"], childColumns = ["planId"], onDelete = ForeignKey.CASCADE)], indices = [Index("planId")])
data class PlanExercise(@PrimaryKey(autoGenerate = true) val id: Long = 0, val planId: Long,
    val position: Int, val name: String, val sets: Int, val reps: Int, val weight: Double)

data class PlanWithExercises(@Embedded val plan: Plan,
    @Relation(parentColumn = "id", entityColumn = "planId") val exercises: List<PlanExercise>)

@Entity(tableName = "workouts")
data class Workout(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String,
    val performedAt: Long, val durationMinutes: Int, val note: String = "",
    val latitude: Double? = null, val longitude: Double? = null)

@Entity(tableName = "workout_exercises", foreignKeys = [ForeignKey(entity = Workout::class,
    parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE)], indices = [Index("workoutId")])
data class WorkoutExercise(@PrimaryKey(autoGenerate = true) val id: Long = 0, val workoutId: Long,
    val position: Int, val name: String, val sets: Int, val reps: Int, val weight: Double)

data class WorkoutWithExercises(@Embedded val workout: Workout,
    @Relation(parentColumn = "id", entityColumn = "workoutId") val exercises: List<WorkoutExercise>)
