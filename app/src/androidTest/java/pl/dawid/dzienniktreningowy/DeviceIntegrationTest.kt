package pl.dawid.dzienniktreningowy

import android.Manifest
import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import pl.dawid.dzienniktreningowy.device.*

@RunWith(AndroidJUnit4::class)
@androidx.test.filters.SdkSuppress(minSdkVersion=33)
class DeviceIntegrationTest {
    @get:Rule val permissions=GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.POST_NOTIFICATIONS)
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    @After fun clean() = runBlocking {
        Reminders.schedule(context,false,18,0)
        context.getSystemService(NotificationManager::class.java).cancelAll()
    }
    @Test fun reminderIsUniqueAndCanBeCancelled() = runBlocking {
        Reminders.schedule(context,true,18,0)
        Reminders.schedule(context,true,19,30)
        val infos=WorkManager.getInstance(context).getWorkInfosForUniqueWork(Reminders.WORK_NAME).get()
        assertEquals(1,infos.count {!it.state.isFinished})
        assertTrue(Reminders.preferences(context).getBoolean("enabled",false))
        assertEquals(19,Reminders.preferences(context).getInt("hour",0))
        assertEquals(30,Reminders.preferences(context).getInt("minute",0))
        Reminders.schedule(context,false,19,30)
        assertTrue(WorkManager.getInstance(context).getWorkInfosForUniqueWork(Reminders.WORK_NAME).get().all {it.state.isFinished})
        assertFalse(Reminders.preferences(context).getBoolean("enabled",true))
    }
    @Test fun workerPublishesNotificationOnlyWhenEnabled() = runBlocking {
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.cancelAll()
        Reminders.preferences(context).edit().putBoolean("enabled",false).commit()
        val worker=TestListenableWorkerBuilder<TrainingReminderWorker>(context).build()
        assertEquals(ListenableWorker.Result.success(),worker.doWork())
        assertTrue(manager.activeNotifications.isEmpty())
        Reminders.preferences(context).edit().putBoolean("enabled",true).commit()
        assertEquals(ListenableWorker.Result.success(),worker.doWork())
        val posted=manager.activeNotifications.single {it.id==Reminders.NOTIFICATION_ID}
        assertEquals("Czas na trening",posted.notification.extras.getString("android.title"))
        assertNotNull(posted.notification.contentIntent)
    }
}

/** Uruchamiany osobno po odebraniu uprawnień przez adb. */
@RunWith(AndroidJUnit4::class)
class PermissionDeniedTest {
    @Before fun scenario() { Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("deviceScenario")=="denied") }
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun noLocationPermissionIsHandled() = runBlocking {
        assertFalse(TrainingLocation.permitted(context))
        try {TrainingLocation.current(context);fail("Expected permission check")} catch(_:SecurityException) { }
    }
    @Test fun noNotificationPermissionDoesNotCrash() {
        assertFalse(Reminders.allowed(context))
        assertFalse(Reminders.notify(context))
    }
}

/** Uruchamiany osobno przy dostarczaniu współrzędnych przez emulator. */
@RunWith(AndroidJUnit4::class)
class LocationIntegrationTest {
    @get:Rule(order=1) val activity=androidx.test.ext.junit.rules.ActivityScenarioRule(MainActivity::class.java)
    @Before fun scenario() { Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("deviceScenario")=="gps") }
    @get:Rule(order=0) val permissions=GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
    @Test fun receivesCoordinatesFromDevice() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val location=TrainingLocation.current(context)
        assertNotNull("Emulator musi dostarczać pozycję GPS podczas testu",location)
        assertEquals(52.2297,location!!.latitude,0.001)
        assertEquals(21.0122,location.longitude,0.001)
    }
}


