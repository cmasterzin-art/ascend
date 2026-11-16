package br.com.ascend.aetheris

import org.junit.Test
import org.junit.Assert.*
import java.time.*

class RemindersTest {
    private val h=Habit(name="Ler",created="2026-01-01",reminder=true,time="08:00")
    @Test fun restDaySkipsReminderWithoutChangingHabitSchedule(){
        val now=ZonedDateTime.parse("2026-11-16T07:00:00Z")
        val next=nextReminderAt(h,setOf("2026-11-16"),now)!!
        assertEquals("2026-11-17",next.toLocalDate().toString());assertEquals(8,next.hour)
        assertTrue(scheduled(h,"2026-11-16"))
    }
    @Test fun severalWeeksOfRestStillScheduleTheNextReminder(){
        val next=nextReminderAt(h.copy(days="1"),setOf("2026-11-23","2026-11-30"),ZonedDateTime.parse("2026-11-16T09:00:00Z"))!!
        assertEquals("2026-12-07",next.toLocalDate().toString())
    }
    @Test fun daylightSavingGapUsesNextValidLocalTime(){
        val now=ZonedDateTime.of(2026,3,8,0,30,0,0,ZoneId.of("America/New_York"))
        val next=nextReminderAt(h.copy(time="02:30",days="7"),emptySet(),now)!!
        assertEquals(LocalDate.of(2026,3,8),next.toLocalDate());assertEquals(3,next.hour);assertEquals(30,next.minute)
    }
    @Test fun disabledOrPausedHabitsHaveNoReminder(){
        val now=ZonedDateTime.parse("2026-11-16T07:00:00Z")
        assertNull(nextReminderAt(h.copy(reminder=false),emptySet(),now))
        assertNull(nextReminderAt(h.copy(paused=true),emptySet(),now))
    }
}
