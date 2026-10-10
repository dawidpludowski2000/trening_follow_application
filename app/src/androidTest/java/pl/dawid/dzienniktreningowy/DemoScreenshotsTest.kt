package pl.dawid.dzienniktreningowy

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.runner.RunWith
import pl.dawid.dzienniktreningowy.data.*
import pl.dawid.dzienniktreningowy.domain.*
import pl.dawid.dzienniktreningowy.ui.*
import java.io.File
import java.util.Calendar

/** Opcjonalny scenariusz prezentacyjny. Uruchamiaj wyłącznie na emulatorze testowym. */
@RunWith(AndroidJUnit4::class)
class DemoScreenshotsTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Before fun optIn() { Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("demo")=="true") }
    private fun screenshot(name:String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(500)
        val folder=File(compose.activity.getExternalFilesDir(null),"screenshots").apply {mkdirs()}
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().let { bitmap ->
            File(folder,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        }
    }
    @Test fun createDemoAndCaptureScreens() {
        runBlocking {
            val db=TrainingDatabase.get(compose.activity);val repo=TrainingRepository(db)
            db.dao().plans().first().forEach {repo.deletePlan(it.plan.id)}
            db.dao().workouts().first().forEach {repo.deleteWorkout(it.workout.id)}
            val strength=listOf(ExerciseInput("Przysiad","3","10","20"),ExerciseInput("Wyciskanie hantli","3","10","8"))
            repo.savePlan(0,"Trening całego ciała",strength)
            repo.savePlan(0,"Krótki trening w domu",listOf(ExerciseInput("Pompki","3","12","0"),ExerciseInput("Wykroki","3","10","0")))
            listOf(18,15,12,8,4,0).forEachIndexed {i,days ->
                val date=Calendar.getInstance().apply {add(Calendar.DAY_OF_YEAR,-days)}.timeInMillis
                repo.saveWorkout(Workout(name="Trening całego ciała",performedAt=date,durationMinutes=35+i*2,note=if(i==5) "Dobry trening. Następnym razem spróbuję większego ciężaru." else "",latitude=if(i==5) 52.2297 else null,longitude=if(i==5) 21.0122 else null),strength.mapIndexed {n,e->e.copy(weight=if(n==0)(20+i*2.5).toString() else "8")})
            }
        }
        compose.activityRule.scenario.onActivity {ViewModelProvider(it)[TrainingViewModel::class.java].apply {edit(null);selectTab("Plany")}}
        compose.waitUntil(10000) {compose.onAllNodesWithText("Trening całego ciała").fetchSemanticsNodes().isNotEmpty()}
        screenshot("01-plany")
        compose.onNodeWithTag("tab_1").performClick()
        screenshot("02-historia")
        compose.onNodeWithTag("tab_2").performClick()
        screenshot("03-postepy")
        compose.onNodeWithTag("tab_3").performClick()
        screenshot("04-ustawienia")
        compose.onNodeWithTag("tab_0").performClick()
        compose.onAllNodesWithText("Edytuj")[0].performScrollTo().performClick()
        compose.onNodeWithTag("editor_list").assertExists()
        screenshot("05-edytor")
        compose.onNodeWithText("Wróć").performClick();compose.onNodeWithText("Odrzuć").performClick()
        compose.onNodeWithTag("tab_2").performClick()
    }
}

