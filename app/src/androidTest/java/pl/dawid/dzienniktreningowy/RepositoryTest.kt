package pl.dawid.dzienniktreningowy
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import pl.dawid.dzienniktreningowy.data.*
import pl.dawid.dzienniktreningowy.domain.*
@RunWith(AndroidJUnit4::class)
class RepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: TrainingDatabase
    private lateinit var repo: TrainingRepository
    private val input = listOf(ExerciseInput("Przysiad", "3", "10", "20"))
    @Before fun setup() { context.deleteDatabase("repository-test.db"); open() }
    private fun open() { db = Room.databaseBuilder(context, TrainingDatabase::class.java,"repository-test.db").build(); repo=TrainingRepository(db) }
    @After fun close() { db.close(); context.deleteDatabase("repository-test.db") }
    @Test fun crudPersistsAndHistoryIsIndependent() = runBlocking {
        val id=repo.savePlan(0,"Plan A",input)
        assertEquals("Plan A",db.dao().plan(id)!!.plan.name)
        repo.savePlan(id,"Plan B",input.map { it.copy(weight="25") })
        assertEquals(25.0,db.dao().plan(id)!!.exercises.single().weight,0.001)
        val workout=repo.saveWorkout(Workout(name="Plan B",performedAt=System.currentTimeMillis(),durationMinutes=30,latitude=52.2297,longitude=21.0122),input)
        db.close(); open()
        assertEquals(1,db.dao().plan(id)!!.exercises.size)
        assertEquals(52.2297,db.dao().workout(workout)!!.workout.latitude!!,0.0001)
        repo.deletePlan(id)
        assertNull(db.dao().plan(id))
        assertEquals("Plan B",db.dao().workout(workout)!!.workout.name)
        repo.saveWorkout(db.dao().workout(workout)!!.workout.copy(name="Zmieniony"),input.map { it.copy(reps="12") })
        assertEquals(12,db.dao().workout(workout)!!.exercises.single().reps)
        repo.deleteWorkout(workout)
        assertNull(db.dao().workout(workout))
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM workout_exercises").use { it.moveToFirst(); assertEquals(0,it.getInt(0)) }
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM plan_exercises").use { it.moveToFirst(); assertEquals(0,it.getInt(0)) }
    }
    @Test fun invalidEditDoesNotDestroyExistingData() = runBlocking {
        val id=repo.savePlan(0,"Plan A",input)
        try { repo.savePlan(id,"",input); fail("Expected rejection") } catch (_: IllegalArgumentException) { }
        assertEquals("Plan A",db.dao().plan(id)!!.plan.name)
        assertEquals(1,db.dao().plan(id)!!.exercises.size)
    }
}
