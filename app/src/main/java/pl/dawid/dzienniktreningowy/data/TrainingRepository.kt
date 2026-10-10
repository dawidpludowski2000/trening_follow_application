package pl.dawid.dzienniktreningowy.data

import androidx.room.withTransaction
import pl.dawid.dzienniktreningowy.domain.*

class TrainingRepository(private val db: TrainingDatabase) {
    private val dao = db.dao()
    val plans = dao.plans()
    val workouts = dao.workouts()
    suspend fun savePlan(id: Long, name: String, inputs: List<ExerciseInput>): Long {
        require(Training.validate(name, inputs) == null)
        return db.withTransaction {
            val key = if (id == 0L) dao.insertPlan(Plan(name = name.trim())) else {
                check(dao.plan(id) != null)
                dao.updatePlan(Plan(id, name.trim())); dao.clearPlanExercises(id); id
            }
            dao.insertPlanExercises(Training.values(inputs).mapIndexed { i, e -> PlanExercise(planId = key, position = i, name = e.name, sets = e.sets, reps = e.reps, weight = e.weight) })
            key
        }
    }
    suspend fun saveWorkout(workout: Workout, inputs: List<ExerciseInput>): Long {
        require(Training.validate(workout.name, inputs, workout.durationMinutes.toString(), workout.note) == null)
        require(workout.performedAt > 0)
        require((workout.latitude == null && workout.longitude == null) ||
            (workout.latitude != null && workout.longitude != null && workout.latitude.isFinite() && workout.longitude.isFinite() && workout.latitude in -90.0..90.0 && workout.longitude in -180.0..180.0))
        return db.withTransaction {
            val clean = workout.copy(name = workout.name.trim(), note = workout.note.trim())
            val key = if (clean.id == 0L) dao.insertWorkout(clean) else {
                check(dao.workout(clean.id) != null)
                dao.updateWorkout(clean); dao.clearWorkoutExercises(clean.id); clean.id
            }
            dao.insertWorkoutExercises(Training.values(inputs).mapIndexed { i, e -> WorkoutExercise(workoutId = key, position = i, name = e.name, sets = e.sets, reps = e.reps, weight = e.weight) })
            key
        }
    }
    suspend fun deletePlan(id: Long) = dao.deletePlan(id)
    suspend fun deleteWorkout(id: Long) = dao.deleteWorkout(id)
}
