package pl.dawid.dzienniktreningowy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT COUNT(*) FROM plans") suspend fun planCount(): Int
    @Query("SELECT COUNT(*) FROM workouts") suspend fun workoutCount(): Int
    @Transaction @Query("SELECT * FROM plans ORDER BY name COLLATE NOCASE")
    fun plans(): Flow<List<PlanWithExercises>>
    @Transaction @Query("SELECT * FROM workouts ORDER BY performedAt DESC, id DESC")
    fun workouts(): Flow<List<WorkoutWithExercises>>
    @Transaction @Query("SELECT * FROM plans WHERE id = :id") suspend fun plan(id: Long): PlanWithExercises?
    @Transaction @Query("SELECT * FROM workouts WHERE id = :id") suspend fun workout(id: Long): WorkoutWithExercises?
    @Insert suspend fun insertPlan(plan: Plan): Long
    @Update suspend fun updatePlan(plan: Plan)
    @Insert suspend fun insertPlanExercises(items: List<PlanExercise>)
    @Query("DELETE FROM plan_exercises WHERE planId = :id") suspend fun clearPlanExercises(id: Long)
    @Query("DELETE FROM plans WHERE id = :id") suspend fun deletePlan(id: Long)
    @Insert suspend fun insertWorkout(workout: Workout): Long
    @Update suspend fun updateWorkout(workout: Workout)
    @Insert suspend fun insertWorkoutExercises(items: List<WorkoutExercise>)
    @Query("DELETE FROM workout_exercises WHERE workoutId = :id") suspend fun clearWorkoutExercises(id: Long)
    @Query("DELETE FROM workouts WHERE id = :id") suspend fun deleteWorkout(id: Long)
}
