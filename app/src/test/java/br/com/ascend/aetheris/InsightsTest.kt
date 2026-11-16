package br.com.ascend.aetheris

import org.junit.Test
import org.junit.Assert.*

class InsightsTest {
    private val h=Habit(id="a",name="A",created="2026-11-01",days="1,3,5")
    private fun record(date:String)=HabitRecord("a@$date","a",date,1,1,50,5,5,0,false,0,"Estudo","vez","A")
    @Test fun restPreservesScheduledStreakAndHistoricalBest(){
        val rs=listOf("2026-11-02","2026-11-04","2026-11-09").map{record(it)}
        val rests=setOf("2026-11-06")
        assertEquals(3,streak(h,rs,"2026-11-09",rests))
        assertEquals(3,bestStreak(h,rs,"2026-11-16",rests))
        assertEquals(2,bestStreak(h,rs,"2026-11-16"))
    }
    @Test fun unplannedActivitiesAndRestAreExcludedFromRate(){
        val rs=listOf("2026-11-02","2026-11-03","2026-11-04","2026-11-06").map{record(it)}
        val s=GameState(habits=listOf(h),records=rs,restDays=listOf(RestDay("2026-11-04")))
        val summary=periodSummary(s,"2026-11-02","2026-11-08")
        assertEquals(2,summary.planned);assertEquals(4,summary.completed);assertEquals(2,summary.scheduledCompleted);assertEquals(100,summary.rate)
    }
    @Test fun localDateRespectsTimeZoneAcrossMidnight(){
        val instant=java.time.Instant.parse("2026-11-16T01:30:00Z")
        assertEquals("2026-11-16",today(java.time.Clock.fixed(instant,java.time.ZoneOffset.UTC)))
        val brazil=today(java.time.Clock.fixed(instant,java.time.ZoneId.of("America/Sao_Paulo")))
        assertEquals("2026-11-15",brazil);assertEquals("2026-11-09",weekOf(brazil))
    }
    @Test fun noPlanHasNoMisleadingPercentage(){
        assertNull(periodSummary(GameState(),"2026-11-02","2026-11-08").rate)
    }
    @Test fun historicalScheduleIsUsedAfterArchival(){
        val archived=h.copy(archived=true,scheduleHistory="2026-11-01|1,3,5|false;2026-11-05|1,3,5|true")
        val s=GameState(habits=listOf(archived),records=listOf(record("2026-11-02")))
        val result=periodSummary(s,"2026-11-02","2026-11-08")
        assertEquals(2,result.planned);assertEquals(50,result.rate)
        assertEquals(1,s.due("2026-11-02").size)
    }
}
