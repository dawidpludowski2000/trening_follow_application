package pl.dawid.dzienniktreningowy.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pl.dawid.dzienniktreningowy.data.*
import pl.dawid.dzienniktreningowy.domain.*

data class TrainingState(val loading: Boolean = true, val plans: List<PlanWithExercises> = emptyList(),
    val workouts: List<WorkoutWithExercises> = emptyList(), val databaseError: Boolean = false)

class TrainingViewModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {
    private val repo = TrainingRepository(TrainingDatabase.get(app))
    private val _state = MutableStateFlow(TrainingState())
    val state: StateFlow<TrainingState> = _state.asStateFlow()
    var tab by mutableStateOf(saved.get<String>("tab") ?: "Plany"); private set
    var draft by mutableStateOf(saved.get<String>("draft")?.let { runCatching { EditorDraft.decode(it) }.getOrNull() }); private set
    var error by mutableStateOf<String?>(null); private set
    var message by mutableStateOf<String?>(null); private set
    var saving by mutableStateOf(false); private set
    private var observation: Job? = null
    init { refresh() }
    fun refresh() {
        observation?.cancel()
        observation = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, databaseError = false)
            combine(repo.plans, repo.workouts) { p,w -> TrainingState(false,p,w) }
                .catch { e -> if(e is CancellationException) throw e; _state.value = _state.value.copy(loading=false,databaseError=true) }
                .collect { _state.value = it }
        }
    }
    fun selectTab(value: String) { tab=value; saved["tab"]=value }
    fun edit(value: EditorDraft?) { if(saving) return; draft=value; saved["draft"]=value?.encode(); error=null; if(value!=null) message=null }
    fun tell(value: String) { message=value }
    fun clearMessage() { message=null }
    fun newPlan() = edit(EditorDraft())
    fun newWorkout() = edit(EditorDraft(kind="workout"))
    fun editPlan(p: PlanWithExercises) = edit(EditorDraft(id=p.plan.id,name=p.plan.name,exercises=p.exercises.sortedBy { it.position }.map { ExerciseInput(it.name,it.sets.toString(),it.reps.toString(),it.weight.toString()) }))
    fun usePlan(p: PlanWithExercises) = edit(EditorDraft(kind="workout",name=p.plan.name,exercises=p.exercises.sortedBy { it.position }.map { ExerciseInput(it.name,it.sets.toString(),it.reps.toString(),it.weight.toString()) }))
    fun editWorkout(w: WorkoutWithExercises) = edit(EditorDraft(kind="workout",id=w.workout.id,name=w.workout.name,date=w.workout.performedAt,
        duration=w.workout.durationMinutes.toString(),note=w.workout.note,latitude=w.workout.latitude,longitude=w.workout.longitude,
        exercises=w.exercises.sortedBy { it.position }.map { ExerciseInput(it.name,it.sets.toString(),it.reps.toString(),it.weight.toString()) }))
    fun save() {
        val value=draft ?: return
        if(saving) return
        error=Training.validate(value.name,value.exercises,if(value.kind=="workout") value.duration else null,value.note)
        if(error != null) return
        saving=true
        viewModelScope.launch {
            try {
                if(value.kind=="plan") repo.savePlan(value.id,value.name,value.exercises)
                else repo.saveWorkout(Workout(value.id,value.name,value.date,value.duration.toInt(),value.note,value.latitude,value.longitude),value.exercises)
                saving=false; edit(null); selectTab(if(value.kind=="plan") "Plany" else "Historia"); tell("Zapisano.")
            } catch(e: Exception) {
                if(e is CancellationException) throw e
                error="Nie udało się zapisać danych. Spróbuj ponownie; sprawdź wolne miejsce na urządzeniu."
            } finally { saving=false }
        }
    }
    fun delete(id: Long, plan: Boolean) {
        if(saving) return
        saving=true
        viewModelScope.launch {
            try { if(plan) repo.deletePlan(id) else repo.deleteWorkout(id); tell("Usunięto.") }
            catch(e: Exception) { if(e is CancellationException) throw e; tell("Nie udało się usunąć danych. Spróbuj ponownie.") }
            finally { saving=false }
        }
    }
}

