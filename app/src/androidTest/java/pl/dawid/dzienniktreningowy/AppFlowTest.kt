package pl.dawid.dzienniktreningowy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import pl.dawid.dzienniktreningowy.data.*
import pl.dawid.dzienniktreningowy.domain.ExerciseInput
import pl.dawid.dzienniktreningowy.ui.*

@RunWith(AndroidJUnit4::class)
class AppFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun reset() {
        compose.activityRule.scenario.onActivity { a -> ViewModelProvider(a)[TrainingViewModel::class.java].apply { edit(null);selectTab("Plany") } }
        runBlocking {
            val db=TrainingDatabase.get(compose.activity)
            db.dao().plans().first().forEach {db.dao().deletePlan(it.plan.id)}
            db.dao().workouts().first().forEach {db.dao().deleteWorkout(it.workout.id)}
        }
        compose.waitUntil(10000) { compose.onAllNodesWithTag("new_plan").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun awaitText(text:String) = compose.waitUntil(10000) {compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}
    @Test fun planWorkoutStatisticsEditAndDelete() {
        compose.onNodeWithTag("new_plan").performClick()
        compose.onNodeWithTag("name").performTextInput("Plan UI")
        compose.onNodeWithTag("exercise_0").performScrollTo().performTextInput("Przysiad")
        compose.onNodeWithTag("weight_0").performScrollTo().performTextReplacement("20")
        closeSoftKeyboard()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("name").performScrollTo().assertTextContains("Plan UI")
        compose.onNodeWithTag("save").performClick()
        awaitText("Plan UI")
        compose.onNodeWithText("Zapisz wykonanie").performScrollTo().performClick()
        compose.onNodeWithTag("save").performClick()
        try { awaitText("Historia aktywności") } catch(e: Throwable) { throw AssertionError(compose.onRoot().printToString(), e) }
        compose.onNodeWithTag("tab_2").performClick()
        compose.onNodeWithText("1 treningów").assertExists()
        compose.onNodeWithText("600,0 kg łącznej objętości").assertExists()
        compose.onNodeWithTag("tab_1").performClick()
        compose.onNodeWithText("Edytuj").performScrollTo().performClick()
        compose.onNodeWithTag("name").performTextReplacement("Trening zmieniony")
        closeSoftKeyboard()
        compose.onNodeWithTag("save").performClick()
        awaitText("Trening zmieniony")
        compose.onNodeWithText("Usuń").performScrollTo().performClick()
        compose.onNodeWithText("Anuluj").performClick()
        compose.onNodeWithText("Trening zmieniony").assertExists()
        compose.onNodeWithText("Usuń").performScrollTo().performClick()
        compose.onNodeWithText("Usuń na stałe").performClick()
        awaitText("Jeszcze bez treningów")
        compose.onNodeWithTag("tab_0").performClick()
        compose.onNodeWithText("Edytuj").performScrollTo().performClick()
        compose.onNodeWithTag("name").performTextReplacement("Plan poprawiony")
        closeSoftKeyboard()
        compose.onNodeWithTag("save").performClick()
        awaitText("Plan poprawiony")
        compose.onNodeWithText("Usuń").performScrollTo().performClick()
        compose.onNodeWithText("Usuń na stałe").performClick()
        awaitText("Zacznij od jednego planu")
    }
    @Test fun invalidFormShowsErrorAndCanBeDiscarded() {
        compose.onNodeWithTag("new_plan").performClick()
        compose.onNodeWithTag("save").performClick()
        compose.onNodeWithTag("validation_error").assertTextContains("Nazwa musi mieć od 1 do 60 znaków.")
        compose.onNodeWithText("Wróć").performClick()
        compose.onNodeWithText("Zostań").performClick()
        compose.onNodeWithTag("name").assertExists()
        compose.onNodeWithText("Wróć").performClick()
        compose.onNodeWithText("Odrzuć").performClick()
        awaitText("Zacznij od jednego planu")
    }
    @Test fun draftSerializationPreservesAllFields() {
        val original=EditorDraft(kind="workout",id=4,name="Żółć",date=123456789,duration="20",note="Notatka",latitude=52.0,longitude=21.0,exercises=listOf(ExerciseInput("Przysiad","2","8","2,5")))
        assertEquals(original,EditorDraft.decode(original.encode()))
    }

    @Test fun settingsCanLoadDemoDataOnceAndItAppearsInStats() {
        compose.onNodeWithTag("tab_3").performClick()
        compose.onNodeWithTag("load_demo_data").assertIsEnabled().performClick()
        compose.waitUntil(10000) {
            runBlocking {
                val dao = TrainingDatabase.get(compose.activity).dao()
                dao.planCount() == 2 && dao.workoutCount() == 6
            }
        }
        compose.onNodeWithTag("load_demo_data").assertIsNotEnabled()
        compose.onNodeWithTag("tab_2").performClick()
        compose.onNodeWithText("6 treningów").assertExists()
        compose.onNodeWithText("Twoje postępy").assertExists()
    }
}


