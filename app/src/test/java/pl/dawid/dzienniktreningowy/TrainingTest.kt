package pl.dawid.dzienniktreningowy
import org.junit.Assert.*
import org.junit.Test
import pl.dawid.dzienniktreningowy.domain.*
class TrainingTest {
    private val exercise = ExerciseInput("Przysiad", "3", "10", "20")
    @Test fun acceptsBodyweightAndDecimalComma() {
        assertNull(Training.validate("Plan", listOf(exercise.copy(weight = "0"))))
        assertEquals(2.5, Training.number("2,5")!!, 0.001)
    }
    @Test fun rejectsEmptyNameAndEmptyPlan() {
        assertNotNull(Training.validate("  ", listOf(exercise)))
        assertNotNull(Training.validate("Plan", emptyList()))
        assertNotNull(Training.validate("Plan", listOf(exercise.copy(name=""))))
    }
    @Test fun rejectsInvalidRanges() {
        listOf("-1", "1001", "NaN", "Infinity", "abc").forEach {
            assertNotNull(Training.validate("Plan", listOf(exercise.copy(weight=it))))
        }
        listOf("0", "51", "abc").forEach { assertNotNull(Training.validate("Plan", listOf(exercise.copy(sets=it)))) }
        listOf("0", "201", "1.5").forEach { assertNotNull(Training.validate("Plan", listOf(exercise.copy(reps=it)))) }
    }
    @Test fun validatesDurationAndNote() {
        assertNotNull(Training.validate("Trening", listOf(exercise), "0"))
        assertNotNull(Training.validate("Trening", listOf(exercise), "601"))
        assertNotNull(Training.validate("Trening", listOf(exercise), "30", "a".repeat(501)))
        assertNull(Training.validate("Trening", listOf(exercise), "30"))
    }
    @Test fun calculatesVolumeAndRepetitions() {
        val values = Training.values(listOf(exercise, exercise.copy(sets="2", reps="5", weight="10")))
        assertEquals(700.0, Training.volume(values), 0.001)
        assertEquals(40, Training.repetitions(values))
        assertEquals(0.0, Training.volume(emptyList()), 0.001)
    }
}
