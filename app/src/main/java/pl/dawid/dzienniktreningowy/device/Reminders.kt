package pl.dawid.dzienniktreningowy.device

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.dawid.dzienniktreningowy.MainActivity
import pl.dawid.dzienniktreningowy.R
import pl.dawid.dzienniktreningowy.domain.ReminderTime
import java.util.concurrent.TimeUnit

object Reminders {
    const val WORK_NAME="daily_training_reminder"
    const val CHANNEL="training_reminders"
    const val NOTIFICATION_ID=101
    fun preferences(context:Context)=context.getSharedPreferences("settings",Context.MODE_PRIVATE)
    fun allowed(context:Context):Boolean =
        (Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) && NotificationManagerCompat.from(context).areNotificationsEnabled() &&
        (Build.VERSION.SDK_INT<26 || context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE)
    fun channel(context:Context) {
        if(Build.VERSION.SDK_INT>=26) context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL,"Przypomnienia o treningu",NotificationManager.IMPORTANCE_DEFAULT).apply {description="Codzienne przypomnienie o aktywności"})
    }
    suspend fun schedule(context:Context,enabled:Boolean,hour:Int,minute:Int) = withContext(Dispatchers.IO) {
        require(hour in 0..23 && minute in 0..59)
        val manager=WorkManager.getInstance(context)
        if(enabled) {
            val request=PeriodicWorkRequestBuilder<TrainingReminderWorker>(24,TimeUnit.HOURS)
                .setInitialDelay(ReminderTime.delayMillis(System.currentTimeMillis(),hour,minute),TimeUnit.MILLISECONDS).build()
            manager.enqueueUniquePeriodicWork(WORK_NAME,ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,request).result.get()
        } else manager.cancelUniqueWork(WORK_NAME).result.get()
        check(preferences(context).edit().putBoolean("enabled",enabled).putInt("hour",hour).putInt("minute",minute).commit())
    }
    fun notify(context:Context):Boolean {
        channel(context)
        if(!allowed(context)) return false
        val intent=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification=NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Czas na trening")
            .setContentText("Zajrzyj do planu i znajdź chwilę na aktywność.")
            .setContentIntent(intent).setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
        return try { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID,notification);true } catch(_:SecurityException) {false}
    }
}
class TrainingReminderWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params) {
    override suspend fun doWork():Result {
        if(Reminders.preferences(applicationContext).getBoolean("enabled",false)) Reminders.notify(applicationContext)
        return Result.success()
    }
}
