package pl.dawid.dzienniktreningowy.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.dawid.dzienniktreningowy.data.*
import pl.dawid.dzienniktreningowy.domain.*
import java.text.SimpleDateFormat
import java.util.*

fun dateLabel(time: Long): String = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("pl-PL")).format(Date(time))
fun decimal(value: Double): String = String.format(Locale.forLanguageTag("pl-PL"),"%.1f",value)
fun WorkoutWithExercises.volume() = Training.volume(exercises.map { ExerciseValue(it.name,it.sets,it.reps,it.weight) })

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TrainingApp(vm: TrainingViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val draft=vm.draft
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    BackHandler(draft != null) { if(!vm.saving) confirmExit=true }
    LaunchedEffect(vm.message) { vm.message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
    Scaffold(topBar = { TopAppBar(title={ Text(if(draft==null) "Dziennik treningowy" else if(draft.kind=="plan") "Plan treningowy" else "Zapis treningu") },
        navigationIcon={ if(draft!=null) TextButton(onClick={confirmExit=true},enabled=!vm.saving) { Text("Wróć") } }) },
        snackbarHost={ if(draft==null) SnackbarHost(snackbar) }, bottomBar={ if(draft==null) NavigationBar {
            listOf("Plany","Historia","Postępy","Ustawienia").forEachIndexed { i,tab ->
                NavigationBarItem(selected=vm.tab==tab,onClick={vm.selectTab(tab)},icon={ Text(listOf("▤","◷","↗","⚙")[i],modifier=Modifier.clearAndSetSemantics {}) },label={Text(tab,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)},modifier=Modifier.testTag("tab_$i"))
            }
        } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.TopCenter) {
            Box(Modifier.widthIn(max=840.dp).fillMaxSize()) {
                when {
                    draft != null -> EditorScreen(draft,vm.error,vm.saving,vm::edit,vm::save)
                    state.loading -> Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { CircularProgressIndicator() }
                    state.databaseError -> Column(Modifier.padding(24.dp)) { Text("Nie udało się odczytać bazy danych."); Button(onClick=vm::refresh) { Text("Spróbuj ponownie") } }
                    vm.tab=="Plany" -> PlansScreen(state.plans,vm)
                    vm.tab=="Historia" -> HistoryScreen(state.workouts,vm)
                    vm.tab=="Postępy" -> StatsScreen(state.workouts)
                    else -> SettingsScreen(vm::tell, state.plans.isEmpty() && state.workouts.isEmpty(), vm.seedingDemo, vm::loadDemoData)
                }
            }
        }
    }
    if(confirmExit) AlertDialog(onDismissRequest={confirmExit=false},title={Text("Odrzucić zmiany?")},text={Text("Niezapisane zmiany formularza zostaną utracone.")},
        confirmButton={TextButton(onClick={confirmExit=false;vm.edit(null)}) {Text("Odrzuć")}},dismissButton={TextButton(onClick={confirmExit=false}) {Text("Zostań")}})
}

@Composable private fun Heading(title: String, subtitle: String) {
    Text(title,style=MaterialTheme.typography.headlineSmall)
    Text(subtitle,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
}
@Composable private fun EmptyCard(title: String, text: String) {
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) { Text(title,style=MaterialTheme.typography.titleMedium); Text(text) } }
}
@Composable fun PlansScreen(plans: List<PlanWithExercises>,vm: TrainingViewModel) {
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    LazyColumn(Modifier.fillMaxSize().testTag("plans_list"), contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Heading("Twój następny trening", "Ułóż plan, a po ćwiczeniach zapisz wykonanie.") }
        item { Button(onClick=vm::newPlan,modifier=Modifier.fillMaxWidth().testTag("new_plan")) { Text("Dodaj plan") } }
        if(plans.isEmpty()) item { EmptyCard("Zacznij od jednego planu", "Dodaj ćwiczenia oraz planowaną liczbę serii, powtórzeń i ciężar.") }
        items(plans,key={it.plan.id}) { p ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(p.plan.name,style=MaterialTheme.typography.titleLarge)
                Text("${p.exercises.size} pozycji • ${p.exercises.sumOf { it.sets }} serii")
                p.exercises.sortedBy { it.position }.forEach { e -> Text("${e.name}: ${e.sets} × ${e.reps} • ${decimal(e.weight)} kg",style=MaterialTheme.typography.bodyMedium) }
                Button(onClick={vm.usePlan(p)},modifier=Modifier.fillMaxWidth()) { Text("Zapisz wykonanie") }
                Row { TextButton(onClick={vm.editPlan(p)}) {Text("Edytuj")}; TextButton(onClick={deleteId=p.plan.id},enabled=!vm.saving) {Text("Usuń")} }
            } }
        }
    }
    DeleteDialog(deleteId!=null,"Usunąć plan?","Historia wykonanych treningów pozostanie bez zmian.",{deleteId=null}) { deleteId?.let { vm.delete(it,true) };deleteId=null }
}
@Composable fun HistoryScreen(workouts: List<WorkoutWithExercises>,vm: TrainingViewModel) {
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    LazyColumn(Modifier.fillMaxSize().testTag("history_list"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Heading("Historia aktywności", "Wykonane treningi zapisane na tym urządzeniu.") }
        item { Button(onClick=vm::newWorkout,modifier=Modifier.fillMaxWidth()) {Text("Dodaj trening bez planu")} }
        if(workouts.isEmpty()) item { EmptyCard("Jeszcze bez treningów", "Zapisz wykonanie planu lub dodaj trening bez planu.") }
        items(workouts,key={it.workout.id}) { entry -> val w=entry.workout
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(w.name,style=MaterialTheme.typography.titleLarge)
                Text("${dateLabel(w.performedAt)} • ${w.durationMinutes} min")
                entry.exercises.sortedBy { it.position }.forEach { e -> Text("${e.name}: ${e.sets} × ${e.reps} • ${decimal(e.weight)} kg") }
                Text("Objętość: ${decimal(entry.volume())} kg",style=MaterialTheme.typography.labelLarge)
                if(w.note.isNotBlank()) Text(w.note)
                if(w.latitude!=null && w.longitude!=null) Text("Miejsce: ${String.format(Locale.US,"%.4f, %.4f",w.latitude,w.longitude)}",style=MaterialTheme.typography.bodySmall)
                Row { TextButton(onClick={vm.editWorkout(entry)}) {Text("Edytuj")};TextButton(onClick={deleteId=w.id},enabled=!vm.saving) {Text("Usuń")} }
            } }
        }
    }
    DeleteDialog(deleteId!=null,"Usunąć trening?","Ten zapis zostanie trwale usunięty z historii i statystyk.",{deleteId=null}) { deleteId?.let {vm.delete(it,false)};deleteId=null }
}
@Composable private fun DeleteDialog(show:Boolean,title:String,text:String,dismiss:()->Unit,confirm:()->Unit) {
    if(show) AlertDialog(onDismissRequest=dismiss,title={Text(title)},text={Text(text)},confirmButton={TextButton(onClick=confirm){Text("Usuń na stałe")}},dismissButton={TextButton(onClick=dismiss){Text("Anuluj")}})
}

@Composable fun EditorScreen(d: EditorDraft,error:String?,saving:Boolean,change:(EditorDraft)->Unit,save:()->Unit) {
    val context=LocalContext.current
    val workout=d.kind=="workout"
    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(Modifier.weight(1f).testTag("editor_list"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Heading(if(d.id==0L) "Nowy ${if(workout) "trening" else "plan"}" else "Edytuj zapis", "Jedna pozycja to seria lub grupa jednakowych serii. Ciężar 0 kg oznacza ćwiczenie bez obciążenia.") }
            item { OutlinedTextField(d.name,{change(d.copy(name=it.take(60)))},label={Text("Nazwa")},singleLine=true,enabled=!saving,modifier=Modifier.fillMaxWidth().testTag("name")) }
            if(workout) {
                item { OutlinedButton(onClick={ val c=Calendar.getInstance().apply {timeInMillis=d.date}; DatePickerDialog(context,{_,y,m,day ->
                    val picked=Calendar.getInstance().apply {set(y,m,day,0,0,0);set(Calendar.MILLISECOND,0)};change(d.copy(date=picked.timeInMillis))
                },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).apply {datePicker.maxDate=System.currentTimeMillis()}.show() },enabled=!saving) {Text("Data: ${dateLabel(d.date)}")} }
                item { OutlinedTextField(d.duration,{change(d.copy(duration=it.take(3)))},label={Text("Czas (minuty)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!saving,modifier=Modifier.fillMaxWidth().testTag("duration")) }
            }
            items(d.exercises.size) { index -> val e=d.exercises[index]
                fun update(new: ExerciseInput) = change(d.copy(exercises=d.exercises.toMutableList().also {it[index]=new}))
                OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("Ćwiczenie ${index+1}",style=MaterialTheme.typography.titleMedium)
                    OutlinedTextField(e.name,{update(e.copy(name=it.take(60)))},label={Text("Ćwiczenie")},singleLine=true,enabled=!saving,modifier=Modifier.fillMaxWidth().testTag("exercise_$index"))
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(e.sets,{update(e.copy(sets=it.take(3)))},label={Text("Serie")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!saving,modifier=Modifier.weight(1f).testTag("sets_$index"))
                        OutlinedTextField(e.reps,{update(e.copy(reps=it.take(3)))},label={Text("Powtórz.")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!saving,modifier=Modifier.weight(1f).testTag("reps_$index"))
                    }
                    OutlinedTextField(e.weight,{update(e.copy(weight=it.take(8)))},label={Text("Ciężar (kg)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true,enabled=!saving,modifier=Modifier.fillMaxWidth().testTag("weight_$index"))
                    TextButton(onClick={change(d.copy(exercises=d.exercises.filterIndexed {i,_->i!=index}))},enabled=!saving) {Text("Usuń pozycję")}
                } }
            }
            item { OutlinedButton(onClick={change(d.copy(exercises=d.exercises+ExerciseInput()))},enabled=!saving && d.exercises.size<30,modifier=Modifier.fillMaxWidth()) {Text("Dodaj ćwiczenie")} }
            if(workout) {
                item { OutlinedTextField(d.note,{change(d.copy(note=it.take(500)))},label={Text("Notatka (opcjonalna)")},enabled=!saving,modifier=Modifier.fillMaxWidth().testTag("note")) }
                item { LocationSection(d,saving,change) }
            }
        }
        Surface(tonalElevation=3.dp) { Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp)) {
            if(error!=null) Text(error,color=MaterialTheme.colorScheme.error,modifier=Modifier.testTag("validation_error").semantics {liveRegion=LiveRegionMode.Polite})
            Button(onClick=save,enabled=!saving,modifier=Modifier.fillMaxWidth().testTag("save")) {Text(if(saving) "Zapisywanie…" else "Zapisz")}
        } }
    }
}

@Composable fun StatsScreen(workouts: List<WorkoutWithExercises>) {
    val last=workouts.sortedBy {it.workout.performedAt}.takeLast(7)
    val total=workouts.sumOf {it.volume()}
    val color=MaterialTheme.colorScheme.primary
    LazyColumn(Modifier.fillMaxSize().testTag("stats_list"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Heading("Twoje postępy", "Podsumowanie wszystkich zapisanych treningów.") }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("${workouts.size} treningów",style=MaterialTheme.typography.headlineMedium)
            Text("${workouts.sumOf {it.workout.durationMinutes}} minut aktywności")
            Text("${decimal(total)} kg łącznej objętości")
            Text("${workouts.sumOf {it.exercises.sumOf {e -> e.sets}}} wykonanych serii")
        } } }
        if(last.isEmpty()) item { EmptyCard("Wykres pojawi się po treningu", "Zapisz pierwszy trening, aby zobaczyć statystyki.") }
        else {
            item { Text("Objętość ostatnich ${last.size} treningów",style=MaterialTheme.typography.titleLarge) }
            item { Canvas(Modifier.fillMaxWidth().height(170.dp).semantics {contentDescription="Wykres objętości. Dokładne wartości w liście poniżej."}) {
                val maximum=last.maxOf {it.volume()}.coerceAtLeast(1.0)
                val step=size.width/last.size
                drawLine(color.copy(alpha=0.4f),Offset(0f,size.height-2),Offset(size.width,size.height-2),2f)
                last.forEachIndexed {i,w -> val h=(w.volume()/maximum*(size.height-8)).toFloat();drawRect(color,Offset(i*step+step*0.2f,size.height-h-2),Size(step*0.6f,h))}
            } }
            items(last) {w -> Text("${dateLabel(w.workout.performedAt)} · ${w.workout.name}: ${decimal(w.volume())} kg")}
        }
        item { Text("Objętość = suma (serie × powtórzenia × ciężar). Wykres czytaj od najstarszego treningu po lewej. Porównuj podobne treningi; masa ciała nie jest wliczana.",style=MaterialTheme.typography.bodySmall) }
    }
}

