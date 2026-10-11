package pl.dawid.dzienniktreningowy.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.*
import pl.dawid.dzienniktreningowy.device.*
import java.util.Locale

@Composable fun LocationSection(d:EditorDraft,saving:Boolean,change:(EditorDraft)->Unit) {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val latest by rememberUpdatedState(d)
    val latestChange by rememberUpdatedState(change)
    var busy by remember {mutableStateOf(false)}
    var status by remember {mutableStateOf<String?>(null)}
    val obtain:()->Unit = {
        if(!busy) { busy=true;status="Pobieranie lokalizacji…"
            scope.launch {
                try {
                    val point=TrainingLocation.current(context)
                    if(point==null) status="Nie udało się ustalić miejsca. Spróbuj na zewnątrz lub zapisz bez lokalizacji."
                    else {latestChange(latest.copy(latitude=point.latitude,longitude=point.longitude));status="Miejsce dodane."}
                } catch(e:Exception) {
                    if(e is CancellationException) throw e
                    status=if(e is SecurityException) "Brak zgody na lokalizację. Trening możesz zapisać bez niej." else e.message ?: "Lokalizacja jest niedostępna."
                } finally {busy=false}
            }
        }
    }
    val permissions=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if(TrainingLocation.permitted(context)) obtain() else status="Brak zgody na lokalizację. Trening możesz zapisać bez niej."
    }
    OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Miejsce treningu",style=MaterialTheme.typography.titleMedium)
        Text("Opcjonalnie zapisz bieżące miejsce. Lokalizację odczytujemy tylko po naciśnięciu przycisku, bez śledzenia w tle.",style=MaterialTheme.typography.bodySmall)
        if(d.latitude!=null && d.longitude!=null) {
            Text(String.format(Locale.US,"%.4f, %.4f",d.latitude,d.longitude),modifier=Modifier.testTag("coordinates"))
            TextButton(onClick={change(d.copy(latitude=null,longitude=null));status=null},enabled=!busy&&!saving) {Text("Usuń lokalizację")}
        }
        OutlinedButton(onClick={if(TrainingLocation.permitted(context)) obtain() else permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))},enabled=!busy&&!saving,modifier=Modifier.testTag("location")) {Text(if(busy) "Ustalanie miejsca…" else "Pobierz bieżącą lokalizację")}
        status?.let {Text(it,style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("location_status"))}
    } }
}

@Composable fun SettingsScreen(tell:(String)->Unit, canLoadDemo:Boolean, seedingDemo:Boolean, loadDemo:()->Unit) {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val prefs=remember {Reminders.preferences(context)}
    var enabled by remember {mutableStateOf(prefs.getBoolean("enabled",false))}
    var hour by remember {mutableIntStateOf(prefs.getInt("hour",18))}
    var minute by remember {mutableIntStateOf(prefs.getInt("minute",0))}
    var allowed by remember {mutableStateOf(Reminders.allowed(context))}
    var busy by remember {mutableStateOf(false)}
    var pendingAction by remember {mutableStateOf("enable")}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer=LifecycleEventObserver {_,event -> if(event==Lifecycle.Event.ON_RESUME) allowed=Reminders.allowed(context)}
        lifecycle.addObserver(observer); onDispose {lifecycle.removeObserver(observer)}
    }
    fun update(value:Boolean,h:Int=hour,m:Int=minute) {
        if(busy) return
        busy=true
        scope.launch {
            try {Reminders.schedule(context,value,h,m);enabled=value;hour=h;minute=m;tell(if(value) "Przypomnienia włączone." else "Przypomnienia wyłączone.")}
            catch(e:Exception) {if(e is CancellationException) throw e;tell("Nie udało się zapisać przypomnienia. Spróbuj ponownie.")}
            finally {busy=false}
        }
    }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        allowed=Reminders.allowed(context)
        if(granted && allowed) {if(pendingAction=="test") {Reminders.notify(context);tell("Wysłano powiadomienie testowe.")} else update(true)}
        else tell("Powiadomienia są zablokowane. Możesz je włączyć w ustawieniach telefonu.")
    }
    fun requirePermission(action:String) {
        if(Reminders.allowed(context)) {if(action=="test") {Reminders.notify(context);tell("Wysłano powiadomienie testowe.")} else update(true)}
        else if(Build.VERSION.SDK_INT>=33) {pendingAction=action;permission.launch(Manifest.permission.POST_NOTIFICATIONS)}
        else tell("Włącz powiadomienia w ustawieniach telefonu.")
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).testTag("settings"),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("Przypomnienia i prywatność",style=MaterialTheme.typography.headlineSmall)
        OutlinedCard { Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Dane przykładowe",style=MaterialTheme.typography.titleLarge)
            Text(if (canLoadDemo) "Dodaj przykładowe plany i historię treningów, aby od razu zobaczyć działanie statystyk. Dane będzie można edytować i usuwać." else "Przykładowe dane są dostępne tylko wtedy, gdy dziennik jest pusty.")
            Button(onClick=loadDemo,enabled=canLoadDemo && !seedingDemo,modifier=Modifier.testTag("load_demo_data")) {
                Text(if (seedingDemo) "Wczytywanie…" else "Wczytaj przykładowe dane")
            }
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Codzienne przypomnienie",style=MaterialTheme.typography.titleLarge)
            Text("Android może opóźnić powiadomienie, np. podczas oszczędzania baterii. To przybliżona pora, nie dokładny alarm.")
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(if(enabled) "Włączone" else "Wyłączone",modifier=Modifier.weight(1f))
                Switch(checked=enabled,onCheckedChange={if(it) requirePermission("enable") else update(false)},enabled=!busy,modifier=Modifier.testTag("reminder_switch"))
            }
            OutlinedButton(onClick={TimePickerDialog(context,{_,h,m->update(enabled,h,m)},hour,minute,true).show()},enabled=!busy) {Text(String.format(Locale.US,"Pora: %02d:%02d",hour,minute))}
            Button(onClick={requirePermission("test")},enabled=!busy,modifier=Modifier.testTag("test_notification")) {Text("Wyślij testowe powiadomienie")}
            if(!allowed) Text("Zezwolenie na powiadomienia jest wyłączone.",color=MaterialTheme.colorScheme.error)
            TextButton(onClick={
                val intent=if(Build.VERSION.SDK_INT>=26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName)
                    else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            }) {Text("Ustawienia powiadomień telefonu")}
        } }
        OutlinedCard { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Twoje dane",style=MaterialTheme.typography.titleLarge)
            Text("Plany, treningi i opcjonalna lokalizacja pozostają w prywatnej bazie aplikacji. Nie wysyłamy ich do serwera. Dostęp chroni blokada Twojego telefonu.")
            Text("Możesz edytować i usuwać swoje wpisy. Odinstalowanie aplikacji usuwa dane; kopia w chmurze jest wyłączona.")
        } }
        Text("Projekt zaliczeniowy • wersja 1.0",style=MaterialTheme.typography.bodySmall)
    }
}
