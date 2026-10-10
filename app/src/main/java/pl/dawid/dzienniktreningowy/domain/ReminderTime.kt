package pl.dawid.dzienniktreningowy.domain
import java.util.Calendar
object ReminderTime {
    fun delayMillis(now: Long, hour: Int, minute: Int): Long {
        require(hour in 0..23 && minute in 0..59)
        val next=Calendar.getInstance().apply {timeInMillis=now;set(Calendar.HOUR_OF_DAY,hour);set(Calendar.MINUTE,minute);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}
        if(next.timeInMillis<=now) next.add(Calendar.DAY_OF_YEAR,1)
        return next.timeInMillis-now
    }
}
