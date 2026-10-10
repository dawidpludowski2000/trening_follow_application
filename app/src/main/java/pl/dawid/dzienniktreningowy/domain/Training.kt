package pl.dawid.dzienniktreningowy.domain

/** Każda pozycja oznacza grupę jednakowych serii. Różne ciężary zapisujemy osobnymi pozycjami. */
data class ExerciseInput(val name: String = "", val sets: String = "3", val reps: String = "10", val weight: String = "0")
data class ExerciseValue(val name: String, val sets: Int, val reps: Int, val weight: Double)

object Training {
    fun number(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
    fun validate(name: String, exercises: List<ExerciseInput>, duration: String? = null, note: String = ""): String? {
        if (name.trim().length !in 1..60) return "Nazwa musi mieć od 1 do 60 znaków."
        if (exercises.isEmpty() || exercises.size > 30) return "Dodaj od 1 do 30 pozycji ćwiczeń."
        if (duration != null && duration.toIntOrNull() !in 1..600) return "Czas treningu musi wynosić od 1 do 600 minut."
        if (note.length > 500) return "Notatka może mieć najwyżej 500 znaków."
        exercises.forEachIndexed { i, e ->
            val prefix = "Ćwiczenie ${i + 1}: "
            if (e.name.trim().length !in 1..60) return prefix + "wpisz nazwę (do 60 znaków)."
            if (e.sets.toIntOrNull() !in 1..50) return prefix + "liczba serii musi wynosić 1–50."
            if (e.reps.toIntOrNull() !in 1..200) return prefix + "liczba powtórzeń musi wynosić 1–200."
            val weight = number(e.weight)
            if (weight == null || weight !in 0.0..1000.0) return prefix + "ciężar musi wynosić 0–1000 kg."
        }
        return null
    }
    fun values(inputs: List<ExerciseInput>) = inputs.map { ExerciseValue(it.name.trim(), it.sets.toInt(), it.reps.toInt(), number(it.weight)!!) }
    fun volume(exercises: List<ExerciseValue>): Double = exercises.sumOf { it.sets * it.reps * it.weight }
    fun repetitions(exercises: List<ExerciseValue>): Int = exercises.sumOf { it.sets * it.reps }
}
