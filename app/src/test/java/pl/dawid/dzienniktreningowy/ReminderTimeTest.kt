package pl.dawid.dzienniktreningowy
import java.util.Calendar
import org.junit.Test
import org.junit.Assert.*
import pl.dawid.dzienniktreningowy.domain.ReminderTime
class ReminderTimeTest {
    private fun now(hour:Int,minute:Int)=Calendar.getInstance().apply {set(2026,Calendar.OCTOBER,10,hour,minute,0);set(Calendar.MILLISECOND,0)}.timeInMillis
    @Test fun futureTimeIsToday() {assertEquals(3600000L,ReminderTime.delayMillis(now(17,0),18,0))}
    @Test fun pastTimeIsTomorrow() {assertEquals(23*3600000L,ReminderTime.delayMillis(now(19,0),18,0))}
    @Test fun exactTimeIsTomorrowNotImmediate() {assertEquals(24*3600000L,ReminderTime.delayMillis(now(18,0),18,0))}
    @Test(expected=IllegalArgumentException::class) fun rejectsInvalidHour() {ReminderTime.delayMillis(now(18,0),24,0)}
}
