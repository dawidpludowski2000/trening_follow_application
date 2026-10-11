package pl.dawid.dzienniktreningowy.data

import androidx.room.withTransaction
import java.util.Calendar
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

    /** Inserts the optional presentation data only when the journal is completely empty. */
    suspend fun seedDemoIfEmpty(): Boolean = db.withTransaction {
        if (dao.planCount() != 0 || dao.workoutCount() != 0) return@withTransaction false

        val strength = listOf(ExerciseInput("Przysiad", "3", "10", "20"), ExerciseInput("Wyciskanie hantli", "3", "10", "8"))
        insertPlan("Trening całego ciała", strength)
        insertPlan("Krótki trening w domu", listOf(ExerciseInput("Pompki", "3", "12", "0"), ExerciseInput("Wykroki", "3", "10", "0")))
        listOf(18, 15, 12, 8, 4, 0).forEachIndexed { index, daysAgo ->
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }.timeInMillis
            val exercises = strength.mapIndexed { exerciseIndex, exercise ->
                exercise.copy(weight = if (exerciseIndex == 0) (20 + index * 2.5).toString() else "8")
            }
            insertWorkout(Workout(name = "Trening całego ciała", performedAt = date, durationMinutes = 35 + index * 2,
                note = if (index == 5) "Dobry trening. Następnym razem spróbuję większego ciężaru." else "",
                latitude = if (index == 5) 52.2297 else null, longitude = if (index == 5) 21.0122 else null), exercises)
        }
        true
    }

    private suspend fun insertPlan(name: String, inputs: List<ExerciseInput>) {
        val id = dao.insertPlan(Plan(name = name))
        dao.insertPlanExercises(Training.values(inputs).mapIndexed { i, e -> PlanExercise(planId = id, position = i, name = e.name, sets = e.sets, reps = e.reps, weight = e.weight) })
    }

    private suspend fun insertWorkout(workout: Workout, inputs: List<ExerciseInput>) {
        val id = dao.insertWorkout(workout)
        dao.insertWorkoutExercises(Training.values(inputs).mapIndexed { i, e -> WorkoutExercise(workoutId = id, position = i, name = e.name, sets = e.sets, reps = e.reps, weight = e.weight) })
    }
}
